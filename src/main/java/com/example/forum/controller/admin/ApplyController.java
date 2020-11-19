package com.example.forum.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.forum.controller.common.BaseController;
import com.example.forum.dto.JsonResult;
import com.example.forum.dto.QueryCondition;
import com.example.forum.entity.*;
import com.example.forum.enums.ApplyStatusEnum;
import com.example.forum.enums.RoleEnum;
import com.example.forum.service.ApplyService;
import com.example.forum.service.RoleService;
import com.example.forum.service.UserRoleRefService;
import com.example.forum.service.UserService;
import com.example.forum.util.PageUtil;
import com.example.forum.vo.SearchVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author 言曌
 * @date 2020/10/31 9:22 上午
 */
@Slf4j
@Controller
@RequestMapping(value = "/admin/apply")
public class ApplyController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplyService applyService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRoleRefService userRoleRefService;


    /**
     * 申请列表
     *
     * @param model model
     * @return 模板路径admin/admin_apply
     */
    @GetMapping
    public String applys(Model model,
                         @RequestParam(value = "status", defaultValue = "-1") Integer status,
                         @RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                         @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                         @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                         @RequestParam(value = "order", defaultValue = "desc") String order,
                         @ModelAttribute SearchVo searchVo) {

        Apply condition = new Apply();
        condition.setStatus(status);

        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        Page<Apply> applys = applyService.findAll(
                page,
                new QueryCondition<>(condition, searchVo));

        for (Apply apply : applys.getRecords()) {
            apply.setUser(userService.get(apply.getUserId()));
        }
        //封装分类和标签
        model.addAttribute("applys", applys.getRecords());
        model.addAttribute("pageInfo", PageUtil.convertPageVo(page));
        model.addAttribute("status", status);
        model.addAttribute("order", order);
        model.addAttribute("sort", sort);
        return "admin/admin_apply";
    }

    /**
     * 申请成为法律专业者
     *
     * @return
     */
    @PostMapping("/send")
    @ResponseBody
    public JsonResult index(@RequestParam("document") String document) {
        User user = getLoginUser();

        // 判断是否已经申请过
        List<Apply> applyList = applyService.findByUserIdAndStatus(user.getId(), ApplyStatusEnum.NOT_CHECKED.getCode());
        if (applyList != null && applyList.size() > 0) {
            return JsonResult.error("您已经申请过了，请耐心等待管理员审核");
        }

        Apply apply = new Apply();
        apply.setStatus(ApplyStatusEnum.NOT_CHECKED.getCode());
        apply.setTitle("申请认证法律从业者");
        apply.setUserId(user.getId());
        apply.setDocument(document);
        apply.setCreateTime(new Date());
        apply.setUpdateTime(new Date());
        apply.setCreateBy(user.getUserName());
        apply.setUpdateBy(user.getUserName());
        applyService.insert(apply);
        return JsonResult.success("申请成功，请耐心等待管理员审核");
    }


    /**
     * 审核通过
     *
     * @return
     */
    @PostMapping("/success")
    @ResponseBody
    @Transactional
    public JsonResult applySuccess(@RequestParam("id") Long id) {
        User user = getLoginUser();
        // 1.更新申请
        Apply apply = applyService.get(id);
        if (apply == null) {
            return JsonResult.error("申请不存在");
        }
        apply.setId(id);
        apply.setStatus(ApplyStatusEnum.CHECKED_SUCCESS.getCode());
        apply.setUpdateTime(new Date());
        apply.setUpdateBy(user.getUserName());
        applyService.update(apply);

        // 修改用户角色
        // 2.先删除该用户的角色关联
        userRoleRefService.deleteByUserId(apply.getUserId());
        Role role = roleService.findByRoleName(RoleEnum.EXPERT.name());
        if (role == null) {
            return JsonResult.error(RoleEnum.EXPERT.name() + "角色不存在");
        }
        // 3.添加角色关联
        userRoleRefService.insert(new UserRoleRef(apply.getUserId(), role.getId()));
        return JsonResult.success("审核通过完成");
    }


    /**
     * 审核不通过
     *
     * @return
     */
    @PostMapping("/failure")
    @ResponseBody
    public JsonResult applyFailure(@RequestParam("id") Long id,
                                   @RequestParam(value = "comment", required = false) String comment) {
        User user = getLoginUser();
        Apply apply = new Apply();
        apply.setId(id);
        apply.setStatus(ApplyStatusEnum.CHECKED_FAILURE.getCode());
        apply.setUpdateTime(new Date());
        apply.setComment(comment);
        apply.setUpdateBy(user.getUserName());
        applyService.update(apply);
        return JsonResult.success("审核不通过完成");
    }


    /**
     * 删除
     *
     * @param id Id
     * @return JsonResult
     */
    @DeleteMapping(value = "/delete")
    @ResponseBody
    public JsonResult checkDelete(@RequestParam("id") Long id) {
        User user = getLoginUser();
        Apply apply = applyService.get(id);
        if (apply == null) {
            return JsonResult.error("申请不存在");
        }
        if (!Objects.equals(user.getId(), apply.getUserId()) && loginUserIsAdmin()) {
            return JsonResult.error("没有权限操作");
        }

        applyService.delete(id);
        return JsonResult.success("删除成功");
    }

    /**
     * 批量删除
     *
     * @param ids 文章ID列表
     * @return 重定向到/admin/post
     */
    @DeleteMapping(value = "/batchDelete")
    @ResponseBody
    public JsonResult batchDelete(@RequestParam("ids") List<Long> ids) {
        for (Long id : ids) {
            applyService.delete(id);
        }
        return JsonResult.success("删除成功");
    }

}
