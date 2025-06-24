package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.VerifySession;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface VerifySessionMapper {

    /**
     * 插入验证会话
     */
    int insert(VerifySession session);

    /**
     * 根据会话ID查询
     */
    VerifySession selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据访问令牌查询
     */
    VerifySession selectByAccessToken(@Param("accessToken") String accessToken);

    /**
     * 根据ID查询
     */
    VerifySession selectById(@Param("id") String id);

    /**
     * 更新会话状态
     */
    int updateStatus(@Param("sessionId") String sessionId, @Param("status") String status);

    /**
     * 更新访问信息
     */
    int updateAccessInfo(@Param("sessionId") String sessionId, 
                        @Param("accessCount") Integer accessCount, 
                        @Param("lastAccessAt") LocalDateTime lastAccessAt);

    /**
     * 根据资源ID查询活跃会话
     */
    List<VerifySession> selectActiveByResourceId(@Param("resourceId") String resourceId);

    /**
     * 根据设备指纹查询活跃会话
     */
    List<VerifySession> selectActiveByDeviceFingerprint(@Param("deviceFingerprint") String deviceFingerprint);

    /**
     * 删除过期的会话
     */
    int deleteExpired(@Param("expiredBefore") LocalDateTime expiredBefore);

    /**
     * 撤销会话
     */
    int revokeSession(@Param("sessionId") String sessionId);

    /**
     * 批量撤销会话
     */
    int revokeSessions(@Param("sessionIds") List<String> sessionIds);

    /**
     * 检查会话是否存在
     */
    boolean existsBySessionId(@Param("sessionId") String sessionId);

    /**
     * 统计活跃会话数量
     */
    int countActiveSessions();
}
