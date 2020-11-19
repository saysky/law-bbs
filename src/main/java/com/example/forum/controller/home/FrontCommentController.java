package com.example.forum.controller.home;

import cn.hutool.http.HtmlUtil;
import com.example.forum.controller.common.BaseController;
import com.example.forum.entity.*;
import com.example.forum.dto.JsonResult;
import com.example.forum.enums.CommentTypeEnum;
import com.example.forum.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author 言曌
 * @date 2020/3/11 4:59 下午
 */
@Controller
public class FrontCommentController extends BaseController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Autowired
    private AnswerService answerService;

    @Autowired
    private UserService userService;

    @Autowired
    private BlackWordService blackWordService;

    /**
     * 发布文章评论
     *
     * @param postId         文章ID
     * @param commentId      上级回复ID
     * @param commentContent 回复的内容
     * @return 重定向到/admin/comment
     */
    @PostMapping(value = "/comment/post")
    @ResponseBody
    public JsonResult newPostComment(@RequestParam(value = "postId") Long postId,
                                     @RequestParam(value = "commentId", required = false) Long commentId,
                                     @RequestParam("commentContent") String commentContent) {


        // 判断是否登录
        User loginUser = getLoginUser();
        if (loginUser == null) {
            return JsonResult.error("请先登录");
        }

        // 判断文章是否存在
        Post post = postService.get(postId);
        if (post == null) {
            return JsonResult.error("文章不存在");
        }

        // 判断评论内容是否包含屏蔽字
        List<BlackWord> blackWordList = blackWordService.findAll();
        for (BlackWord blackWord : blackWordList) {
            if (commentContent.contains(blackWord.getContent())) {
                return JsonResult.error("评论内容包含违规字符");
            }
        }

        // 如果是回复
        Comment comment = new Comment();
        if (commentId != null) {
            //回复回复
            Comment parentComment = commentService.get(commentId);
            if (parentComment == null || !Objects.equals(parentComment.getBusinessId(), postId)) {
                return JsonResult.error("回复不存在");
            }
            User parentUser = userService.get(parentComment.getUserId());
            if (parentUser != null) {
                String lastContent = "<a href='#comment" + parentComment.getId() + "'>@" + parentUser.getUserDisplayName() + "</a> ";
                comment.setCommentContent(lastContent + parentUser.getUserDisplayName() + ": " + HtmlUtil.escape(commentContent));
                comment.setCommentParent(parentComment.getId());
                comment.setAcceptUserId(parentComment.getUserId());
                comment.setPathTrace(parentComment.getPathTrace() + parentComment.getId() + "/");
            }
        } else {
            // 回复文章
            comment.setCommentContent(HtmlUtil.escape(commentContent));
            comment.setCommentParent(0L);
            comment.setAcceptUserId(post.getUserId());
            comment.setPathTrace("/");
        }
        comment.setUserId(loginUser.getId());
        comment.setType(CommentTypeEnum.POST.getCode());
        comment.setBusinessId(postId);
        comment.setCreateTime(new Date());
        comment.setUpdateTime(new Date());
        commentService.insert(comment);

        // 修改评论数
        postService.resetCommentSize(postId);
        return JsonResult.success("回复成功", comment.getId());
    }

    /**
     * 发布回答评论
     *
     * @param answerId 回答ID
     * @param content  评论的内容
     * @return 重定向到/admin/comment
     */
    @PostMapping(value = "/comment/answer")
    @ResponseBody
    public JsonResult newAnswerComment(@RequestParam(value = "answerId") Long answerId,
                                       @RequestParam("content") String content) {


        // 判断是否登录
        User loginUser = getLoginUser();
        if (loginUser == null) {
            return JsonResult.error("请先登录");
        }

        // 判断回答是否存在
        Answer answer = answerService.get(answerId);
        if (answer == null) {
            return JsonResult.error("回答不存在");
        }

        // 判断评论内容是否包含屏蔽字
        List<BlackWord> blackWordList = blackWordService.findAll();
        for (BlackWord blackWord : blackWordList) {
            if (content.contains(blackWord.getContent())) {
                return JsonResult.error("评论内容包含违规字符");
            }
        }

        // 如果是回复
        Comment comment = new Comment();
        User parentUser = userService.get(answer.getUserId());
        if (parentUser == null) {
            return JsonResult.error("该回答作者不存在");
        }
        String lastContent = "<a href='#answer" + answer.getId() + "'>@" + parentUser.getUserDisplayName() + "</a> ";
        comment.setCommentContent(lastContent + parentUser.getUserDisplayName() + ": " + HtmlUtil.escape(content));
        comment.setCommentParent(0L);
        comment.setAcceptUserId(answer.getUserId());
        comment.setPathTrace("/");
        comment.setType(CommentTypeEnum.ANSWER.getCode());
        comment.setUserId(loginUser.getId());
        comment.setBusinessId(answerId);
        comment.setCreateTime(new Date());
        comment.setUpdateTime(new Date());
        commentService.insert(comment);

        // 修改回答评论数
        answerService.resetCommentSize(answerId);
        return JsonResult.success("评论成功", comment.getId());
    }

    /**
     * 点赞评论
     *
     * @param commentId
     * @return
     */
    @PostMapping("/comment/like")
    @ResponseBody
    public JsonResult likeComment(@RequestParam("commentId") Long commentId) {
        Comment comment = commentService.get(commentId);
        if (comment == null) {
            return JsonResult.error("回复不存在");
        }
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentService.update(comment);
        return JsonResult.success();
    }

    /**
     * 点赞评论
     *
     * @param commentId
     * @return
     */
    @PostMapping("/comment/dislike")
    @ResponseBody
    public JsonResult dislikeComment(@RequestParam("commentId") Long commentId) {
        Comment comment = commentService.get(commentId);
        if (comment == null) {
            return JsonResult.error("回复不存在");
        }
        comment.setDislikeCount(comment.getDislikeCount() + 1);
        commentService.update(comment);
        return JsonResult.success();
    }
}
