package com.example.forum.controller.admin;

import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.forum.controller.common.BaseController;
import com.example.forum.dto.JsonResult;
import com.example.forum.dto.QueryCondition;
import com.example.forum.entity.*;
import com.example.forum.enums.*;
import com.example.forum.exception.MyBusinessException;
import com.example.forum.service.*;
import com.example.forum.util.PageUtil;
import com.example.forum.vo.SearchVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * <pre>
 *     后台问题管理控制器
 * </pre>
 *
 * @author : saysky
 * @date : 2017/12/10
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/question")
public class QuestionController extends BaseController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionMarkRefService questionMarkRefService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    /**
     * 处理后台获取问题列表的请求
     *
     * @param model model
     * @return 模板路径admin/admin_question
     */
    @GetMapping
    public String questions(Model model,
                            @RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                            @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                            @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                            @RequestParam(value = "order", defaultValue = "desc") String order,
                            @ModelAttribute SearchVo searchVo) {

        Long loginUserId = getLoginUserId();
        Question condition = new Question();
        // 管理员可以查看所有用户的，非管理员只能看到自己的问题
        if (!loginUserIsAdmin()) {
            condition.setUserId(loginUserId);
        }

        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        Page<Question> questions = questionService.findAll(
                page,
                new QueryCondition<>(condition, searchVo));

        for (Question question : questions.getRecords()) {
            question.setUser(userService.get(question.getUserId()));
        }

        //封装分类和标签
        model.addAttribute("questions", questions.getRecords());
        model.addAttribute("pageInfo", PageUtil.convertPageVo(page));
        model.addAttribute("sort", sort);
        return "admin/admin_question";
    }


    /**
     * 已收藏的
     *
     * @param model model
     * @return 模板路径admin/admin_question
     */
    @GetMapping("/mark")
    public String markedQuestions(Model model,
                                  @RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                                  @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                                  @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                                  @RequestParam(value = "order", defaultValue = "desc") String order,
                                  @ModelAttribute SearchVo searchVo) {

        Long loginUserId = getLoginUserId();
        QuestionMarkRef condition = new QuestionMarkRef();
        // 管理员可以查看所有用户的，非管理员只能看到自己的问题
        if (!loginUserIsAdmin()) {
            condition.setUserId(loginUserId);
        }

        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        Page<QuestionMarkRef> marks = questionMarkRefService.findAll(
                page,
                new QueryCondition<>(condition, searchVo));

        for (QuestionMarkRef questionMarkRef : marks.getRecords()) {
            questionMarkRef.setQuestion(questionService.get(questionMarkRef.getQuestionId()));
            questionMarkRef.setUser(userService.get(questionMarkRef.getUserId()));
        }

        model.addAttribute("marks", marks.getRecords());
        model.addAttribute("pageInfo", PageUtil.convertPageVo(page));
        return "admin/admin_question_mark";
    }


    /**
     * 处理跳转到新建问题页面
     *
     * @return 模板路径admin/admin_question_new
     */
    @GetMapping(value = "/new")
    public String newQuestion() {
        return "admin/admin_question_new";
    }


    /**
     * 添加/更新问题
     *
     * @param question Question实体
     */
    @PostMapping(value = "/save")
    @ResponseBody
    public JsonResult pushQuestion(@ModelAttribute Question question) {

        // 1.获得登录用户
        User user = getLoginUser();
        Boolean isAdmin = loginUserIsAdmin();
        question.setUserId(getLoginUserId());
        question.setCreateBy(user.getUserName());
        question.setUpdateBy(user.getUserName());
        question.setAnswerCount(0);
        question.setMarkCount(0);

        //2、非管理员只能修改自己的问题，管理员都可以修改
        Question originQuestion = null;
        if (question.getId() != null) {
            originQuestion = questionService.get(question.getId());
            if (!Objects.equals(originQuestion.getUserId(), user.getId()) && !isAdmin) {
                return JsonResult.error("没有权限");
            }
            //以下属性不能修改
            question.setUserId(originQuestion.getUserId());
            question.setAnswerCount(originQuestion.getAnswerCount());
            question.setMarkCount(originQuestion.getMarkCount());
            question.setDelFlag(originQuestion.getDelFlag());
            question.setCreateBy(originQuestion.getCreateBy());
        }
        // 3、提取摘要
        int questionSummaryLength = 100;
        //问题摘要
        String summaryText = HtmlUtil.cleanHtmlTag(question.getContent());
        if (summaryText.length() > questionSummaryLength) {
            summaryText = summaryText.substring(0, questionSummaryLength);
        }
        question.setSummary(summaryText);

        // 4.添加/更新入库
        questionService.insertOrUpdate(question);
        return JsonResult.success("发布成功");
    }


    /**
     * 处理删除问题的请求
     *
     * @param questionId 问题编号
     * @return 重定向到/admin/question
     */
    @DeleteMapping(value = "/delete")
    @ResponseBody
    public JsonResult removeQuestion(@RequestParam("id") Long questionId) {
        Question question = questionService.get(questionId);
        basicCheck(question);
        questionService.delete(questionId);
        return JsonResult.success("删除成功");
    }

    /**
     * 批量删除
     *
     * @param ids 问题ID列表
     * @return 重定向到/admin/question
     */
    @DeleteMapping(value = "/batchDelete")
    @ResponseBody
    public JsonResult batchDelete(@RequestParam("ids") List<Long> ids) {
        Long userId = getLoginUserId();
        //批量操作
        //1、防止恶意操作
        if (ids == null || ids.size() == 0 || ids.size() >= 100) {
            return new JsonResult(ResultCodeEnum.FAIL.getCode(), "参数不合法!");
        }
        //2、检查用户权限
        //问题作者才可以删除
        List<Question> questionList = questionService.findByBatchIds(ids);
        for (Question question : questionList) {
            if (!Objects.equals(question.getUserId(), userId) && !loginUserIsAdmin()) {
                return new JsonResult(ResultCodeEnum.FAIL.getCode(), "没有权限");
            }
        }
        //3、删除
        for (Question question : questionList) {
            questionService.delete(question.getId());
        }
        return JsonResult.success("删除成功");
    }


    /**
     * 检查问题是否存在和用户是否有权限控制
     *
     * @param question
     */
    private void basicCheck(Question question) {
        if (question == null) {
            throw new MyBusinessException("问题不存在");
        }
        //只有创建者有权删除
        User user = getLoginUser();
        //管理员和问题作者可以删除
        Boolean isAdmin = loginUserIsAdmin();
        if (!Objects.equals(question.getUserId(), user.getId()) && !isAdmin) {
            throw new MyBusinessException("没有权限");
        }
    }

    /**
     * 跳转到编辑问题页面
     *
     * @param questionId 问题编号
     * @param model      model
     * @return 模板路径admin/admin_editor
     */
    @GetMapping(value = "/edit")
    public String editQuestion(@RequestParam("id") Long questionId, Model model) {
        Question question = questionService.get(questionId);
        basicCheck(question);

        model.addAttribute("question", question);
        return "admin/admin_question_edit";
    }


}
