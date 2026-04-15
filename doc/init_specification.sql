-- =============================================
-- 商品规格模板初始化脚本
-- =============================================

-- 清空现有数据（先执行这部分）
TRUNCATE TABLE tb_specification_option;
TRUNCATE TABLE tb_specification;
TRUNCATE TABLE tb_type_template;

-- =============================================
-- 1. 规格表 (tb_specification)
-- =============================================
INSERT INTO tb_specification (id, spec_name) VALUES
-- 基础规格
(1, '网络'),
(2, '内存'),
(3, '颜色'),
(4, '存储容量'),
(5, '版本'),
(6, '尺码'),
(7, '尺寸'),
(8, '口味'),
(9, '规格'),
(10, '材质'),
(11, '套装'),
(12, '适用人群'),
(13, '型号'),
(14, '直径'),
(15, '适用灶具');

-- =============================================
-- 2. 规格选项表 (tb_specification_option)
-- =============================================

-- 网络 (spec_id=1)
INSERT INTO tb_specification_option (id, spec_id, option_name, orders) VALUES
(1, 1, '移动4G', 1),
(2, 1, '联通4G', 2),
(3, 1, '电信4G', 3),
(4, 1, '移动5G', 4),
(5, 1, '联通5G', 5),
(6, 1, '电信5G', 6),
(7, 1, '全网通', 7);

-- 内存 (spec_id=2)
INSERT INTO tb_specification_option (id, spec_id, option_name, orders) VALUES
(10, 2, '4GB', 1),
(11, 2, '6GB', 2),
(12, 2, '8GB', 3),
(13, 2, '12GB', 4),
(14, 2, '16GB', 5),
(15, 2, '24GB', 6),
(16, 2, '32GB', 7);

-- 颜色 (spec_id=3)
INSERT INTO tb_specification_option (id, spec_id, option_name, orders) VALUES
(20, 3, '黑色', 1),
(21, 3, '白色', 2),
(22, 3, '红色', 3),
(23, 3, '蓝色', 4),
(24, 3, '绿色', 5),
(25, 3, '金色', 6),
(26, 3, '银色', 7),
(27, 3, '灰色', 8),
(28, 3, '粉色', 9),
(29, 3, '紫色', 10);

-- 存储容量 (spec_id=4)
INSERT INTO tb_specification_option (id, spec_id, option_name, orders) VALUES
(30, 4, '64GB', 1),
(31, 4, '128GB', 2),
(32, 4, '256GB', 3),
(33, 4, '512GB', 4),
(34, 4, '1TB', 5),
(35, 4, '2TB', 6);

-- 版本 (spec_id=5)
INSERT INTO tb_specification_option (id, spec_id, option_name, orders) VALUES
(40, 5, '标准版', 1),
(41, 5, '高配版', 2),
(42, 5, '尊享版', 3),
(43, 5, 'Pro版', 4),
(44, 5, 'Max版', 5),
(45, 5, '青春版', 6);

-- 尺码 (spec_id=6) - 服装鞋靴
INSERT INTO tb_specification_option (id, spec_id, option_name, orders) VALUES
(50, 6, 'S', 1),
(51, 6, 'M', 2),
(52, 6, 'L', 3),
(53, 6, 'XL', 4),
(54, 6, 'XXL', 5),
(55, 6, 'XXXL', 6),
(56, 6, '均码', 7);

-- 尺寸 (spec_id=7) - 家电等
INSERT INTO tb_specification_option (id, spec_id, option_name, orders) VALUES
(60, 7, '小号', 1),
(61, 7, '中号', 2),
(62, 7, '大号', 3),
(63, 7, '加大号', 4),
(64, 7, '32寸', 5),
(65, 7, '43寸', 6),
(66, 7, '55寸', 7),
(67, 7, '65寸', 8),
(68, 7, '75寸', 9),
(69, 7, '85寸', 10);

-- 口味 (spec_id=8) - 食品
INSERT INTO tb_specification_option (id, spec_id, option_name, orders) VALUES
(70, 8, '原味', 1),
(71, 8, '香辣', 2),
(72, 8, '麻辣', 3),
(73, 8, '五香', 4),
(74, 8, '孜然', 5),
(75, 8, '蒜香', 6),
(76, 8, '椒盐', 7),
(77, 8, '番茄', 8),
(78, 8, '烧烤', 9);

-- 规格 (spec_id=9) - 通用规格
INSERT INTO tb_specification_option (id, spec_id, option_name, orders) VALUES
(80, 9, '单件装', 1),
(81, 9, '套装', 2),
(82, 9, '家庭装', 3),
(83, 9, '礼盒装', 4),
(84, 9, '大礼包', 5),
(85, 9, '小包装', 6),
(86, 9, '大包装', 7);

