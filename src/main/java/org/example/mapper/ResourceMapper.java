package org.example.mapper;

import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.Resource;

import java.util.List;

@Mapper
public interface ResourceMapper {

    /**
     * 根据ID查询资源
     */
    Resource selectById(@Param("id") Integer id);

    /**
     * 查询所有资源
     */
    List<Resource> selectAll();

    /**
     * 根据名称模糊查询资源
     */
    List<Resource> selectByNameLike(@Param("name") String name);

    /**
     * 根据内容模糊查询资源
     */
    List<Resource> selectByContentLike(@Param("content") String content);

    /**
     * 根据层级查询资源
     */
    List<Resource> selectByLevel(@Param("level") Integer level);

    /**
     * 根据类型查询资源
     */
    List<Resource> selectByType(@Param("type") String type);

    /**
     * 根据名称或内容模糊查询资源
     */
    List<Resource> selectByNameOrContentLike(@Param("name") String name, @Param("content") String content);

    /**
     * 高级搜索：在名称和内容中搜索关键词，支持部分匹配
     * 优先级：完全匹配 > 名称包含 > 内容包含
     */
    List<Resource> selectByAdvancedSearch(@Param("searchTerm") String searchTerm);

    /**
     * 高级搜索带分页
     */
    Page<Resource> selectByAdvancedSearchWithPagination(@Param("searchTerm") String searchTerm);

    /**
     * 根据多个关键词搜索（用于分词搜索）
     */
    List<Resource> selectByMultipleTerms(@Param("term1") String term1, 
                                        @Param("term2") String term2, 
                                        @Param("term3") String term3);

    /**
     * 根据层级和搜索词查询
     */
    List<Resource> selectByLevelAndSearchTerm(@Param("level") Integer level, @Param("searchTerm") String searchTerm);

    /**
     * 仅在名称字段中进行高级搜索
     */
    List<Resource> selectByNameOnlyAdvancedSearch(@Param("searchTerm") String searchTerm);

    /**
     * 仅在名称字段中根据多个关键词搜索（用于分词搜索）
     */
    List<Resource> selectByNameOnlyMultipleTerms(@Param("term1") String term1,
                                                @Param("term2") String term2,
                                                @Param("term3") String term3);

    /**
     * 插入资源
     */
    int insert(Resource resource);

    /**
     * 更新资源
     */
    int updateById(Resource resource);

    /**
     * 删除资源
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 检查资源是否存在
     */
    boolean existsById(@Param("id") Integer id);

    /**
     * 根据层级查询资源数量
     */
    Long countByLevel(@Param("level") Integer level);

    /**
     * 根据URL检查资源是否存在
     */
    boolean existsByUrl(@Param("url") String url);

    /**
     * 根据URL检查资源是否存在
     */
    boolean existsByTime(@Param("time") int time);

    /**
     * 统一搜索接口：支持搜索词、层级和类型过滤（数据库层面实现）
     * 支持多字段搜索和仅名称搜索
     */
    List<Resource> selectByUnifiedSearch(@Param("searchTerm") String searchTerm,
                                        @Param("level") Integer level,
                                        @Param("type") String type,
                                        @Param("searchMode") String searchMode);

    /**
     * 统一搜索接口：支持多个关键词、层级和类型过滤（数据库层面实现）
     * 用于分词搜索
     */
    List<Resource> selectByUnifiedMultipleTermsSearch(@Param("term1") String term1,
                                                     @Param("term2") String term2,
                                                     @Param("term3") String term3,
                                                     @Param("level") Integer level,
                                                     @Param("type") String type,
                                                     @Param("searchMode") String searchMode);

    /**
     * 根据source删除资源
     */
    int deleteBySource(@Param("source") Integer source);

    /**
     * 批量插入资源
     */
    int insertBatch(@Param("resources") List<Resource> resources);

    /**
     * 根据ID列表批量查询资源
     */
    List<Resource> selectByIds(@Param("ids") List<Integer> ids);
}
