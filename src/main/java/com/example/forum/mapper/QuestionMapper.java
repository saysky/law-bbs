package com.example.forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.forum.entity.Question;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 言曌
 * @date 2020/10/31 12:47 下午
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

}
