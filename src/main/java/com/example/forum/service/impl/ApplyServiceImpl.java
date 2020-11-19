package com.example.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.forum.entity.Apply;
import com.example.forum.mapper.ApplyMapper;
import com.example.forum.service.ApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 *     申请业务逻辑实现类
 * </pre>
 */
@Service
public class ApplyServiceImpl implements ApplyService {


    @Autowired
    private ApplyMapper applyMapper;

    @Override
    public BaseMapper<Apply> getRepository() {
        return applyMapper;
    }

    @Override
    public QueryWrapper<Apply> getQueryWrapper(Apply apply) {
        //对指定字段查询
        QueryWrapper<Apply> queryWrapper = new QueryWrapper<>();
        if (apply != null) {
            if (apply.getUserId() != null && apply.getUserId() != -1) {
                queryWrapper.eq("user_id", apply.getUserId());
            }
            if (apply.getStatus() != null && apply.getStatus() != -1) {
                queryWrapper.eq("status", apply.getStatus());
            }
        }
        return queryWrapper;
    }

    @Override
    public Apply insertOrUpdate(Apply entity) {
        if (entity.getId() == null) {
            entity.setCreateTime(new Date());
            entity.setUpdateTime(new Date());
            insert(entity);
        } else {
            entity.setUpdateTime(new Date());
            update(entity);
        }
        return entity;
    }

    @Override
    public void delete(Long id) {
        applyMapper.deleteById(id);
    }

    @Override
    public List<Apply> findAll() {
        List<Apply> applyList = applyMapper.selectList(null);
        return applyList;
    }

    @Override
    public List<Apply> findByUserIdAndStatus(Long userId, Integer status) {
        Map<String, Object> map = new HashMap<>();
        if (userId != null && userId != -1) {
            map.put("user_id", userId);
        }
        if (status != null && status != -1) {
            map.put("status", status);
        }
        return applyMapper.selectByMap(map);
    }
}
