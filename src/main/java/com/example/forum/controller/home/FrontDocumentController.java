package com.example.forum.controller.home;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.forum.controller.common.BaseController;
import com.example.forum.dto.PostQueryCondition;
import com.example.forum.dto.QueryCondition;
import com.example.forum.entity.Document;
import com.example.forum.entity.Post;
import com.example.forum.entity.User;
import com.example.forum.service.DocumentService;
import com.example.forum.service.PostService;
import com.example.forum.service.TagService;
import com.example.forum.util.PageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author 言曌
 * @date 2020/3/11 4:59 下午
 */
@Controller
public class FrontDocumentController extends BaseController {

    @Autowired
    private DocumentService documentService;

    /**
     * 文档列表
     *
     * @param pageNumber
     * @param pageSize
     * @param sort
     * @param order
     * @param keywords
     * @param model
     * @return
     */
    @GetMapping("/document")
    public String index(@RequestParam(value = "page", defaultValue = "1") Integer pageNumber,
                        @RequestParam(value = "size", defaultValue = "10") Integer pageSize,
                        @RequestParam(value = "sort", defaultValue = "createTime") String sort,
                        @RequestParam(value = "order", defaultValue = "desc") String order,
                        @RequestParam(value = "keywords", required = false) String keywords,
                        Model model) {

        Page page = PageUtil.initMpPage(pageNumber, pageSize, sort, order);
        Document condition = new Document();
        condition.setName(keywords);
        Page<Document> postPage = documentService.findAll(page, new QueryCondition<>(condition));
        model.addAttribute("documents", postPage.getRecords());
        model.addAttribute("page", postPage);
        return "home/document";
    }

    /**
     * 文件下载
     *
     * @param id       文件ID
     * @param response
     */
    @GetMapping(value = "/document/download")
    public void downloadFile(@RequestParam("id") Long id, HttpServletResponse response) throws IOException {
        User user = getLoginUser();
        if (user == null) {
            response.sendRedirect("/login");
            return;
        }
        Document document = documentService.get(id);
        if (document == null) {
            response.sendRedirect("/404");
        }
        if (document != null) {
            InputStream f = new FileInputStream(new File(document.getPath()));
            response.reset();
            response.setContentType("application/x-msdownload;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(document.getName(), "UTF-8") + "." + document.getSuffix());
            ServletOutputStream sout = response.getOutputStream();
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;
            try {
                bis = new BufferedInputStream(f);
                bos = new BufferedOutputStream(sout);
                byte[] buff = new byte[2048];
                int bytesRead;
                while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                    bos.write(buff, 0, bytesRead);
                }
                bos.flush();
                bos.close();
                bis.close();
            } catch (final IOException e) {
                throw e;
            } finally {
                if (bis != null) {
                    bis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            }
        }
    }

}
