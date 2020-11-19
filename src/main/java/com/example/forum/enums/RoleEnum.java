package com.example.forum.enums;

/**
 * 角色
 */
public enum RoleEnum {

    /**
     * 待审核
     */
    ADMIN("admin"),

    /**
     * 审核通过
     */
    EXPERT("expert"),

    /**
     * 用户
     */
    USER("user")
    ;


    private String code;

    RoleEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
