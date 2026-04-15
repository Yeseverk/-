package com.cjc.controller;

import com.cjc.dto.GoodsDto;
import com.cjc.mapper.TbGoodsMapper;
import com.cjc.pojo.TbGoods;
import com.cjc.query.QueryParams;
import com.cjc.service.GoodsService;
import com.cjc.service.GoodsStaticService;
import com.cjc.util.*;
import com.cjc.vo.TbGoodsVo;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/goods")
@Slf4j
public class GoodsController {
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private BaiduAuditUtil baiduAuditUtil;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private GoodsStaticService goodsStaticService;
    @Autowired
    private TbGoodsMapper goodsMapper;

    // ========== 商家接口 ==========

    @PostMapping("/seller/queryPage")
    public Result<PageList<TbGoodsVo>> sellerQueryPage(@RequestBody QueryParams<TbGoods> queryParams, HttpServletRequest request) {
        String token = request.getHeader("token");
        Claims claims = JwtUtil.parseJwt(token);
        String sellerId = claims.getId();
        PageList<TbGoodsVo> pageList = goodsService.queryPage(queryParams, sellerId);
        return Result.success(pageList);
    }

    @PostMapping("/seller/file/upload")
    public Result upload(MultipartFile file) {
        log.info("接收到文件上传请求: {}", file.getOriginalFilename());
        try {
            String originalFilename = file.getOriginalFilename();
            String filePath = aliOssUtil.upload(file.getBytes(), originalFilename);
            // 图片审核（百度云，每月免费5000次）
            boolean isSafe = baiduAuditUtil.auditImage(filePath);
            if (!isSafe) {
                return Result.fail("图片涉嫌违规内容，已被系统拦截，请重新上传合规图片");
            }
            return Result.success(filePath);
        } catch (IOException e) {
            return Result.fail("文件上传失败，请稍后重试");
        }
    }

    @PostMapping("/seller/save")
    public Result save(@RequestBody GoodsDto goodsDto, HttpServletRequest request) {
        String token = request.getHeader("token");
        Claims claims = JwtUtil.parseJwt(token);
        String sellerId = claims.getId();
        goodsService.save(goodsDto, sellerId);
        return Result.success();
    }

    @DeleteMapping("/seller/delete/{id}")
    public Result delete(@PathVariable("id") Long id, HttpServletRequest request) {
        String token = request.getHeader("token");
        Claims claims = JwtUtil.parseJwt(token);
        String sellerId = claims.getId();
        goodsService.delete(id, sellerId);
        return Result.success();
    }

    @PatchMapping("/seller/batchDelete")
    public Result batchDelete(@RequestBody List<Long> ids, HttpServletRequest request) {
        String token = request.getHeader("token");
        Claims claims = JwtUtil.parseJwt(token);
        String sellerId = claims.getId();
        goodsService.batchDelete(ids, sellerId);
        return Result.success();
    }

    @GetMapping("/seller/{id}")
    public Result<GoodsDto> getById(@PathVariable Long id, HttpServletRequest request) {
        String token = request.getHeader("token");
        Claims claims = JwtUtil.parseJwt(token);
        String sellerId = claims.getId();
        GoodsDto dto = goodsService.getById(id, sellerId);
        return Result.success(dto);
    }

    @PostMapping("/seller/update")
    public Result update(@RequestBody GoodsDto goodsDto, HttpServletRequest request) {
        String token = request.getHeader("token");
        Claims claims = JwtUtil.parseJwt(token);
        String sellerId = claims.getId();
        goodsService.update(goodsDto, sellerId);
        return Result.success();
    }

    @PostMapping("/seller/submitAudit")
    public Result submitAudit(@RequestBody List<Long> ids, HttpServletRequest request) {
        String token = request.getHeader("token");
        Claims claims = JwtUtil.parseJwt(token);
        String sellerId = claims.getId();
        goodsService.submitAudit(ids, sellerId);
        return Result.success();
    }

    /**
     * 商家 - 上架商品（只有审核通过的商品才能上架）
     */
    @PostMapping("/seller/putOnSale")
    public Result putOnSale(@RequestBody List<Long> ids, HttpServletRequest request) {
        String token = request.getHeader("token");
        Claims claims = JwtUtil.parseJwt(token);
        String sellerId = claims.getId();
        goodsService.putOnSale(ids, sellerId);
        return Result.success();
    }

    /**
     * 商家 - 下架商品
     */
    @PostMapping("/seller/pullOffSale")
    public Result pullOffSale(@RequestBody List<Long> ids, HttpServletRequest request) {
        String token = request.getHeader("token");
        Claims claims = JwtUtil.parseJwt(token);
        String sellerId = claims.getId();
        goodsService.pullOffSale(ids, sellerId);
        return Result.success();
    }

    // ========== 运营商接口 ==========

    /**
     * 运营商 - 商品列表查询
     */
    @PostMapping("/admin/queryPage")
    public Result<PageList<TbGoodsVo>> adminQueryPage(@RequestBody QueryParams<TbGoods> queryParams) {
        // 运营商可以查看所有商品，不需要 token 验证 sellerId
        PageList<TbGoodsVo> pageList = goodsService.adminQueryPage(queryParams);
        return Result.success(pageList);
    }

