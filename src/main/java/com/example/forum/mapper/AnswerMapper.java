package com.example.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.forum.entity.Answer;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 言曌
 * @date 2020/10/31 12:47 下午
 */
@Mapper
public interface AnswerMapper extends BaseMapper<Answer> {

    /**
     * 更新记录回复数
     *
     * @param answerId 记录Id
     */
    void resetCommentSize(Long answerId);

}