-- 材质 (spec_id=10)
INSERT INTO tb_specification_option (id, spec_id, option_name, orders) VALUES
(90, 10, '棉', 1),
(91, 10, '涤纶', 2),
(92, 10, '丝绸', 3),
(93, 10, '羊毛', 4),
(94, 10, '真皮', 5),
(95, 10, 'PU皮', 6),
(96, 10, '帆布', 7),
(97, 10, '麻', 8),
(98, 10, '雪纺', 9);

-- 套装 (spec_id=11)
INSERT INTO tb_specification_option (id, spec_id, option_name, orders) VALUES
(100, 11, '单件', 1),
(101, 11, '两件套', 2),
(102, 11, '三件套', 3),
(103, 11, '四件套', 4),
(104, 11, '五件套', 5);

-- 适用人群 (spec_id=12)
INSERT INTO tb_specification_option (id, spec_id, option_name, orders) VALUES
(110, 12, '男士', 1),
(111, 12, '女士', 2),
(112, 12, '儿童', 3),
(113, 12, '婴儿', 4),
(114, 12, '老人', 5),
(115, 12, '通用', 6);

-- 型号 (spec_id=13)
INSERT INTO tb_specification_option (id, spec_id, option_name, orders) VALUES
(120, 13, '标准型', 1),
(121, 13, '加强型', 2),
(122, 13, '豪华型', 3),
(123, 13, '旗舰型', 4);

-- 直径 (spec_id=14) - 锅具专用
INSERT INTO tb_specification_option (id, spec_id, option_name, orders) VALUES
(130, 14, '20cm', 1),
(131, 14, '24cm', 2),
(132, 14, '26cm', 3),
(133, 14, '28cm', 4),
(134, 14, '30cm', 5),
(135, 14, '32cm', 6),
(136, 14, '34cm', 7),
(137, 14, '36cm', 8);

-- 适用灶具 (spec_id=15)
INSERT INTO tb_specification_option (id, spec_id, option_name, orders) VALUES
(140, 15, '燃气灶', 1),
(141, 15, '电磁炉', 2),
(142, 15, '电陶炉', 3),
(143, 15, '电热炉', 4),
(144, 15, '通用', 5);


-- =============================================
-- 3. 类型模板表 (tb_type_template)
-- =============================================

-- 手机数码模板
INSERT INTO tb_type_template (id, name, spec_ids, brand_ids, custom_attribute_items) VALUES
(1, '手机', '[{"id":1},{"id":2},{"id":4},{"id":3},{"id":5}]', '[{"id":1,"text":"苹果"},{"id":2,"text":"华为"},{"id":3,"text":"小米"},{"id":4,"text":"OPPO"},{"id":5,"text":"vivo"},{"id":6,"text":"三星"},{"id":7,"text":"荣耀"}]', '[{"text":"产地","value":"中国大陆"},{"text":"保修期","value":"1年"}]'),
(2, '平板电脑', '[{"id":2},{"id":4},{"id":3},{"id":5}]', '[{"id":1,"text":"苹果"},{"id":2,"text":"华为"},{"id":3,"text":"小米"},{"id":8,"text":"联想"}]', '[{"text":"屏幕尺寸","value":""},{"text":"保修期","value":"1年"}]'),
(3, '笔记本电脑', '[{"id":2},{"id":4},{"id":3},{"id":5}]', '[{"id":1,"text":"苹果"},{"id":2,"text":"华为"},{"id":8,"text":"联想"},{"id":9,"text":"戴尔"},{"id":10,"text":"华硕"},{"id":11,"text":"惠普"}]', '[{"text":"屏幕尺寸","value":""},{"text":"显卡","value":""}]'),
(4, '智能手表', '[{"id":3},{"id":5}]', '[{"id":1,"text":"苹果"},{"id":2,"text":"华为"},{"id":3,"text":"小米"}]', '[{"text":"防水等级","value":"IP68"},{"text":"续航","value":""}]'),

