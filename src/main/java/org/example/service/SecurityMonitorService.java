package org.example.service;

import org.example.mapper.VerifyAccessLogMapper;
import org.example.mapper.VerifyChallengeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 安全监控服务
 * 负责频率限制、异常检测和安全告警
 */
@Service
public class SecurityMonitorService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityMonitorService.class);

    // 频率限制阈值
    private static final int IP_RATE_LIMIT_PER_MINUTE = 10;
    private static final int DEVICE_RATE_LIMIT_PER_MINUTE = 5;
    private static final int IP_FAILURE_THRESHOLD = 10;
    private static final int DEVICE_FAILURE_THRESHOLD = 5;

    @Autowired
    private VerifyAccessLogMapper accessLogMapper;

    @Autowired
    private VerifyChallengeMapper challengeMapper;

    // 内存中的频率限制计数器（生产环境应使用Redis）
    private final ConcurrentHashMap<String, RateLimitCounter> ipRateLimitMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RateLimitCounter> deviceRateLimitMap = new ConcurrentHashMap<>();

    /**
     * 检查频率限制
     */
    public boolean checkRateLimit(String ipAddress, String deviceFingerprint) {
        // 检查IP频率限制
        if (ipAddress != null && !checkIpRateLimit(ipAddress)) {
            logger.warn("IP频率限制触发: {}", ipAddress);
            sendSecurityAlert(String.format("IP %s 请求频率过高", ipAddress));
            return false;
        }

        // 检查设备频率限制（只有当设备指纹不为空时才检查）
        if (deviceFingerprint != null && !deviceFingerprint.trim().isEmpty() && !checkDeviceRateLimit(deviceFingerprint)) {
            logger.warn("设备频率限制触发: {}", deviceFingerprint);
            sendSecurityAlert(String.format("设备 %s 请求频率过高", deviceFingerprint));
            return false;
        }

        // 检查失败次数
        if (!checkFailureThreshold(ipAddress, deviceFingerprint)) {
            return false;
        }

        return true;
    }

    /**
     * 检查IP频率限制
     */
    private boolean checkIpRateLimit(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return true; // 如果IP为空，允许通过
        }
        RateLimitCounter counter = ipRateLimitMap.computeIfAbsent(ipAddress, k -> new RateLimitCounter());
        return counter.increment() <= IP_RATE_LIMIT_PER_MINUTE;
    }

    /**
     * 检查设备频率限制
     */
    private boolean checkDeviceRateLimit(String deviceFingerprint) {
        if (deviceFingerprint == null || deviceFingerprint.trim().isEmpty()) {
            return true; // 如果设备指纹为空，允许通过
        }
        RateLimitCounter counter = deviceRateLimitMap.computeIfAbsent(deviceFingerprint, k -> new RateLimitCounter());
        return counter.increment() <= DEVICE_RATE_LIMIT_PER_MINUTE;
    }

    /**
     * 检查失败阈值
     */
    private boolean checkFailureThreshold(String ipAddress, String deviceFingerprint) {
        try {
            // 检查IP失败次数（最近10分钟）
            if (ipAddress != null && !ipAddress.trim().isEmpty()) {
                int ipFailures = challengeMapper.countFailuresByIpAddress(ipAddress, 10);
                if (ipFailures >= IP_FAILURE_THRESHOLD) {
                    logger.warn("IP失败次数超过阈值: {}, 失败次数: {}", ipAddress, ipFailures);
                    sendSecurityAlert(String.format("IP %s 失败次数过多: %d", ipAddress, ipFailures));
                    return false;
                }
            }

            // 检查设备失败次数（最近10分钟）
            if (deviceFingerprint != null && !deviceFingerprint.trim().isEmpty()) {
                int deviceFailures = challengeMapper.countFailuresByDeviceFingerprint(deviceFingerprint, 10);
                if (deviceFailures >= DEVICE_FAILURE_THRESHOLD) {
                    logger.warn("设备失败次数超过阈值: {}, 失败次数: {}", deviceFingerprint, deviceFailures);
                    sendSecurityAlert(String.format("设备 %s 失败次数过多: %d", deviceFingerprint, deviceFailures));
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            logger.error("检查失败阈值时发生错误", e);
            return true; // 出错时允许通过，避免影响正常用户
        }
    }

    /**
     * 监控验证尝试
     */
    public void monitorVerificationAttempt(String ipAddress, String deviceFingerprint, boolean success) {
        try {
            if (!success) {
                // 记录失败尝试
                logger.info("验证失败 - IP: {}, 设备: {}", ipAddress, deviceFingerprint);
            }

            // 检查异常模式
            checkAnomalousPatterns(ipAddress, deviceFingerprint);

        } catch (Exception e) {
            logger.error("监控验证尝试时发生错误", e);
        }
    }

    /**
     * 检查异常模式
     */
    private void checkAnomalousPatterns(String ipAddress, String deviceFingerprint) {
        try {
            // 检查IP访问模式
            if (ipAddress != null && !ipAddress.trim().isEmpty()) {
                int ipAttempts = accessLogMapper.countByIpAddress(ipAddress, 60); // 最近1小时
                int ipFailures = accessLogMapper.countFailuresByIpAddress(ipAddress, 60);

                if (ipAttempts > 50) {
                    double successRate = (double) (ipAttempts - ipFailures) / ipAttempts;
                    if (successRate > 0.95) {
                        sendSecurityAlert(String.format("IP %s 成功率异常高: %.2f%%, 尝试次数: %d",
                                                       ipAddress, successRate * 100, ipAttempts));
                    }
                }
            }

            // 检查设备访问模式
            if (deviceFingerprint != null && !deviceFingerprint.trim().isEmpty()) {
                int deviceAttempts = accessLogMapper.countByDeviceFingerprint(deviceFingerprint, 60);
                int deviceFailures = accessLogMapper.countFailuresByDeviceFingerprint(deviceFingerprint, 60);

                if (deviceAttempts > 30) {
                    double successRate = (double) (deviceAttempts - deviceFailures) / deviceAttempts;
                    if (successRate > 0.95) {
                        sendSecurityAlert(String.format("设备 %s 成功率异常高: %.2f%%, 尝试次数: %d",
                                                       deviceFingerprint, successRate * 100, deviceAttempts));
                    }
                }
            }

        } catch (Exception e) {
            logger.error("检查异常模式时发生错误", e);
        }
    }

    /**
     * 发送安全告警
     */
    private void sendSecurityAlert(String message) {
        logger.warn("SECURITY ALERT: {}", message);
        // 这里可以集成钉钉、企业微信、邮件等告警方式
        // 例如：notificationService.sendAlert(message);
    }

    /**
     * 清理过期的频率限制计数器
     */
    public void cleanupExpiredCounters() {
        long currentTime = System.currentTimeMillis();
        
        ipRateLimitMap.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().getLastUpdateTime() > 60000); // 1分钟过期
        
        deviceRateLimitMap.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().getLastUpdateTime() > 60000);
        
        logger.debug("清理过期计数器完成，IP计数器: {}, 设备计数器: {}", 
                    ipRateLimitMap.size(), deviceRateLimitMap.size());
    }

    /**
     * 获取安全统计信息
     */
    public SecurityStats getSecurityStats() {
        try {
            // 获取异常IP列表
            int anomalousIpCount = accessLogMapper.selectAnomalousIpAddresses(50, 0.95, 60).size();
            
            // 获取异常设备列表
            int anomalousDeviceCount = accessLogMapper.selectAnomalousDeviceFingerprints(30, 0.95, 60).size();
            
            // 获取活跃计数器数量
            int activeIpCounters = ipRateLimitMap.size();
            int activeDeviceCounters = deviceRateLimitMap.size();
            
            return new SecurityStats(anomalousIpCount, anomalousDeviceCount, activeIpCounters, activeDeviceCounters);
            
        } catch (Exception e) {
            logger.error("获取安全统计信息失败", e);
            return new SecurityStats(0, 0, 0, 0);
        }
    }

    /**
     * 频率限制计数器
     */
    private static class RateLimitCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long lastUpdateTime = System.currentTimeMillis();
        private volatile long windowStart = System.currentTimeMillis();

        public int increment() {
            long currentTime = System.currentTimeMillis();
            
            // 如果超过1分钟，重置计数器
            if (currentTime - windowStart > 60000) {
                count.set(0);
                windowStart = currentTime;
            }
            
            lastUpdateTime = currentTime;
            return count.incrementAndGet();
        }

        public long getLastUpdateTime() {
            return lastUpdateTime;
        }
    }

    /**
     * 安全统计信息
     */
    public static class SecurityStats {
        private final int anomalousIpCount;
        private final int anomalousDeviceCount;
        private final int activeIpCounters;
        private final int activeDeviceCounters;

        public SecurityStats(int anomalousIpCount, int anomalousDeviceCount, 
                           int activeIpCounters, int activeDeviceCounters) {
            this.anomalousIpCount = anomalousIpCount;
            this.anomalousDeviceCount = anomalousDeviceCount;
            this.activeIpCounters = activeIpCounters;
            this.activeDeviceCounters = activeDeviceCounters;
        }

        // Getters
        public int getAnomalousIpCount() { return anomalousIpCount; }
        public int getAnomalousDeviceCount() { return anomalousDeviceCount; }
        public int getActiveIpCounters() { return activeIpCounters; }
        public int getActiveDeviceCounters() { return activeDeviceCounters; }
    }
}
