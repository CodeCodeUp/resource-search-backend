package org.example.service;

import org.example.config.QuarkPanConfig;
import org.example.dto.quark.QuarkFileListData;
import org.example.util.QuarkPanClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 夸克网盘服务类
 */
@Service
public class QuarkPanService {
    
    private static final Logger logger = LoggerFactory.getLogger(QuarkPanService.class);
    
    // 夸克网盘链接正则表达式
    private static final Pattern QUARK_URL_PATTERN = Pattern.compile("https://pan\\.quark\\.cn/s/([a-zA-Z0-9]+)");
    
    @Autowired
    private QuarkPanClient quarkPanClient;
    
    @Autowired
    private QuarkPanConfig quarkPanConfig;

    /**
     * 替换文本中的夸克网盘链接
     */
    public String replaceQuarkUrls(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        logger.info("开始处理文本中的夸克网盘链接");
        
        Matcher matcher = QUARK_URL_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String pwdId = matcher.group(1);
            logger.info("提取到链接标识符: {}", pwdId);
            
            String newUrl = convertQuarkUrl(pwdId);
            if (newUrl != null) {
                matcher.appendReplacement(result, newUrl);
                logger.info("链接转换成功: {} -> {}", matcher.group(0), newUrl);
            } else {
                logger.error("链接转换失败: {}", matcher.group(0));
                matcher.appendReplacement(result, matcher.group(0)); // 保持原链接
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    /**
     * 转换单个夸克网盘链接
     */
    public String convertQuarkUrl(String pwdId) {
        try {
            logger.info("开始转换链接，pwdId: {}", pwdId);
            
            // 1. 获取分享页面token
            String stoken = quarkPanClient.getSharePageToken(pwdId);
            if (stoken == null) {
                logger.error("获取stoken失败，pwdId: {}", pwdId);
                return null;
            }
            logger.info("获取stoken成功，pwdId: {}", pwdId);
            
            // 2. 获取文件列表
            QuarkFileListData fileListData = quarkPanClient.getShareFileList(pwdId, stoken, null);
            if (fileListData == null || fileListData.getList() == null || fileListData.getList().isEmpty()) {
                logger.error("获取文件列表失败或为空，pwdId: {}", pwdId);
                return null;
            }
            
            logger.info("获取到文件列表，数量: {}", fileListData.getList().size());

            // 3. 保存文件到网盘 - 使用新API直接保存所有文件和文件夹到默认目录
            // 不再需要分离文件和文件夹，API会自动处理所有内容
            List<String> savedFids = quarkPanClient.saveFiles(pwdId, stoken, quarkPanConfig.getDefaultSaveDirectoryId());
            if (savedFids == null || savedFids.isEmpty()) {
                logger.error("保存文件失败，pwdId: {}", pwdId);
                return null;
            }
            logger.info("保存文件成功，保存的文件数量: {}", savedFids.size());
            
            // 4. 创建新的分享链接
            String shareUrl = quarkPanClient.createShareUrl(savedFids);
            if (shareUrl != null) {
                logger.info("创建分享链接成功，pwdId: {}", pwdId);
                return shareUrl;
            } else {
                logger.error("创建分享链接失败，pwdId: {}", pwdId);
                return null;
            }
            
        } catch (Exception e) {
            logger.error("转换链接时发生错误，pwdId: {}", pwdId, e);
            return null;
        }
    }



    /**
     * 提取文本中的夸克网盘链接标识符
     */
    public List<String> extractQuarkUrlIds(String text) {
        List<String> urlIds = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return urlIds;
        }
        
        Matcher matcher = QUARK_URL_PATTERN.matcher(text);
        while (matcher.find()) {
            urlIds.add(matcher.group(1));
        }
        
        return urlIds;
    }
}