    /**
     * 运营商 - 商品详情查看
     */
    @GetMapping("/admin/detail/{id}")
    public Result<Map<String, Object>> adminDetail(@PathVariable Long id) {
        Map<String, Object> detail = goodsService.getAdminDetailById(id);
        return Result.success(detail);
    }

    /**
     * 运营商 - 审核商品
     */
    @PostMapping("/admin/audit")
    public Result audit(@RequestBody Map<String, Object> params) {
        Long id = Long.parseLong(params.get("id").toString());
        String auditStatus = params.get("auditStatus").toString();
        goodsService.audit(id, auditStatus);
        return Result.success();
    }

    /**
     * 运营商 - 批量审核
     */
    @PostMapping("/admin/batchAudit")
    public Result batchAudit(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Number> idNumbers = (List<Number>) params.get("ids");
        List<Long> ids = idNumbers.stream().map(Number::longValue).toList();
        String auditStatus = params.get("auditStatus").toString();
        goodsService.batchAudit(ids, auditStatus);
        return Result.success();
    }

    /**
     * 运营商 - 批量删除
     */
    @PostMapping("/admin/batchDelete")
    public Result adminBatchDelete(@RequestBody List<Number> ids) {
        List<Long> longIds = ids.stream().map(Number::longValue).toList();
        goodsService.adminBatchDelete(longIds);
        return Result.success();
    }

    // ========== 前台公开接口（无需登录） ==========

    /**
     * 前台 - 商品列表（只展示已上架且审核通过的商品）
     */
    @PostMapping("/list")
    public Result<PageList<TbGoodsVo>> list(@RequestBody QueryParams<TbGoods> queryParams) {
        PageList<TbGoodsVo> pageList = goodsService.list(queryParams);
        return Result.success(pageList);
    }

    /**
     * 前台 - 商品详情（只展示已上架且审核通过的商品）
     */
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Map<String, Object> detail = goodsService.getDetailById(id);
        return Result.success(detail);
    }

    // ========== 商家端规格模板接口 ==========

    /**
     * 商家端 - 根据分类ID获取类型模板（包含规格和品牌）
     */
    @GetMapping("/seller/template/{categoryId}")
    public Result<Map<String, Object>> getTemplateByCategory(@PathVariable Long categoryId) {
        Map<String, Object> template = goodsService.getTemplateByCategory(categoryId);
        return Result.success(template);
    }

    /**
     * 商家端 - 根据规格ID列表获取规格详情（规格名称 + 选项列表）
     */
    @PostMapping("/seller/specs/detail")
    public Result<List<Map<String, Object>>> getSpecsDetail(@RequestBody List<Long> specIds) {
        List<Map<String, Object>> specs = goodsService.getSpecsDetail(specIds);
        return Result.success(specs);
    }

    // ========== 静态页接口 ==========

    /**
     * 获取商品静态页URL
     * 如果有静态页返回静态页URL，否则返回动态接口地址
     */
    @GetMapping("/static/{id}")
    public Result<String> getStaticUrl(@PathVariable Long id) {
        TbGoods goods = goodsMapper.selectByPrimaryKey(id);
        if (goods == null) {
            return Result.fail("商品不存在");
        }
        if (goods.getStaticUrl() != null && !goods.getStaticUrl().isEmpty()) {
            // 有静态页，返回静态页URL
            return Result.success(goods.getStaticUrl());
        }
        // 没有静态页，返回动态接口地址
        return Result.success("/goods/detail/" + id);
    }

    /**
     * 运营商 - 手动重新生成静态页
     * 用于商品编辑后刷新静态页
     */
    @PostMapping("/admin/regenerateStatic/{id}")
    public Result regenerateStatic(@PathVariable Long id) {
        TbGoods goods = goodsMapper.selectByPrimaryKey(id);
        if (goods == null) {
            return Result.fail("商品不存在");
        }
        // 只有审核通过且上架的商品才生成静态页
        if (!"2".equals(goods.getAuditStatus()) || !"1".equals(goods.getIsMarketable())) {
            return Result.fail("只有审核通过且上架的商品才能生成静态页");
        }

        try {
            // 使用regenerateStaticPage，会自动删除旧版本
            String oldStaticUrl = goods.getStaticUrl();
            String staticUrl = ((com.cjc.service.impl.GoodsStaticServiceImpl) goodsStaticService)
                .regenerateStaticPage(id, oldStaticUrl);
            // 更新数据库
            TbGoods updateGoods = new TbGoods();
            updateGoods.setId(id);
            updateGoods.setStaticUrl(staticUrl);
            goodsMapper.updateByPrimaryKeySelective(updateGoods);
            return Result.success(staticUrl);
        } catch (Exception e) {
            log.error("重新生成静态页失败: goodsId={}", id, e);
            return Result.fail("生成静态页失败: " + e.getMessage());
        }
    }

    /**
     * 运营商 - 批量重新生成静态页
     */
    @PostMapping("/admin/batchRegenerateStatic")
    public Result batchRegenerateStatic(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail("商品ID列表不能为空");
        }

        Map<String, Object> result = ((com.cjc.service.impl.GoodsStaticServiceImpl) goodsStaticService)
            .batchGenerateStaticPageWithResult(ids);
        return Result.success(result);
    }
}
