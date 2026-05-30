package com.freedoc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.freedoc.entity.Doc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DocMapper extends BaseMapper<Doc> {

    @Select("<script>" +
            "SELECT * FROM doc WHERE MATCH(doc_title, doc_content) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) " +
            "<if test='projectId != null and projectId != \"\"'>" +
            "AND project_id = #{projectId} " +
            "</if>" +
            "ORDER BY update_time DESC" +
            "</script>")
    List<Doc> searchByKeyword(@Param("keyword") String keyword, @Param("projectId") String projectId);

}
