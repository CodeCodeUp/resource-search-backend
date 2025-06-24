package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.*;
import org.example.entity.VerifyAccessLog;
import org.example.entity.VerifyChallenge;
import org.example.entity.VerifySession;
import org.example.mapper.VerifyAccessLogMapper;
import org.example.mapper.VerifyChallengeMapper;
import org.example.mapper.VerifySessionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

/**
 * 滑动验证服务
 */
@Service
public class SlideVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(SlideVerificationService.class);

    private static final String SECRET_KEY = "client_verify"; // 应该从配置文件读取
    private static final int DEFAULT_TOLERANCE = 5;
    private static final int CHALLENGE_EXPIRE_MINUTES = 5;
    private static final int SESSION_EXPIRE_HOURS = 1;

    // 拼图位置安全范围常量
    private static final int PUZZLE_X_MIN = 15;
    private static final int PUZZLE_X_MAX = 243;
    private static final int PUZZLE_Y_MIN = 15;
    private static final int PUZZLE_Y_MAX = 93;

    @Autowired
    private VerifyChallengeMapper challengeMapper;

    @Autowired
    private VerifySessionMapper sessionMapper;

    @Autowired
    private VerifyAccessLogMapper accessLogMapper;

    @Autowired
    private SecurityMonitorService securityMonitorService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 生成验证挑战
     */
    @Transactional
    public VerifyChallengeResponse generateChallenge(VerifyChallengeRequest request, String ipAddress) {
        logger.info("生成验证挑战，设备指纹: {}, IP: {}", request.getDeviceFingerprint(), ipAddress);

        // 检查频率限制
        if (!securityMonitorService.checkRateLimit(ipAddress, request.getDeviceFingerprint())) {
            throw new RuntimeException("请求过于频繁，请稍后再试");
        }

        // 生成挑战ID
        String challengeId = generateChallengeId();

        // 生成拼图位置
        VerifyChallengeResponse.PuzzlePosition puzzlePosition = generatePuzzlePosition();

        // 验证拼图位置是否在安全范围内
        if (!validatePuzzlePosition(puzzlePosition)) {
            logger.error("生成的拼图位置不在安全范围内，重新生成");
            puzzlePosition = generatePuzzlePosition(); // 重新生成一次
            if (!validatePuzzlePosition(puzzlePosition)) {
                throw new RuntimeException("无法生成安全范围内的拼图位置");
            }
        }

        // 生成背景图片（这里简化为返回固定的base64字符串）
        String backgroundImage = generateBackgroundImage();

        // 计算过期时间
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(CHALLENGE_EXPIRE_MINUTES);
        long expiresAtTimestamp = expiresAt.toEpochSecond(ZoneOffset.UTC) * 1000;

        // 生成服务器签名
        String serverSignature = generateServerSignature(challengeId, puzzlePosition, expiresAtTimestamp);

        // 保存挑战到数据库
        try {
            String puzzlePositionJson = objectMapper.writeValueAsString(puzzlePosition);
            VerifyChallenge challenge = new VerifyChallenge(
                challengeId, backgroundImage, puzzlePositionJson, DEFAULT_TOLERANCE,
                request.getDeviceFingerprint(), request.getUserAgent(), ipAddress
            );
            challenge.setExpiresAt(expiresAt);

            challengeMapper.insert(challenge);
            logger.info("验证挑战已保存，挑战ID: {}", challengeId);

        } catch (JsonProcessingException e) {
            logger.error("序列化拼图位置失败", e);
            throw new RuntimeException("生成挑战失败");
        }

        return new VerifyChallengeResponse(
            challengeId, backgroundImage, puzzlePosition, 
            DEFAULT_TOLERANCE, expiresAtTimestamp, serverSignature
        );
    }

    /**
     * 验证滑动结果
     */
    @Transactional
    public VerifySlideResponse verifySlide(VerifySlideRequest request, String ipAddress) {
        logger.info("验证滑动结果，挑战ID: {}, 滑动位置: {}", request.getChallengeId(), request.getSlidePosition());

        try {
            // 查询挑战
            VerifyChallenge challenge = challengeMapper.selectByChallengeId(request.getChallengeId());
            if (challenge == null) {
                logAccessAttempt(null, request.getResourceId(), "verify", ipAddress, 
                               request.getUserAgent(), request.getDeviceFingerprint(), false, "挑战不存在");
                return new VerifySlideResponse(false, null, null, null);
            }

            // 验证挑战状态
            if (challenge.isExpired()) {
                logAccessAttempt(null, request.getResourceId(), "verify", ipAddress, 
                               request.getUserAgent(), request.getDeviceFingerprint(), false, "挑战已过期");
                return new VerifySlideResponse(false, null, null, null);
            }

            if (challenge.isUsed()) {
                logAccessAttempt(null, request.getResourceId(), "verify", ipAddress, 
                               request.getUserAgent(), request.getDeviceFingerprint(), false, "挑战已使用");
                return new VerifySlideResponse(false, null, null, null);
            }

            // 验证设备指纹
            if (!challenge.getDeviceFingerprint().equals(request.getDeviceFingerprint())) {
                logAccessAttempt(null, request.getResourceId(), "verify", ipAddress, 
                               request.getUserAgent(), request.getDeviceFingerprint(), false, "设备指纹不匹配");
                return new VerifySlideResponse(false, null, null, null);
            }

            // 验证滑动位置
            boolean positionValid = validateSlidePosition(challenge, request.getSlidePosition());
            if (!positionValid) {
                // 标记挑战为失败
                challengeMapper.updateStatus(request.getChallengeId(), "failed", LocalDateTime.now());
                logAccessAttempt(null, request.getResourceId(), "verify", ipAddress, 
                               request.getUserAgent(), request.getDeviceFingerprint(), false, "滑动位置不正确");
                return new VerifySlideResponse(false, null, null, null);
            }

            // 验证滑动时间（防止机器人）
            if (!validateSlideTime(request.getSlideTime())) {
                challengeMapper.updateStatus(request.getChallengeId(), "failed", LocalDateTime.now());
                logAccessAttempt(null, request.getResourceId(), "verify", ipAddress, 
                               request.getUserAgent(), request.getDeviceFingerprint(), false, "滑动时间异常");
                return new VerifySlideResponse(false, null, null, null);
            }

            // 验证成功，标记挑战为已验证
            challengeMapper.updateStatus(request.getChallengeId(), "verified", LocalDateTime.now());

            // 创建验证会话
            String sessionId = generateSessionId();
            String accessToken = generateAccessToken(sessionId, request.getResourceId());
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(SESSION_EXPIRE_HOURS);

            VerifySession session = new VerifySession(
                sessionId, request.getChallengeId(), request.getResourceId(),
                accessToken, request.getDeviceFingerprint(), request.getUserAgent(), ipAddress
            );
            session.setExpiresAt(expiresAt);

            sessionMapper.insert(session);

            // 记录成功的访问日志
            logAccessAttempt(sessionId, request.getResourceId(), "verify", ipAddress, 
                           request.getUserAgent(), request.getDeviceFingerprint(), true, null);

            logger.info("滑动验证成功，会话ID: {}", sessionId);

            return new VerifySlideResponse(
                true, accessToken, expiresAt.toEpochSecond(ZoneOffset.UTC) * 1000, sessionId
            );

        } catch (Exception e) {
            logger.error("验证滑动结果失败", e);
            logAccessAttempt(null, request.getResourceId(), "verify", ipAddress, 
                           request.getUserAgent(), request.getDeviceFingerprint(), false, e.getMessage());
            return new VerifySlideResponse(false, null, null, null);
        }
    }

    /**
     * 从访问令牌获取资源ID
     */
    public String getResourceIdFromToken(String token) {
        try {
            // 查询会话
            VerifySession session = sessionMapper.selectByAccessToken(token);
            if (session != null && session.isActive()) {
                return session.getResourceId();
            }
            return null;
        } catch (Exception e) {
            logger.error("从令牌获取资源ID失败", e);
            return null;
        }
    }

    /**
     * 验证访问令牌
     */
    public boolean validateAccessToken(String token, String resourceId, String action, String ipAddress,
                                     String userAgent, String deviceFingerprint) {
        logger.info("验证访问令牌，资源ID: {}, 操作: {}", resourceId, action);

        try {
            // 查询会话
            VerifySession session = sessionMapper.selectByAccessToken(token);
            if (session == null) {
                logAccessAttempt(null, resourceId, action, ipAddress, userAgent, deviceFingerprint, false, "令牌不存在");
                return false;
            }

            // 检查会话状态
            if (!session.isActive()) {
                logAccessAttempt(session.getSessionId(), resourceId, action, ipAddress, userAgent, deviceFingerprint, false, "会话已过期或被撤销");
                return false;
            }

            // 检查资源ID
            if (!session.getResourceId().equals(resourceId)) {
                logAccessAttempt(session.getSessionId(), resourceId, action, ipAddress, userAgent, deviceFingerprint, false, "资源ID不匹配");
                return false;
            }

            // 更新访问信息
            session.incrementAccessCount();
            sessionMapper.updateAccessInfo(session.getSessionId(), session.getAccessCount(), session.getLastAccessAt());

            // 记录成功的访问日志
            logAccessAttempt(session.getSessionId(), resourceId, action, ipAddress, userAgent, deviceFingerprint, true, null);

            logger.info("访问令牌验证成功，会话ID: {}", session.getSessionId());
            return true;

        } catch (Exception e) {
            logger.error("验证访问令牌失败", e);
            logAccessAttempt(null, resourceId, action, ipAddress, userAgent, deviceFingerprint, false, e.getMessage());
            return false;
        }
    }

    /**
     * 生成挑战ID
     */
    private String generateChallengeId() {
        long timestamp = System.currentTimeMillis();
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return String.format("challenge_%d_%s", timestamp, randomPart);
    }

    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return "session_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成拼图位置
     * x坐标范围：15 <= x <= 243
     * y坐标范围：15 <= y <= 93
     */
    private VerifyChallengeResponse.PuzzlePosition generatePuzzlePosition() {
        // 生成安全范围内的随机位置
        int x = PUZZLE_X_MIN + secureRandom.nextInt(PUZZLE_X_MAX - PUZZLE_X_MIN + 1);
        int y = PUZZLE_Y_MIN + secureRandom.nextInt(PUZZLE_Y_MAX - PUZZLE_Y_MIN + 1);

        logger.debug("生成拼图位置: x={}, y={}", x, y);
        return new VerifyChallengeResponse.PuzzlePosition(x, y);
    }

    /**
     * 生成背景图片（简化实现）
     */
    private String generateBackgroundImage() {
        // 这里应该生成真实的验证码图片，简化为返回固定字符串
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
    }

    /**
     * 生成服务器签名
     */
    private String generateServerSignature(String challengeId, VerifyChallengeResponse.PuzzlePosition position, long expiresAt) {
        try {
            String data = challengeId + position.getX() + position.getY() + expiresAt;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature);
        } catch (Exception e) {
            logger.error("生成服务器签名失败", e);
            return "";
        }
    }

    /**
     * 生成访问令牌
     */
    private String generateAccessToken(String sessionId, String resourceId) {
        try {
            long timestamp = System.currentTimeMillis();
            String data = sessionId + resourceId + timestamp;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] token = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(token);
        } catch (Exception e) {
            logger.error("生成访问令牌失败", e);
            return UUID.randomUUID().toString();
        }
    }

    /**
     * 验证滑动位置
     */
    private boolean validateSlidePosition(VerifyChallenge challenge, Double slidePosition) {
        try {
            VerifyChallengeResponse.PuzzlePosition position = objectMapper.readValue(
                challenge.getPuzzlePosition(), VerifyChallengeResponse.PuzzlePosition.class);
            
            double tolerance = challenge.getTolerance();
            double correctPosition = position.getX();
            
            return Math.abs(slidePosition - correctPosition) <= tolerance;
        } catch (JsonProcessingException e) {
            logger.error("解析拼图位置失败", e);
            return false;
        }
    }

    /**
     * 验证滑动时间
     */
    private boolean validateSlideTime(Long slideTime) {
        // 滑动时间应该在合理范围内（200ms - 10s）
        return slideTime != null && slideTime >= 200 && slideTime <= 10000;
    }

    /**
     * 验证拼图位置是否在安全范围内
     */
    private boolean validatePuzzlePosition(VerifyChallengeResponse.PuzzlePosition position) {
        if (position == null) {
            return false;
        }

        int x = position.getX();
        int y = position.getY();

        boolean xValid = x >= PUZZLE_X_MIN && x <= PUZZLE_X_MAX;
        boolean yValid = y >= PUZZLE_Y_MIN && y <= PUZZLE_Y_MAX;

        if (!xValid || !yValid) {
            logger.warn("拼图位置超出安全范围: x={}, y={}, x范围:[{}-{}], y范围:[{}-{}]",
                       x, y, PUZZLE_X_MIN, PUZZLE_X_MAX, PUZZLE_Y_MIN, PUZZLE_Y_MAX);
        }

        return xValid && yValid;
    }

    /**
     * 记录访问日志
     */
    private void logAccessAttempt(String sessionId, String resourceId, String action, String ipAddress, 
                                 String userAgent, String deviceFingerprint, boolean success, String errorMessage) {
        try {
            VerifyAccessLog log = new VerifyAccessLog(
                sessionId, resourceId, action, ipAddress, userAgent, deviceFingerprint, success, errorMessage
            );
            accessLogMapper.insert(log);
        } catch (Exception e) {
            logger.error("记录访问日志失败", e);
        }
    }
}
