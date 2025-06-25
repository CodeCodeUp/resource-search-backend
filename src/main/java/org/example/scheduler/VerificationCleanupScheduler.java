package org.example.scheduler;

import org.example.mapper.VerifyAccessLogMapper;
import org.example.mapper.VerifyChallengeMapper;
import org.example.mapper.VerifySessionMapper;
import org.example.service.SecurityMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 验证系统清理定时任务
 * 定期清理过期数据和维护系统性能
 */
@Component
public class VerificationCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(VerificationCleanupScheduler.class);

    @Autowired
    private VerifyChallengeMapper challengeMapper;

    @Autowired
    private VerifySessionMapper sessionMapper;

    @Autowired
    private VerifyAccessLogMapper accessLogMapper;

    @Autowired
    private SecurityMonitorService securityMonitorService;

    /**
     * 清理过期的验证挑战
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void cleanupExpiredChallenges() {
        logger.info("开始清理过期的验证挑战...");

        try {
            LocalDateTime expiredBefore = LocalDateTime.now().minusHours(1); // 清理24小时前的数据
            int deletedCount = challengeMapper.deleteExpired(expiredBefore);
            
            logger.info("清理过期验证挑战完成，删除 {} 条记录", deletedCount);

        } catch (Exception e) {
            logger.error("清理过期验证挑战失败", e);
        }
    }

    /**
     * 清理过期的验证会话
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void cleanupExpiredSessions() {
        logger.info("开始清理过期的验证会话...");

        try {
            LocalDateTime expiredBefore = LocalDateTime.now().minusHours(1); // 清理24小时前的数据
            int deletedCount = sessionMapper.deleteExpired(expiredBefore);
            
            logger.info("清理过期验证会话完成，删除 {} 条记录", deletedCount);

        } catch (Exception e) {
            logger.error("清理过期验证会话失败", e);
        }
    }

    /**
     * 清理旧的访问日志
     * 每天执行一次
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupOldAccessLogs() {
        logger.info("开始清理旧的访问日志...");

        try {
            LocalDateTime olderThan = LocalDateTime.now().minusDays(1); // 清理30天前的日志
            int deletedCount = accessLogMapper.deleteOldLogs(olderThan);
            
            logger.info("清理旧访问日志完成，删除 {} 条记录", deletedCount);

        } catch (Exception e) {
            logger.error("清理旧访问日志失败", e);
        }
    }

    /**
     * 清理内存中的频率限制计数器
     * 每10分钟执行一次
     */
    @Scheduled(fixedRate = 600000) // 10分钟
    public void cleanupRateLimitCounters() {
        logger.debug("开始清理频率限制计数器...");

        try {
            securityMonitorService.cleanupExpiredCounters();
            logger.debug("清理频率限制计数器完成");

        } catch (Exception e) {
            logger.error("清理频率限制计数器失败", e);
        }
    }

    /**
     * 安全监控报告
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void securityMonitoringReport() {
        logger.info("开始生成安全监控报告...");

        try {
            SecurityMonitorService.SecurityStats stats = securityMonitorService.getSecurityStats();
            
            logger.info("安全监控报告 - 异常IP数量: {}, 异常设备数量: {}, 活跃IP计数器: {}, 活跃设备计数器: {}",
                       stats.getAnomalousIpCount(), stats.getAnomalousDeviceCount(),
                       stats.getActiveIpCounters(), stats.getActiveDeviceCounters());

            // 如果发现异常，可以在这里发送告警
            if (stats.getAnomalousIpCount() > 10 || stats.getAnomalousDeviceCount() > 5) {
                logger.warn("检测到大量异常访问，建议检查安全状况");
            }

        } catch (Exception e) {
            logger.error("生成安全监控报告失败", e);
        }
    }

    /**
     * 系统健康检查
     * 每30分钟执行一次
     */
    @Scheduled(fixedRate = 1800000) // 30分钟
    public void systemHealthCheck() {
        logger.debug("开始系统健康检查...");

        try {
            // 检查活跃会话数量
            int activeSessions = sessionMapper.countActiveSessions();
            
            if (activeSessions > 10000) {
                logger.warn("活跃会话数量过多: {}, 可能需要调整会话过期时间", activeSessions);
            }

            logger.debug("系统健康检查完成，活跃会话数量: {}", activeSessions);

        } catch (Exception e) {
            logger.error("系统健康检查失败", e);
        }
    }
}
