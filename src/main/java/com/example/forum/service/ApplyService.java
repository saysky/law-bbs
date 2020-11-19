package com.example.forum.service;


import com.example.forum.common.base.BaseService;
import com.example.forum.entity.Apply;

import java.util.List;

/**
 * <pre>
 *    申请业务逻辑接口
 * </pre>
 *
 * @author : saysky
 * @date : 2017/11/14
 */
public interface ApplyService extends BaseService<Apply, Long> {


    /**
     * 根据用户ID和状态查询
     * @param userId
     * @param status
     * @return
     */
    List<Apply> findByUserIdAndStatus(Long userId, Integer status);
}
