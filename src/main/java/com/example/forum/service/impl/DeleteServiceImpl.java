package com.example.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.forum.entity.*;
import com.example.forum.enums.CommentTypeEnum;
import com.example.forum.enums.PostTypeEnum;
import com.example.forum.mapper.*;
import com.example.forum.service.DeleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 言曌
 * @date 2020/11/14 4:45 下午
 */
@Service
public class DeleteServiceImpl implements DeleteService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private UserRoleRefMapper userRoleRefMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionMarkRefMapper questionMarkRefMapper;

    @Autowired
    private QuestionLikeRefMapper questionLikeRefMapper;

    @Autowired
    private QuestionNoticeRefMapper questionNoticeRefMapper;

    @Autowired
    private AnswerMapper answerMapper;

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private PostCategoryRefMapper postCategoryRefMapper;

    @Autowired
    private PostTagRefMapper postTagRefMapper;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private ApplyMapper applyMapper;

    @Autowired
    private RolePermissionRefMapper rolePermissionRefMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {

        // 1.删除文章
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.eq("user_id", id);
        List<Post> postList = postMapper.selectList(postQueryWrapper);
        for (Post post : postList) {
            this.deletePost(post.getId());
        }

        // 2.删除问题
        QueryWrapper<Question> questionQueryWrapper = new QueryWrapper<>();
        questionQueryWrapper.eq("user_id", id);
        List<Question> questionList = questionMapper.selectList(questionQueryWrapper);
        for (Question question : questionList) {
            this.deleteQuestion(question.getId());
        }

        // 3.删除用户
        userMapper.deleteById(id);

        // 4.删除角色关联
        QueryWrapper<UserRoleRef> userRoleRefQueryWrapper = new QueryWrapper<>();
        userRoleRefQueryWrapper.eq("user_id", id);
        userRoleRefMapper.delete(userRoleRefQueryWrapper);

        // 5.删除关注
        QueryWrapper<Follow> followQueryWrapper = new QueryWrapper<>();
        followQueryWrapper.eq("user_id", id).or().eq("accept_user_id", id);
        followMapper.delete(followQueryWrapper);

        // 6.删除申请
        QueryWrapper<Apply> applyQueryWrapper = new QueryWrapper<>();
        applyQueryWrapper.eq("user_id", id);
        applyMapper.delete(applyQueryWrapper);

        // 7.删除文档
        QueryWrapper<Document> documentQueryWrapper = new QueryWrapper<>();
        documentQueryWrapper.eq("user_id", id);
        documentMapper.delete(documentQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(Long id) {
        // 1.删除回答
        QueryWrapper<Answer> answerQueryWrapper = new QueryWrapper<>();
        answerQueryWrapper.eq("question_id", id);
        List<Answer> answers = answerMapper.selectList(answerQueryWrapper);
        for (Answer answer : answers) {
            this.deleteAnswer(answer.getId());
        }

        // 2.删除问题
        questionMapper.deleteById(id);

        // 3.删除问题通知
        QueryWrapper<QuestionNoticeRef> questionNoticeRefQueryWrapper = new QueryWrapper<>();
        questionNoticeRefQueryWrapper.eq("question_id", id);
        questionNoticeRefMapper.delete(questionNoticeRefQueryWrapper);

        // 4.删除问题点赞
        QueryWrapper<QuestionLikeRef> questionLikeRefQueryWrapper = new QueryWrapper<>();
        questionLikeRefQueryWrapper.eq("question_id", id);
        questionLikeRefMapper.delete(questionLikeRefQueryWrapper);

        // 5.删除问题收藏
        QueryWrapper<QuestionMarkRef> questionMarkRefQueryWrapper = new QueryWrapper<>();
        questionMarkRefQueryWrapper.eq("question_id", id);
        questionMarkRefMapper.delete(questionMarkRefQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnswer(Long id) {
        Answer answer = answerMapper.selectById(id);
        if (answer != null) {
            Question question = questionMapper.selectById(answer.getQuestionId());
            // 1.删除回答
            answerMapper.deleteById(id);

            // 2.修改问题的回答数
            if (question != null) {
                QueryWrapper<Answer> answerQueryWrapper = new QueryWrapper<>();
                answerQueryWrapper.eq("question_id", question.getId());
                List<Answer> answerList = answerMapper.selectList(answerQueryWrapper);
                question.setAnswerCount(answerList.size());
                questionMapper.updateById(question);
            }

            // 3.删除评论
            QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
            commentQueryWrapper.eq("business_id", id);
            commentQueryWrapper.eq("type", CommentTypeEnum.ANSWER.getCode());
            commentMapper.delete(commentQueryWrapper);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long id) {
        // 1. 删除文章
        postMapper.deleteById(id);

        // 2. 删除文章标签关联
        QueryWrapper<PostTagRef> postTagRefQueryWrapper = new QueryWrapper<>();
        postTagRefQueryWrapper.eq("post_id", id);
        postTagRefMapper.delete(postTagRefQueryWrapper);

        // 3. 删除文章分类关联
        QueryWrapper<PostCategoryRef> postCategoryRefQueryWrapper = new QueryWrapper<>();
        postCategoryRefQueryWrapper.eq("post_id", id);
        postCategoryRefMapper.delete(postCategoryRefQueryWrapper);

        // 4.删除评论
        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.eq("business_id", id);
        commentQueryWrapper.eq("type", CommentTypeEnum.POST.getCode());
        commentMapper.delete(commentQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        // 1.删除用户关联
        QueryWrapper<UserRoleRef> userRoleRefQueryWrapper = new QueryWrapper<>();
        userRoleRefQueryWrapper.eq("role_id", id);
        userRoleRefMapper.delete(userRoleRefQueryWrapper);

        // 2.删除权限关联
        QueryWrapper<RolePermissionRef> rolePermissionRefQueryWrapper = new QueryWrapper<>();
        rolePermissionRefQueryWrapper.eq("role_id", id);
        rolePermissionRefMapper.delete(rolePermissionRefQueryWrapper);

        // 3.删除角色
        roleMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long id) {
        // 1.删除关联
        QueryWrapper<RolePermissionRef> rolePermissionRefQueryWrapper = new QueryWrapper<>();
        rolePermissionRefQueryWrapper.eq("permission_id", id);
        rolePermissionRefMapper.delete(rolePermissionRefQueryWrapper);

        // 2.删除权限
        permissionMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        // 1.删除分类
        categoryMapper.deleteById(id);

        // 2.删除关联
        QueryWrapper<PostCategoryRef> postTagRefQueryWrapper = new QueryWrapper<>();
        postTagRefQueryWrapper.eq("cate_id", id);
        postCategoryRefMapper.delete(postTagRefQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        // 1.删除标签
        tagMapper.deleteById(id);

        // 2.删除关联
        QueryWrapper<PostTagRef> postTagRefQueryWrapper = new QueryWrapper<>();
        postTagRefQueryWrapper.eq("tag_id", id);
        postTagRefMapper.delete(postTagRefQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment != null) {
            // 1.删除评论
            QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
            commentQueryWrapper.eq("comment_parent", id);
            List<Comment> childList = commentMapper.selectList(commentQueryWrapper);
            if (childList != null && childList.size() > 0) {
                for (Comment c : childList) {
                    this.deleteComment(c.getId());
                }
            }

            // 2.删除评论
            commentMapper.deleteById(id);

            // 3.更新所属文章/回答评论数
            if (CommentTypeEnum.POST.getCode().equals(comment.getType())) {
                Post post = postMapper.selectById(comment.getBusinessId());
                if (post != null) {
                    QueryWrapper<Comment> commentQueryWrapper2 = new QueryWrapper<>();
                    commentQueryWrapper2.eq("business_id", post.getId());
                    commentQueryWrapper2.eq("type", CommentTypeEnum.POST.getCode());
                    List<Comment> commentList = commentMapper.selectList(commentQueryWrapper2);
                    post.setCommentSize(commentList.size());
                    postMapper.updateById(post);
                }
            } else if (CommentTypeEnum.ANSWER.getCode().equals(comment.getType())) {
                Answer answer = answerMapper.selectById(comment.getBusinessId());
                if (answer != null) {
                    QueryWrapper<Comment> commentQueryWrapper2 = new QueryWrapper<>();
                    commentQueryWrapper2.eq("business_id", answer.getId());
                    commentQueryWrapper2.eq("type", CommentTypeEnum.ANSWER.getCode());
                    List<Comment> commentList = commentMapper.selectList(commentQueryWrapper2);
                    answer.setCommentCount(commentList.size());
                    answerMapper.updateById(answer);
                }
            }
        }
    }
}

