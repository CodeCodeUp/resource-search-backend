package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.VerifyAccessLog;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface VerifyAccessLogMapper {

    /**
     * 插入访问日志
     */
    int insert(VerifyAccessLog log);

    /**
     * 根据ID查询
     */
    VerifyAccessLog selectById(@Param("id") Long id);

    /**
     * 根据会话ID查询日志
     */
    List<VerifyAccessLog> selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据资源ID查询日志
     */
    List<VerifyAccessLog> selectByResourceId(@Param("resourceId") String resourceId, 
                                           @Param("limit") Integer limit);

    /**
     * 根据IP地址查询最近的日志
     */
    List<VerifyAccessLog> selectRecentByIpAddress(@Param("ipAddress") String ipAddress, 
                                                 @Param("minutes") int minutes);

    /**
     * 根据设备指纹查询最近的日志
     */
    List<VerifyAccessLog> selectRecentByDeviceFingerprint(@Param("deviceFingerprint") String deviceFingerprint, 
                                                         @Param("minutes") int minutes);

    /**
     * 统计IP地址的访问次数
     */
    int countByIpAddress(@Param("ipAddress") String ipAddress, 
                        @Param("minutes") int minutes);

    /**
     * 统计设备指纹的访问次数
     */
    int countByDeviceFingerprint(@Param("deviceFingerprint") String deviceFingerprint, 
                                @Param("minutes") int minutes);

    /**
     * 统计失败的访问次数（按IP）
     */
    int countFailuresByIpAddress(@Param("ipAddress") String ipAddress, 
                                @Param("minutes") int minutes);

    /**
     * 统计失败的访问次数（按设备指纹）
     */
    int countFailuresByDeviceFingerprint(@Param("deviceFingerprint") String deviceFingerprint, 
                                        @Param("minutes") int minutes);

    /**
     * 删除过期的日志
     */
    int deleteOldLogs(@Param("olderThan") LocalDateTime olderThan);

    /**
     * 获取异常IP列表
     */
    List<String> selectAnomalousIpAddresses(@Param("minAttempts") int minAttempts, 
                                           @Param("maxSuccessRate") double maxSuccessRate, 
                                           @Param("minutes") int minutes);

    /**
     * 获取异常设备指纹列表
     */
    List<String> selectAnomalousDeviceFingerprints(@Param("minAttempts") int minAttempts, 
                                                  @Param("maxSuccessRate") double maxSuccessRate, 
                                                  @Param("minutes") int minutes);
}
