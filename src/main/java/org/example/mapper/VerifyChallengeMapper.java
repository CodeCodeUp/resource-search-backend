package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.VerifyChallenge;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface VerifyChallengeMapper {

    /**
     * 插入验证挑战
     */
    int insert(VerifyChallenge challenge);

    /**
     * 根据挑战ID查询
     */
    VerifyChallenge selectByChallengeId(@Param("challengeId") String challengeId);

    /**
     * 根据ID查询
     */
    VerifyChallenge selectById(@Param("id") String id);

    /**
     * 更新挑战状态
     */
    int updateStatus(@Param("challengeId") String challengeId, 
                    @Param("status") String status, 
                    @Param("usedAt") LocalDateTime usedAt);

    /**
     * 根据设备指纹查询最近的挑战
     */
    List<VerifyChallenge> selectRecentByDeviceFingerprint(@Param("deviceFingerprint") String deviceFingerprint, 
                                                         @Param("minutes") int minutes);

    /**
     * 根据IP地址查询最近的挑战
     */
    List<VerifyChallenge> selectRecentByIpAddress(@Param("ipAddress") String ipAddress, 
                                                 @Param("minutes") int minutes);

    /**
     * 删除过期的挑战
     */
    int deleteExpired(@Param("expiredBefore") LocalDateTime expiredBefore);

    /**
     * 统计设备指纹的失败次数
     */
    int countFailuresByDeviceFingerprint(@Param("deviceFingerprint") String deviceFingerprint, 
                                        @Param("minutes") int minutes);

    /**
     * 统计IP地址的失败次数
     */
    int countFailuresByIpAddress(@Param("ipAddress") String ipAddress, 
                                @Param("minutes") int minutes);

    /**
     * 检查挑战是否存在
     */
    boolean existsByChallengeId(@Param("challengeId") String challengeId);
}
