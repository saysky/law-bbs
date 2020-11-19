package com.example.forum.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.forum.common.base.BaseEntity;
import com.example.forum.util.RelativeDateFormat;
import lombok.Data;

/**
 * 文献
 *
 * @author 言曌
 * @date 2020/10/24 10:14 下午
 */
@Data
@TableName("document")
public class Document extends BaseEntity {

    /**
     * 文献名称
     */
    private String name;

    /**
     * 文献URL
     */
    private String url;

    /**
     * 文件大小
     */
    private String size;

    /**
     * 物理路径
     */
    private String path;

    /**
     * 后缀
     */
    private String suffix;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    @TableField(exist = false)
    private String createTimeStr;

    public String getCreateTimeStr() {
        return RelativeDateFormat.format(getCreateTime());
    }

}
