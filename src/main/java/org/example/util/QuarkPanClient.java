package org.example.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.QuarkPanConfig;
import org.example.dto.quark.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 夸克网盘API客户端
 */
@Component
public class QuarkPanClient {
    
    private static final Logger logger = LoggerFactory.getLogger(QuarkPanClient.class);
    
    @Autowired
    private HttpClientUtil httpClientUtil;
    
    @Autowired
    private QuarkPanConfig quarkPanConfig;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取分享页面token
     */
    public String getSharePageToken(String pwdId) {
        try {
            String url = String.format(
                "https://drive-h.quark.cn/1/clouddrive/share/sharepage/token?pr=ucpro&fr=pc&uc_param_str=&__dt=709&__t=%d",
                System.currentTimeMillis()
            );
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("pwd_id", pwdId);
            requestBody.put("passcode", "");
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            String response = httpClientUtil.postJson(url, jsonBody, getDefaultHeaders());
            
            if (response != null) {
                QuarkApiResponse<Map<String, Object>> apiResponse = objectMapper.readValue(
                    response, new TypeReference<QuarkApiResponse<Map<String, Object>>>() {}
                );
                
                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    return (String) apiResponse.getData().get("stoken");
                }
            }
            
            logger.error("获取分享页面token失败，pwdId: {}", pwdId);
            return null;
            
        } catch (Exception e) {
            logger.error("获取分享页面token时发生错误，pwdId: {}", pwdId, e);
            return null;
        }
    }

    /**
     * 获取分享文件列表
     */
    public QuarkFileListData getShareFileList(String pwdId, String stoken, String pdirFid) {
        try {
            String encodedStoken = URLEncoder.encode(stoken, "UTF-8");
            String url = String.format(
                "https://drive-h.quark.cn/1/clouddrive/share/sharepage/detail?pr=ucpro&fr=pc&uc_param_str=&pwd_id=%s&stoken=%s&pdir_fid=%s&force=0&_page=1&_size=50&_fetch_banner=1&_fetch_share=1&_fetch_total=1&_sort=file_type:asc,updated_at:desc&__dt=1032&__t=%d",
                pwdId, encodedStoken, pdirFid != null ? pdirFid : "0", System.currentTimeMillis()
            );
            
            String response = httpClientUtil.get(url, getDefaultHeaders());
            
            if (response != null) {
                QuarkApiResponse<QuarkFileListData> apiResponse = objectMapper.readValue(
                    response, new TypeReference<QuarkApiResponse<QuarkFileListData>>() {}
                );
                
                if (apiResponse.isSuccess()) {
                    return apiResponse.getData();
                }
            }
            
            logger.error("获取分享文件列表失败，pwdId: {}, pdirFid: {}", pwdId, pdirFid);
            return null;
            
        } catch (Exception e) {
            logger.error("获取分享文件列表时发生错误，pwdId: {}, pdirFid: {}", pwdId, pdirFid, e);
            return null;
        }
    }

    /**
     * 保存文件到网盘 - 使用新的API格式，直接保存所有文件到指定目录
     */
    public List<String> saveFiles(String pwdId, String stoken, String toPdirFid) {
        try {
            String url = String.format(
                "https://drive-pc.quark.cn/1/clouddrive/share/sharepage/save?pr=ucpro&fr=pc&uc_param_str=&__dt=779577&__t=%d",
                System.currentTimeMillis()
            );

            // 使用新的API参数格式，直接保存所有文件
            QuarkSaveRequest saveRequest = new QuarkSaveRequest(
                new ArrayList<>(),  // fid_list 为空
                new ArrayList<>(),  // fid_token_list 为空
                toPdirFid,          // to_pdir_fid
                pwdId,              // pwd_id
                stoken,             // stoken
                "0",                // pdir_fid 设为 "0" 表示根目录
                true,               // pdir_save_all 设为 true 表示保存所有文件
                new ArrayList<>(),  // exclude_fids 为空
                "link"              // scene 设为 "link"
            );

            String jsonBody = objectMapper.writeValueAsString(saveRequest);
            logger.info("保存请求参数: {}", jsonBody);

            String response = httpClientUtil.postJson(url, jsonBody, getDefaultHeaders());

            if (response != null) {
                QuarkApiResponse<QuarkSaveData> apiResponse = objectMapper.readValue(
                    response, new TypeReference<QuarkApiResponse<QuarkSaveData>>() {}
                );

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    return waitForSaveTask(apiResponse.getData().getTaskId());
                }
            }

            logger.error("保存文件失败，pwdId: {}", pwdId);
            return null;

        } catch (Exception e) {
            logger.error("保存文件时发生错误，pwdId: {}", pwdId, e);
            return null;
        }
    }

    /**
     * 等待保存任务完成
     */
    private List<String> waitForSaveTask(String taskId) {
        try {
            int retryCount = 0;
            
            while (retryCount < quarkPanConfig.getMaxRetryCount()) {
                Thread.sleep(quarkPanConfig.getRequestInterval());
                
                String url = String.format(
                    "https://drive-pc.quark.cn/1/clouddrive/task?pr=ucpro&fr=pc&uc_param_str=&task_id=%s&retry_index=1&__dt=9565&__t=%d",
                    taskId, System.currentTimeMillis()
                );
                
                String response = httpClientUtil.get(url, getDefaultHeaders());
                
                if (response != null) {
                    QuarkApiResponse<QuarkSaveData> apiResponse = objectMapper.readValue(
                        response, new TypeReference<QuarkApiResponse<QuarkSaveData>>() {}
                    );
                    
                    if (apiResponse.getCode() != null && apiResponse.getCode() == 41035) {
                        logger.error("单次转存文件个数超出用户等级限制");
                        return null;
                    }
                    
                    if (apiResponse.isSuccess() && apiResponse.getData() != null && 
                        apiResponse.getData().getSaveAs() != null &&
                        apiResponse.getData().getSaveAs().getSaveAsTopFids() != null) {
                        
                        List<Object> topFids = apiResponse.getData().getSaveAs().getSaveAsTopFids();
                        return topFids.stream()
                                .map(Object::toString)
                                .collect(java.util.stream.Collectors.toList());
                    }
                }
                
                retryCount++;
            }
            
            logger.error("等待保存任务完成超时，taskId: {}", taskId);
            return null;
            
        } catch (Exception e) {
            logger.error("等待保存任务完成时发生错误，taskId: {}", taskId, e);
            return null;
        }
    }

    /**
     * 创建文件夹
     */
    public String createFolder(String fileName, String pdirFid) {
        try {
            randomPause();

            String url = "https://drive-pc.quark.cn/1/clouddrive/file?pr=ucpro&fr=pc&uc_param_str=";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("pdir_fid", pdirFid);
            requestBody.put("file_name", fileName);
            requestBody.put("dir_path", "");
            requestBody.put("dir_init_lock", false);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            String response = httpClientUtil.postJson(url, jsonBody, getDefaultHeaders());

            if (response != null) {
                QuarkApiResponse<Map<String, Object>> apiResponse = objectMapper.readValue(
                    response, new TypeReference<QuarkApiResponse<Map<String, Object>>>() {}
                );

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    return (String) apiResponse.getData().get("fid");
                }
            }

            logger.error("创建文件夹失败，fileName: {}, pdirFid: {}", fileName, pdirFid);
            return null;

        } catch (Exception e) {
            logger.error("创建文件夹时发生错误，fileName: {}, pdirFid: {}", fileName, pdirFid, e);
            return null;
        }
    }

    /**
     * 创建分享链接
     */
    public String createShareUrl(List<String> fidList) {
        try {
            String shareId = createShare(fidList);
            if (shareId == null) {
                return null;
            }

            return getSharePassword(shareId);

        } catch (Exception e) {
            logger.error("创建分享链接时发生错误", e);
            return null;
        }
    }

    /**
     * 创建分享
     */
    private String createShare(List<String> fidList) {
        try {
            String url = "https://drive-pc.quark.cn/1/clouddrive/share?pr=ucpro&fr=pc&uc_param_str=";

            QuarkShareRequest shareRequest = new QuarkShareRequest(fidList, "", 1, 1);
            String jsonBody = objectMapper.writeValueAsString(shareRequest);

            String response = httpClientUtil.postJson(url, jsonBody, getDefaultHeaders());

            if (response != null) {
                QuarkApiResponse<Map<String, Object>> apiResponse = objectMapper.readValue(
                        response, new TypeReference<QuarkApiResponse<Map<String, Object>>>() {}
                );

                if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                    Map<String, Object> data = apiResponse.getData();

                    // 检查是否有直接的分享ID
                    Map<String, Object> taskResp = (Map<String, Object>) data.get("task_resp");
                    if (taskResp != null) {
                        Map<String, Object> taskData = (Map<String, Object>) taskResp.get("data");
                        if (taskData != null) {
                            String shareId = (String) taskData.get("share_id");
                            if (shareId != null) {
                                return shareId;
                            }
                        }
                    }

                    // 如果没有直接的分享ID，等待任务完成
                    String taskId = (String) data.get("task_id");
                    logger.info("taskId:{}",taskId);
                    if (taskId != null) {
                        return waitForShareTask(taskId);
                    }
                }
            }

            logger.error("创建分享失败");
            return null;

        } catch (Exception e) {
            logger.error("创建分享时发生错误", e);
            return null;
        }
    }

    /**
     * 等待分享任务完成
     */
    private String waitForShareTask(String taskId) {
        try {
            int retryCount = 0;
            while (retryCount < quarkPanConfig.getMaxRetryCount()) {
                logger.info("waitForShareTask-retryCount:{}",retryCount);
                String url = String.format(
                        "https://drive-pc.quark.cn/1/clouddrive/task?pr=ucpro&fr=pc&uc_param_str=&retry_index=1&task_id=%s",
                        taskId
                );

                String response = httpClientUtil.get(url, getDefaultHeaders());

                if (response != null) {
                    QuarkApiResponse<Map<String, Object>> apiResponse = objectMapper.readValue(
                            response, new TypeReference<QuarkApiResponse<Map<String, Object>>>() {}
                    );

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        String shareUrl = (String) apiResponse.getData().get("share_id");
                        if (StringUtils.hasLength(shareUrl)) {
                            return shareUrl;
                        }
                    }
                }

                retryCount++;
                randomPause();
            }

            logger.error("等待分享任务完成超时，taskId: {}", taskId);
            return null;
        } catch (Exception e) {
            logger.error("等待分享任务完成时发生错误，taskId: {}", taskId, e);
            return null;
        }
    }

    /**
     * 获取分享密码链接
     */
    private String getSharePassword(String shareId) {
        try {
            int retryCount = 0;

            while (retryCount < quarkPanConfig.getMaxRetryCount()) {
                logger.info("getSharePassword-waitForShareTask-retryCount:{}",retryCount);
                String url = "https://drive-pc.quark.cn/1/clouddrive/share/password?pr=ucpro&fr=pc&uc_param_str=";

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("share_id", shareId);

                String jsonBody = objectMapper.writeValueAsString(requestBody);
                String response = httpClientUtil.postJson(url, jsonBody, getDefaultHeaders());

                if (response != null) {
                    QuarkApiResponse<Map<String, Object>> apiResponse = objectMapper.readValue(
                        response, new TypeReference<QuarkApiResponse<Map<String, Object>>>() {}
                    );

                    if (apiResponse.getStatus() != null && apiResponse.getStatus() == 200 &&
                        apiResponse.getData() != null) {
                        String shareUrl = (String) apiResponse.getData().get("share_url");
                        if (StringUtils.hasLength(shareUrl)) {
                            logger.info("获取分享链接成功");
                            return shareUrl;
                        }
                    }
                }

                retryCount++;
                randomPause();
            }

            logger.error("获取分享密码链接失败，shareId: {}", shareId);
            return null;

        } catch (Exception e) {
            logger.error("获取分享密码链接时发生错误，shareId: {}", shareId, e);
            return null;
        }
    }

    /**
     * 随机暂停
     */
    private void randomPause() {
        try {
            int pauseDuration = (int) (Math.random() * 1000) + 500; // 0.5秒到1.5秒之间随机暂停
            Thread.sleep(pauseDuration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 获取默认请求头
     */
    private Map<String, String> getDefaultHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("cookie", quarkPanConfig.getCookie());
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.5414.121 Safari/537.36");
        return headers;
    }
}
