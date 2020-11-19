package com.example.forum.service;

/**
 * 删除功能
 * 因为毕竟复杂，单独抽出来
 * @author 言曌
 * @date 2020/11/14 4:42 下午
 */

public interface DeleteService {

    void deleteUser(Long id);

    void deleteQuestion(Long id);

    void deleteAnswer(Long id);

    void deletePost(Long id);

    void deleteRole(Long id);

    void deletePermission(Long id);

    void deleteCategory(Long id);

    void deleteTag(Long id);

    void deleteComment(Long id);
}
