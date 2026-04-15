-- 商品详情页静态化 - 数据库字段扩展
-- 在 tb_goods 表添加 static_url 字段

-- 添加静态页URL字段
ALTER TABLE tb_goods ADD COLUMN static_url VARCHAR(255) DEFAULT NULL COMMENT '静态页URL（商品详情页静态化）';

-- 可选：为 static_url 字段添加索引（如果经常根据URL查询）
-- CREATE INDEX idx_static_url ON tb_goods(static_url);

-- 说明：
-- 1. static_url 存储商品详情静态HTML文件的访问URL
-- 2. 审核通过时自动生成静态页并更新此字段
-- 3. 下架时删除静态页并清空此字段
-- 4. 如果字段为空，前端使用动态接口 /goods/detail/{id}
-- 5. 如果字段有值，前端可直接访问静态页URL（CDN返回，不经过后端）