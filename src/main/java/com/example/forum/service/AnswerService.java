package com.example.forum.service;


import com.example.forum.common.base.BaseService;
import com.example.forum.entity.Answer;
import com.example.forum.entity.Question;

import java.util.List;

/**
 * <pre>
 *     回答业务逻辑接口
 * </pre>
 *
 * @author : saysky
 * @date : 2017/11/14
 */
public interface AnswerService extends BaseService<Answer, Long> {

    /**
     * 更新记录回复数
     *
     * @param answerId 记录Id
     */
    void resetCommentSize(Long answerId);

}
