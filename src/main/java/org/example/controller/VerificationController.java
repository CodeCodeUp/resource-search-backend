package org.example.controller;

import org.example.dto.*;
import org.example.entity.Resource;
import org.example.service.ResourceService;
import org.example.service.SecurityMonitorService;
import org.example.service.SlideVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 滑动验证控制器
 */
@RestController
@RequestMapping("/verify")
@CrossOrigin(origins = "*")
public class VerificationController {

    private static final Logger logger = LoggerFactory.getLogger(VerificationController.class);

    @Autowired
    private SlideVerificationService verificationService;

    @Autowired
    private SecurityMonitorService securityMonitorService;

    @Autowired
    private ResourceService resourceService;

    /**
     * 获取验证挑战
     */
    @GetMapping("/challenge")
    public ResponseEntity<Map<String, Object>> getChallenge(@RequestParam(required = false) String deviceFingerprint,
                                                           @RequestParam(required = false) String userAgent,
                                                           @RequestParam(required = false) Long timestamp,
                                                           HttpServletRequest httpRequest) {
        logger.info("API调用：获取验证挑战");

        try {
            String ipAddress = getClientIpAddress(httpRequest);

            // 从请求头获取设备指纹和用户代理（如果参数中没有提供）
            if (deviceFingerprint == null || deviceFingerprint.trim().isEmpty()) {
                deviceFingerprint = httpRequest.getHeader("X-Device-Fingerprint");
                if (deviceFingerprint == null) {
                    deviceFingerprint = "unknown_device_" + System.currentTimeMillis(); // 生成默认设备指纹
                }
            }
            if (userAgent == null || userAgent.trim().isEmpty()) {
                userAgent = httpRequest.getHeader("User-Agent");
                if (userAgent == null) {
                    userAgent = "Unknown User Agent";
                }
            }
            if (timestamp == null) {
                timestamp = System.currentTimeMillis();
            }

            VerifyChallengeRequest request = new VerifyChallengeRequest(deviceFingerprint, userAgent, timestamp);
            VerifyChallengeResponse response = verificationService.generateChallenge(request, ipAddress);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);

            logger.info("验证挑战生成成功，挑战ID: {}", response.getChallengeId());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("获取验证挑战失败: {}", e.getMessage(), e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 验证滑动结果
     */
    @PostMapping("/slide")
    public ResponseEntity<Map<String, Object>> verifySlide(@Valid @RequestBody VerifySlideRequest request,
                                                          HttpServletRequest httpRequest) {
        logger.info("API调用：验证滑动结果");

        try {
            request.setDeviceFingerprint(httpRequest.getHeader("X-Device-Fingerprint"));
            request.setUserAgent(httpRequest.getHeader("User-Agent"));
            String ipAddress = getClientIpAddress(httpRequest);
            
            VerifySlideResponse response = verificationService.verifySlide(request, ipAddress);
            
            // 监控验证尝试
            securityMonitorService.monitorVerificationAttempt(
                ipAddress, request.getDeviceFingerprint(), response.getVerified()
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);
            
            if (response.getVerified()) {
                logger.info("滑动验证成功，会话ID: {}", response.getSessionId());
            } else {
                logger.info("滑动验证失败");
            }
            
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("验证滑动结果失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取资源访问令牌
     * 注意：此接口只返回访问令牌，不返回资源URL
     * 需要通过 /validate-token 接口验证令牌后才能获取真实URL
     */
    @PostMapping("/access-token")
    public ResponseEntity<Map<String, Object>> getAccessToken(@RequestBody Map<String, String> request,
                                                             HttpServletRequest httpRequest) {
        logger.info("API调用：获取资源访问令牌");

        try {
            String resourceId = request.get("resourceId");
            String verifyToken = request.get("verifyToken");
            String sessionId = request.get("sessionId");

            if (resourceId == null || verifyToken == null || sessionId == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "缺少必要参数");
                return ResponseEntity.badRequest().body(result);
            }

            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            String deviceFingerprint = httpRequest.getHeader("X-Device-Fingerprint");

            // 验证访问令牌
            boolean valid = verificationService.validateAccessToken(
                verifyToken, resourceId, "access", ipAddress, userAgent, deviceFingerprint
            );

            if (!valid) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "验证令牌无效或已过期");
                return ResponseEntity.badRequest().body(result);
            }

            // 验证资源是否存在（但不返回URL）
            Optional<ResourceResponse> resourceOpt = resourceService.getResourceById(Integer.valueOf(resourceId));
            if (!resourceOpt.isPresent()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "资源不存在");
                return ResponseEntity.badRequest().body(result);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);

            Map<String, Object> data = new HashMap<>();
            data.put("accessToken", verifyToken); // 重用验证令牌作为访问令牌
            data.put("expiresAt", System.currentTimeMillis() + 3600000); // 1小时后过期
            // 注意：这里不返回resourceUrl，需要通过validate-token接口验证后才能获取
            result.put("data", data);

            logger.info("资源访问令牌获取成功，资源ID: {}", resourceId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("获取资源访问令牌失败: {}", e.getMessage(), e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 验证访问令牌并获取资源信息
     * 只有验证成功后才返回真实的资源URL和详细信息
     */
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request,
                                                            HttpServletRequest httpRequest) {
        logger.info("API调用：验证访问令牌");

        try {
            String token = request.get("token");

            if (token == null || token.trim().isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "缺少访问令牌");
                return ResponseEntity.badRequest().body(result);
            }

            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            String deviceFingerprint = httpRequest.getHeader("X-Device-Fingerprint");

            // 通过令牌获取资源ID
            String resourceId = verificationService.getResourceIdFromToken(token);
            if (resourceId == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "无效的访问令牌");
                return ResponseEntity.badRequest().body(result);
            }

            boolean valid = verificationService.validateAccessToken(
                token, resourceId, "view", ipAddress, userAgent, deviceFingerprint
            );

            if (valid) {
                // 如果验证成功，返回资源数据
                Optional<ResourceResponse> resourceOpt = resourceService.getResourceById(Integer.valueOf(resourceId));

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);

                Map<String, Object> data = new HashMap<>();
                data.put("valid", true);

                if (resourceOpt.isPresent()) {
                    ResourceResponse resource = resourceOpt.get();
                    Map<String, Object> resourceData = new HashMap<>();
                    resourceData.put("id", resource.getId());
                    resourceData.put("name", resource.getName());
                    resourceData.put("url", resource.getUrl());
                    resourceData.put("type", resource.getType());
                    data.put("resourceData", resourceData);
                }

                result.put("data", data);

                logger.info("访问令牌验证成功，资源ID: {}", resourceId);
                return ResponseEntity.ok(result);
            } else {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);

                Map<String, Object> data = new HashMap<>();
                data.put("valid", false);
                result.put("data", data);

                logger.info("访问令牌验证失败，资源ID: {}", resourceId);
                return ResponseEntity.ok(result);
            }

        } catch (Exception e) {
            logger.error("验证访问令牌失败: {}", e.getMessage(), e);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取安全统计信息
     */
    @GetMapping("/security-stats")
    public ResponseEntity<Map<String, Object>> getSecurityStats() {
        logger.info("API调用：获取安全统计信息");

        try {
            SecurityMonitorService.SecurityStats stats = securityMonitorService.getSecurityStats();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", stats);
            
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("获取安全统计信息失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 清理过期数据
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanup() {
        logger.info("API调用：清理过期数据");

        try {
            securityMonitorService.cleanupExpiredCounters();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "清理完成");
            
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("清理过期数据失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