-- 家电模板
(10, '电视机', '[{"id":7},{"id":3}]', '[{"id":12,"text":"海尔"},{"id":13,"text":"海信"},{"id":14,"text":"TCL"},{"id":15,"text":"创维"},{"id":16,"text":"小米"}]', '[{"text":"分辨率","value":"4K"},{"text":"能效等级","value":"一级"}]'),
(11, '空调', '[{"id":7},{"id":3},{"id":13}]', '[{"id":12,"text":"海尔"},{"id":17,"text":"格力"},{"id":18,"text":"美的"},{"id":19,"text":"奥克斯"}]', '[{"text":"匹数","value":""},{"text":"能效等级","value":"一级"}]'),
(12, '冰箱', '[{"id":7},{"id":3},{"id":13}]', '[{"id":12,"text":"海尔"},{"id":17,"text":"格力"},{"id":18,"text":"美的"},{"id":20,"text":"西门子"}]', '[{"text":"容量","value":""},{"text":"制冷方式","value":"风冷"}]'),
(13, '洗衣机', '[{"id":7},{"id":3},{"id":13}]', '[{"id":12,"text":"海尔"},{"id":18,"text":"美的"},{"id":20,"text":"西门子"},{"id":21,"text":"松下"}]', '[{"text":"洗涤容量","value":""},{"text":"电机类型","value":"变频"}]'),

-- 服装模板
(20, '男装', '[{"id":6},{"id":3},{"id":10},{"id":11}]', '[{"id":22,"text":"优衣库"},{"id":23,"text":"ZARA"},{"id":24,"text":"H&M"},{"id":25,"text":"海澜之家"}]', '[{"text":"面料","value":""},{"text":"季节","value":""}]'),
(21, '女装', '[{"id":6},{"id":3},{"id":10},{"id":11}]', '[{"id":22,"text":"优衣库"},{"id":23,"text":"ZARA"},{"id":26,"text":"ONLY"},{"id":27,"text":"VERO MODA"}]', '[{"text":"面料","value":""},{"text":"风格","value":""}]'),
(22, '童装', '[{"id":6},{"id":3},{"id":10}]', '[{"id":28,"text":"巴拉巴拉"},{"id":29,"text":"安奈儿"},{"id":30,"text":"小猪班纳"}]', '[{"text":"适合年龄","value":""},{"text":"面料","value":"纯棉"}]'),

-- 食品模板
(30, '零食', '[{"id":8},{"id":9}]', '[{"id":31,"text":"三只松鼠"},{"id":32,"text":"良品铺子"},{"id":33,"text":"百草味"}]', '[{"text":"保质期","value":""},{"text":"产地","value":""}]'),
(31, '生鲜', '[{"id":9},{"id":8}]', '[]', '[{"text":"保质期","value":""},{"text":"产地","value":""}]'),
(32, '酒水', '[{"id":9},{"id":8}]', '[{"id":34,"text":"茅台"},{"id":35,"text":"五粮液"},{"id":36,"text":"青岛啤酒"}]', '[{"text":"酒精度","value":""},{"text":"产地","value":""}]'),

-- 美妆模板
(40, '护肤', '[{"id":9},{"id":3}]', '[{"id":37,"text":"兰蔻"},{"id":38,"text":"雅诗兰黛"},{"id":39,"text":"SK-II"},{"id":40,"text":"资生堂"}]', '[{"text":"肤质","value":"所有肤质"},{"text":"功效","value":""}]'),
(41, '彩妆', '[{"id":3},{"id":9}]', '[{"id":41,"text":"MAC"},{"id":42,"text":"迪奥"},{"id":43,"text":"YSL"},{"id":44,"text":"阿玛尼"}]', '[{"text":"适合肤质","value":""}]'),

-- 母婴模板
(50, '奶粉', '[{"id":9},{"id":12}]', '[{"id":45,"text":"飞鹤"},{"id":46,"text":"美赞臣"},{"id":47,"text":"惠氏"},{"id":48,"text":"爱他美"}]', '[{"text":"适用阶段","value":""},{"text":"产地","value":""}]'),
(51, '纸尿裤', '[{"id":9},{"id":12}]', '[{"id":49,"text":"帮宝适"},{"id":50,"text":"好奇"},{"id":51,"text":"花王"},{"id":52,"text":"大王"}]', '[{"text":"适用体重","value":""}]'),

-- 运动模板
(60, '运动鞋', '[{"id":6},{"id":3}]', '[{"id":53,"text":"耐克"},{"id":54,"text":"阿迪达斯"},{"id":55,"text":"李宁"},{"id":56,"text":"安踏"}]', '[{"text":"适用场景","value":""}]'),
(61, '运动服', '[{"id":6},{"id":3},{"id":10}]', '[{"id":53,"text":"耐克"},{"id":54,"text":"阿迪达斯"},{"id":55,"text":"李宁"},{"id":56,"text":"安踏"}]', '[{"text":"面料","value":""}]'),

