package com.example.forum.enums;

/**
 * 申请状态enum
 */
public enum ApplyStatusEnum {

    /**
     * 待审核
     */
    NOT_CHECKED(0),

    /**
     * 审核通过
     */
    CHECKED_SUCCESS(1),

    /**
     * 审核不通过
     */
    CHECKED_FAILURE(1)
    ;


    private Integer code;

    ApplyStatusEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
