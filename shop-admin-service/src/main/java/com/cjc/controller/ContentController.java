package com.cjc.controller;

import com.cjc.pojo.TbContent;
import com.cjc.pojo.TbContentCategory;
import com.cjc.query.QueryParams;
import com.cjc.service.ContentService;
import com.cjc.util.AliOssUtil;
import com.cjc.util.BaiduAuditUtil;
import com.cjc.util.PageList;
import com.cjc.util.Result;
import com.cjc.vo.TbContentCategoryVo;
import com.cjc.vo.TbContentVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "广告管理", description = "广告管理接口")
@RestController
@RequestMapping("/admin/content")
@Slf4j
public class ContentController {

    @Autowired
    private ContentService contentService;

    @Autowired
    private AliOssUtil aliOssUtil;

    @Autowired
    private BaiduAuditUtil baiduAuditUtil;

    /**
     * 广告分页查询
     */
    @PostMapping("/queryPage")
    public Result<PageList<TbContentVo>> queryPage(@RequestBody QueryParams<TbContent> queryParams) {
        PageList<TbContentVo> pageList = contentService.queryPage(queryParams);
        return Result.success(pageList);
    }

    /**
     * 广告保存（新增/修改）
     */
    @PostMapping("/save")
    public Result save(@RequestBody TbContent content) {
        if (content.getId() == null) {
            contentService.insert(content);
        } else {
            contentService.update(content);
        }
        return Result.success();
    }

    /**
     * 广告新增
     */
    @PostMapping("/insert")
    public Result insert(@RequestBody TbContent content) {
        contentService.insert(content);
        return Result.success();
    }

    /**
     * 广告修改
     */
    @PostMapping("/update")
    public Result update(@RequestBody TbContent content) {
        contentService.update(content);
        return Result.success();
    }

    /**
     * 广告删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        contentService.delete(id);
        return Result.success();
    }

    /**
     * 广告批量删除
     */
    @PatchMapping("/batchDelete")
    public Result batchDelete(@RequestBody List<Long> ids) {
        contentService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 查询所有广告
     */
    @GetMapping("/queryAll")
    public Result<List<TbContentVo>> queryAll() {
        List<TbContentVo> list = contentService.queryAll();
        return Result.success(list);
    }

    /**
     * 根据分类查询广告
     */
    @GetMapping("/queryByCategoryId")
    public Result<List<TbContentVo>> queryByCategoryId(@RequestParam Long categoryId) {
        List<TbContentVo> list = contentService.queryByCategoryId(categoryId);
        return Result.success(list);
    }

    /**
     * 根据分类KEY查询广告（前台主站使用）
     * 如：key=index_banner 获取首页轮播图
     */
    @GetMapping("/queryByKey")
    public Result<List<TbContentVo>> queryByKey(@RequestParam String key) {
        List<TbContentVo> list = contentService.queryByCategoryKey(key);
        return Result.success(list);
    }

    /**
     * 上传广告图片
     */
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String url = aliOssUtil.upload(file.getBytes(), originalFilename);

            // 图片审核（百度云）
            boolean isSafe = baiduAuditUtil.auditImage(url);
            if (!isSafe) {
                return Result.fail("广告图片涉嫌违规内容，已被系统拦截");
            }

            log.info("广告图片上传成功: {}", url);
            return Result.success(url);
        } catch (IOException e) {
            log.error("广告图片上传失败", e);
            return Result.fail("图片上传失败");
        }
    }

    /**
     * 广告分类分页查询
     */
    @PostMapping("/category/queryPage")
    public Result<PageList<TbContentCategoryVo>> categoryQueryPage(@RequestBody QueryParams<TbContentCategory> queryParams) {
        PageList<TbContentCategoryVo> pageList = contentService.queryCategoryPage(queryParams);
        return Result.success(pageList);
    }

    /**
     * 广告分类保存（新增/修改）
     */
    @PostMapping("/category/save")
    public Result categorySave(@RequestBody TbContentCategory category) {
        if (category.getId() == null) {
            contentService.insertCategory(category);
        } else {
            contentService.updateCategory(category);
        }
        return Result.success();
    }

    /**
     * 广告分类新增
     */
    @PostMapping("/category/insert")
    public Result categoryInsert(@RequestBody TbContentCategory category) {
        contentService.insertCategory(category);
        return Result.success();
    }

    /**
     * 广告分类修改
     */
    @PostMapping("/category/update")
    public Result categoryUpdate(@RequestBody TbContentCategory category) {
        contentService.updateCategory(category);
        return Result.success();
    }

    /**
     * 广告分类删除
     */
    @DeleteMapping("/category/delete/{id}")
    public Result categoryDelete(@PathVariable Long id) {
        contentService.deleteCategory(id);
        return Result.success();
    }

    /**
     * 查询所有广告分类
     */
    @GetMapping("/category/queryAll")
    public Result<List<TbContentCategoryVo>> categoryQueryAll() {
        List<TbContentCategoryVo> list = contentService.queryAllCategory();
        return Result.success(list);
    }
}