-- 厨具模板
(70, '炒锅', '[{"id":14},{"id":10},{"id":15},{"id":3}]', '[{"id":60,"text":"苏泊尔"},{"id":61,"text":"美的"},{"id":62,"text":"九阳"},{"id":63,"text":"炊大皇"},{"id":64,"text":"爱仕达"},{"id":65,"text":"双立人"},{"id":66,"text":"菲仕乐"},{"id":67,"text":"WMF"}]', '[{"text":"产地","value":"中国大陆"},{"text":"涂层","value":"不粘涂层"}]'),
(71, '平底锅', '[{"id":14},{"id":10},{"id":15},{"id":3}]', '[{"id":60,"text":"苏泊尔"},{"id":61,"text":"美的"},{"id":62,"text":"九阳"},{"id":63,"text":"炊大皇"},{"id":64,"text":"爱仕达"},{"id":65,"text":"双立人"},{"id":66,"text":"菲仕乐"},{"id":67,"text":"WMF"}]', '[{"text":"产地","value":"中国大陆"},{"text":"涂层","value":"不粘涂层"}]'),
(72, '汤锅', '[{"id":14},{"id":10},{"id":15},{"id":3}]', '[{"id":60,"text":"苏泊尔"},{"id":61,"text":"美的"},{"id":62,"text":"九阳"},{"id":63,"text":"炊大皇"},{"id":64,"text":"爱仕达"}]', '[{"text":"容量","value":""},{"text":"材质","value":"不锈钢"}]'),
(73, '蒸锅', '[{"id":14},{"id":10},{"id":15},{"id":3}]', '[{"id":60,"text":"苏泊尔"},{"id":61,"text":"美的"},{"id":63,"text":"炊大皇"},{"id":64,"text":"爱仕达"}]', '[{"text":"层数","value":"2层"}]');


-- =============================================
-- 4. 更新分类表关联模板 (tb_item_cat)
-- 根据实际分类ID更新，以下为示例
-- =============================================

-- 假设分类ID对应关系，请根据实际情况调整
-- 手机通讯 -> 手机模板(id=1)
-- UPDATE tb_item_cat SET type_id = 1 WHERE name = '手机通讯';
-- 平板电脑 -> 平板模板(id=2)
-- UPDATE tb_item_cat SET type_id = 2 WHERE name = '平板电脑';
-- 空调 -> 空调模板(id=11)
-- UPDATE tb_item_cat SET type_id = 11 WHERE name = '空调';

-- =============================================
-- 5. 分类关联模板（根据分类名称匹配）
-- =============================================

-- 手机数码
UPDATE tb_item_cat SET type_id = 1 WHERE name = '手机';
UPDATE tb_item_cat SET type_id = 2 WHERE name = '平板电脑';
UPDATE tb_item_cat SET type_id = 3 WHERE name = '笔记本';
UPDATE tb_item_cat SET type_id = 4 WHERE name = '智能手表';

-- 家电
UPDATE tb_item_cat SET type_id = 10 WHERE name = '电视机';
UPDATE tb_item_cat SET type_id = 11 WHERE name = '空调';
UPDATE tb_item_cat SET type_id = 12 WHERE name = '冰箱';
UPDATE tb_item_cat SET type_id = 13 WHERE name = '洗衣机';

-- 服装
UPDATE tb_item_cat SET type_id = 20 WHERE name = '男装';
UPDATE tb_item_cat SET type_id = 21 WHERE name = '女装';
UPDATE tb_item_cat SET type_id = 22 WHERE name = '童装';

-- 食品
UPDATE tb_item_cat SET type_id = 30 WHERE name = '零食';
UPDATE tb_item_cat SET type_id = 31 WHERE name = '生鲜';
UPDATE tb_item_cat SET type_id = 32 WHERE name = '酒水';

-- 美妆
UPDATE tb_item_cat SET type_id = 40 WHERE name = '护肤';
UPDATE tb_item_cat SET type_id = 41 WHERE name = '彩妆';

-- 母婴
UPDATE tb_item_cat SET type_id = 50 WHERE name = '奶粉';
UPDATE tb_item_cat SET type_id = 51 WHERE name = '纸尿裤';

-- 运动
UPDATE tb_item_cat SET type_id = 60 WHERE name = '运动鞋';
UPDATE tb_item_cat SET type_id = 61 WHERE name = '运动服';

-- 厨具
UPDATE tb_item_cat SET type_id = 70 WHERE name = '炒锅';
UPDATE tb_item_cat SET type_id = 72 WHERE name = '汤锅';
UPDATE tb_item_cat SET type_id = 73 WHERE name = '蒸锅';

-- =============================================
-- 查询验证
-- =============================================
-- SELECT * FROM tb_specification;
-- SELECT * FROM tb_specification_option ORDER BY spec_id, orders;
-- SELECT * FROM tb_type_template;
