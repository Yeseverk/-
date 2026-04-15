package com.cjc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cjc.exception.BusinessException;
import com.cjc.pojo.TbGoods;
import com.cjc.pojo.TbGoodsDesc;
import com.cjc.pojo.TbItem;
import com.cjc.service.GoodsService;
import com.cjc.service.GoodsStaticService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class GoodsStaticServiceImpl implements GoodsStaticService {

    @Autowired
    private Configuration freemarkerConfig;

    @Lazy
    @Autowired
    private GoodsService goodsService;

    @Value("${static.page.path:/data/static/goods}")
    private String staticPagePath;

    @Value("${static.page.url:http://localhost:8086/static/goods}")
    private String staticPageUrl;

    @Override
    public String generateStaticPage(Long goodsId) {
        return generateStaticPage(goodsId, null);
    }

    public String generateStaticPage(Long goodsId, String oldStaticUrl) {
        try {
            Map<String, Object> data = goodsService.getDetailById(goodsId);
            TbGoods goods = (TbGoods) data.get("goods");
            TbGoodsDesc goodsDesc = (TbGoodsDesc) data.get("goodsDesc");
            List<TbItem> itemList = (List<TbItem>) data.get("itemList");

            if (goods == null) {
                throw new BusinessException("商品不存在，无法生成静态页");
            }

            List<Map<String, Object>> specList = extractSpecList(itemList);

            Map<String, Object> templateData = new HashMap<>();
            templateData.put("goods", goods);
            templateData.put("goodsDesc", sanitizeGoodsDesc(goodsDesc));
            templateData.put("itemList", itemList);
            templateData.put("specList", specList);
            templateData.put("skuDataJson", JSON.toJSONString(itemList));

            int defaultStock = 99;
            if (itemList != null && !itemList.isEmpty()) {
                TbItem defaultItem = itemList.stream()
                    .filter(item -> "1".equals(item.getIsDefault()))
                    .findFirst()
                    .orElse(itemList.get(0));
                defaultStock = defaultItem.getNum() != null ? defaultItem.getNum() : 99;
            }
            templateData.put("defaultStock", defaultStock);

            long version = System.currentTimeMillis();
            templateData.put("version", version);

            Template template = freemarkerConfig.getTemplate("goods_detail.ftl");
            String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, templateData);

            // 存到本地文件系统
            String fileName = goodsId + "_" + version + ".html";
            File dir = new File(staticPagePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            File htmlFile = new File(dir, fileName);
            try (FileWriter writer = new FileWriter(htmlFile, StandardCharsets.UTF_8)) {
                writer.write(htmlContent);
            }

            // 删除旧版本
            if (oldStaticUrl != null && !oldStaticUrl.isEmpty()) {
                deleteOldStaticPage(oldStaticUrl);
            } else {
                // 删除同商品的其他版本
                cleanOldVersions(goodsId, fileName);
            }

            String url = staticPageUrl + "/" + fileName;
            log.info("商品静态页生成成功: goodsId={}, path={}, url={}", goodsId, htmlFile.getAbsolutePath(), url);
            return url;

        } catch (IOException | TemplateException e) {
            log.error("生成静态页失败: goodsId={}", goodsId, e);
            throw new BusinessException("生成静态页失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteStaticPage(Long goodsId, String staticUrl) {
        if (staticUrl != null && !staticUrl.isEmpty()) {
            String fileName = extractFileName(staticUrl);
            if (fileName != null) {
                File file = new File(staticPagePath, fileName);
                if (file.exists() && file.delete()) {
                    log.info("删除静态页: goodsId={}, file={}", goodsId, fileName);
                }
            }
        }
    }

    @Override
    public void batchGenerateStaticPage(List<Long> goodsIds) {
        batchGenerateStaticPageWithResult(goodsIds);
    }

    public Map<String, Object> batchGenerateStaticPageWithResult(List<Long> goodsIds) {
        Map<String, Object> result = new HashMap<>();
        List<Long> successList = new ArrayList<>();
        List<Map<String, Object>> failList = new ArrayList<>();

        if (goodsIds == null || goodsIds.isEmpty()) {
            result.put("total", 0);
            result.put("success", 0);
            result.put("fail", 0);
            return result;
        }

        for (Long goodsId : goodsIds) {
            try {
                generateStaticPage(goodsId);
                successList.add(goodsId);
            } catch (Exception e) {
                log.error("批量生成静态页失败: goodsId={}", goodsId, e);
                Map<String, Object> failItem = new HashMap<>();
                failItem.put("goodsId", goodsId);
                failItem.put("reason", e.getMessage());
                failList.add(failItem);
            }
        }

        result.put("total", goodsIds.size());
        result.put("success", successList.size());
        result.put("fail", failList.size());
        return result;
    }

    public String regenerateStaticPage(Long goodsId, String oldStaticUrl) {
        return generateStaticPage(goodsId, oldStaticUrl);
    }

    private void deleteOldStaticPage(String oldStaticUrl) {
        String fileName = extractFileName(oldStaticUrl);
        if (fileName != null) {
            File file = new File(staticPagePath, fileName);
            if (file.exists() && file.delete()) {
                log.info("删除旧版本静态页: {}", fileName);
            }
        }
    }

    private void cleanOldVersions(Long goodsId, String currentFileName) {
        File dir = new File(staticPagePath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> 
                name.startsWith(goodsId + "_") && name.endsWith(".html") && !name.equals(currentFileName)
            );
            if (files != null) {
                for (File file : files) {
                    if (file.delete()) {
                        log.info("清理旧版本静态页: {}", file.getName());
                    }
                }
            }
        }
    }

    private String extractFileName(String url) {
        if (url == null) return null;
        int lastSlash = url.lastIndexOf("/");
        if (lastSlash >= 0 && lastSlash < url.length() - 1) {
            return url.substring(lastSlash + 1);
        }
        return null;
    }

    private TbGoodsDesc sanitizeGoodsDesc(TbGoodsDesc goodsDesc) {
        if (goodsDesc == null) {
            return null;
        }

        Safelist safelist = Safelist.relaxed()
            .addTags("figure", "figcaption", "video", "source", "iframe")
            .addAttributes("video", "src", "controls", "width", "height", "poster")
            .addAttributes("source", "src", "type")
            .addAttributes("iframe", "src", "width", "height", "frameborder", "allowfullscreen")
            .addAttributes("img", "data-src", "data-lazy", "loading")
            .addAttributes("a", "target", "rel");

        if (goodsDesc.getIntroduction() != null) {
            goodsDesc.setIntroduction(Jsoup.clean(goodsDesc.getIntroduction(), safelist));
        }
        if (goodsDesc.getPackageList() != null) {
            goodsDesc.setPackageList(Jsoup.clean(goodsDesc.getPackageList(), safelist));
        }
        if (goodsDesc.getSaleService() != null) {
            goodsDesc.setSaleService(Jsoup.clean(goodsDesc.getSaleService(), safelist));
        }

        return goodsDesc;
    }

    private List<Map<String, Object>> extractSpecList(List<TbItem> itemList) {
        if (itemList == null || itemList.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, Set<String>> specMap = new LinkedHashMap<>();

        for (TbItem item : itemList) {
            if (item.getSpec() != null && !item.getSpec().isEmpty()) {
                try {
                    JSONObject specJson = JSON.parseObject(item.getSpec());
                    for (String key : specJson.keySet()) {
                        specMap.computeIfAbsent(key, k -> new LinkedHashSet<>())
                            .add(specJson.getString(key));
                    }
                } catch (Exception e) {
                    log.warn("解析SKU规格失败: spec={}", item.getSpec());
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : specMap.entrySet()) {
            Map<String, Object> spec = new HashMap<>();
            spec.put("specName", entry.getKey());
            spec.put("options", new ArrayList<>(entry.getValue()));
            result.add(spec);
        }

        return result;
    }
}
