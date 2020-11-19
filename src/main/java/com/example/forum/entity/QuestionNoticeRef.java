package com.example.forum.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.forum.common.base.BaseEntity;
import com.example.forum.util.RelativeDateFormat;
import lombok.Data;

/**
 * 问题通知关联
 *
 * @author 言曌
 * @date 2020/10/31 12:01 下午
 */

@Data
@TableName("question_notice_ref")
public class QuestionNoticeRef extends BaseEntity {

    /**
     * 操作者用户ID
     */
    private Long userId;

    /**
     * 通知用户ID
     */
    private Long acceptUserId;

    /**
     * 类型：like点赞问题，mark收藏问题，publish发布问题，answer回答问题
     */
    private String type;

    /**
     * 问题ID
     */
    private Long questionId;


    @TableField(exist = false)
    private Question question;

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private User acceptUser;


    /**
     * 创建时间
     */
    @TableField(exist = false)
    private String createTimeStr;

    public String getCreateTimeStr() {
        return RelativeDateFormat.format(getCreateTime());
    }
}
