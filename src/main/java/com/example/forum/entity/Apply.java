package com.example.forum.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.forum.common.base.BaseEntity;
import lombok.Data;

/**
 * <pre>
 *     用户申请
 * </pre>
 */
@Data
@TableName("apply")
public class Apply extends BaseEntity {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 标题
     */
    private String title;

    /**
     * 文件
     */
    private String document;

    /**
     * 状态 0 待审核，1已通过，2拒绝
     */
    private Integer status;

    /**
     * 备注
     */
    private String comment;

    @TableField(exist = false)
    private User user;
}
