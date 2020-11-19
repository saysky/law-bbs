package com.example.forum.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.forum.common.base.BaseEntity;
import com.example.forum.util.RelativeDateFormat;
import lombok.Data;

/**
 * 点赞问题关联
 * @author 言曌
 * @date 2020/10/31 12:01 下午
 */

@Data
@TableName("question_like_ref")
public class QuestionLikeRef extends BaseEntity {

    /**
     * 点赞人ID
     */
    private Long userId;

    /**
     * 问题ID
     */
    private Long questionId;

    @TableField(exist = false)
    private Question question;

    @TableField(exist = false)
    private User user;


    /**
     * 创建时间
     */
    @TableField(exist = false)
    private String createTimeStr;

    public String getCreateTimeStr() {
        return RelativeDateFormat.format(getCreateTime());
    }
}
