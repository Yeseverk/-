<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${goods.goodsName} - 商品详情</title>
    <style>
        /* 基础样式 */
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif; background: #f5f7fa; color: #333; }
        .container { max-width: 1200px; margin: 0 auto; padding: 20px 15px; }

        /* 面包屑 */
        .breadcrumb { font-size: 14px; color: #999; margin-bottom: 20px; }
        .breadcrumb a { color: #999; text-decoration: none; }
        .breadcrumb a:hover { color: #ff4b2b; }
        .breadcrumb span { color: #333; }

        /* 主信息区 */
        .main-info { display: flex; gap: 30px; background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 12px rgba(0,0,0,0.04); }

        /* 图片区 */
        .gallery { width: 420px; flex-shrink: 0; }
        .main-image { width: 100%; aspect-ratio: 1; border-radius: 12px; overflow: hidden; background: #f8f8f8; }
        .main-image img { width: 100%; height: 100%; object-fit: cover; }

        /* 信息区 */
        .info-section { flex: 1; min-width: 0; }
        .title { font-size: 22px; color: #333; margin-bottom: 8px; line-height: 1.4; }
        .caption { font-size: 14px; color: #666; margin-bottom: 16px; }

        /* 价格 */
        .price-block { background: linear-gradient(135deg, #fff5f5 0%, #fff8f6 100%); border-radius: 8px; padding: 16px 20px; margin-bottom: 16px; }
        .price-label { font-size: 14px; color: #666; }
        .price { font-size: 28px; font-weight: 700; color: #ff4b2b; }
        .price i { font-style: normal; font-size: 18px; }

        /* 统计 */
        .stats { display: flex; gap: 24px; margin-bottom: 20px; }
        .stat-item { text-align: center; }
        .stat-value { font-size: 18px; font-weight: 600; color: #333; }
        .stat-label { font-size: 12px; color: #999; margin-top: 4px; }

        /* 规格选择 */
        .spec-section { margin-bottom: 20px; }
        .spec-row { display: flex; align-items: flex-start; margin-bottom: 12px; }
        .spec-label { width: 60px; font-size: 14px; color: #666; padding-top: 8px; }
        .spec-options { flex: 1; display: flex; flex-wrap: wrap; gap: 10px; }
        .spec-btn { padding: 8px 16px; border: 1px solid #ebeef5; border-radius: 6px; background: #fff; color: #333; font-size: 14px; cursor: pointer; transition: all 0.2s; }
        .spec-btn:hover { border-color: #ff4b2b; }
        .spec-btn.active { border-color: #ff4b2b; background: rgba(255,75,43,0.08); color: #ff4b2b; }
        .spec-btn.disabled { border-color: #eee; color: #ccc; cursor: not-allowed; background: #f8f8f8; }

        /* 数量 */
        .quantity-section { display: flex; align-items: center; margin-bottom: 20px; }
        .quantity-label { width: 60px; font-size: 14px; color: #666; }
        .quantity-control { display: flex; align-items: center; gap: 10px; }
        .quantity-input { width: 60px; height: 36px; text-align: center; border: 1px solid #ebeef5; border-radius: 6px; font-size: 14px; }
        .stock-tip { font-size: 13px; color: #999; }

        /* 操作按钮 */
        .action-buttons { display: flex; gap: 12px; margin-bottom: 20px; }
        .btn-buy, .btn-cart { display: flex; align-items: center; justify-content: center; gap: 8px; padding: 14px 30px; border-radius: 8px; font-size: 16px; font-weight: 500; cursor: pointer; flex: 1; }
        .btn-buy { background: linear-gradient(135deg, #ff4b2b 0%, #ff416c 100%); color: #fff; border: none; }
        .btn-cart { background: rgba(255,75,43,0.08); color: #ff4b2b; border: 1px solid #ff4b2b; }

        /* 服务保障 */
        .service-info { display: flex; gap: 16px; }
        .service-item { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #666; }
        .service-item svg { width: 16px; height: 16px; color: #10b981; }

        /* 详情区 */
        .detail-section { background: #fff; border-radius: 12px; padding: 20px; margin-top: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.04); }
        .detail-tabs { display: flex; gap: 20px; border-bottom: 2px solid #ebeef5; margin-bottom: 20px; }
        .tab-btn { padding: 12px 0; font-size: 16px; color: #666; background: transparent; border: none; cursor: pointer; position: relative; }
        .tab-btn.active { color: #ff4b2b; font-weight: 500; }
        .tab-btn.active::after { content: ''; position: absolute; bottom: -2px; left: 0; right: 0; height: 2px; background: #ff4b2b; }

        /* 富文本 */
        .rich-content { font-size: 14px; color: #333; line-height: 1.8; }
        .rich-content img { max-width: 100%; height: auto; display: block; margin: 16px auto; border-radius: 8px; }
        .rich-content p { margin: 12px 0; }

        /* 无内容提示 */
        .no-content { text-align: center; padding: 60px 0; color: #999; }

        /* 响应式 */
        @media (max-width: 900px) { .main-info { flex-direction: column; } .gallery { width: 100%; } }
    </style>
</head>
<body>
    <div class="container">
        <!-- 面包屑 -->
        <div class="breadcrumb">
            <a href="/">首页</a> /
            <a href="/goods/list">商品列表</a> /
            <span>${goods.goodsName}</span>
        </div>

        <!-- 商品主信息 -->
        <div class="main-info">
            <!-- 图片区 -->
            <div class="gallery">
                <div class="main-image">
                    <#if goods.smallPic?? && goods.smallPic?length gt 0>
                    <img src="${goods.smallPic}" alt="${goods.goodsName}" id="mainImage">
                    <#else>
                    <img src="https://via.placeholder.com/400x400?text=No+Image" alt="暂无图片" id="mainImage">
                    </#if>
                </div>
            </div>

            <!-- 信息区 -->
            <div class="info-section">
                <h1 class="title">${goods.goodsName}</h1>
                <#if goods.caption?? && goods.caption?length gt 0>
                <p class="caption">${goods.caption}</p>
                </#if>

                <!-- 价格 -->
                <div class="price-block">
                    <span class="price-label">售价</span>
                    <span class="price"><i>¥</i><span id="currentPrice">${(goods.price!0)?string('#.##')}</span></span>
                </div>

                <!-- 统计 -->
                <div class="stats">
                    <div class="stat-item">
                        <span class="stat-value">${goods.salesCount!'0'}+</span>
                        <span class="stat-label">累计销量</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-value" id="stockNum">${defaultStock!'99'}</span>
                        <span class="stat-label">库存数量</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-value">${goods.commentCount!'0'}</span>
                        <span class="stat-label">用户评价</span>
                    </div>
                </div>

                <!-- SKU规格选择 -->
                <#if specList?? && specList?size gt 0>
                <div class="spec-section">
                    <#list specList as spec>
                    <div class="spec-row">
                        <span class="spec-label">${spec.specName}</span>
                        <div class="spec-options" data-spec-name="${spec.specName}">
                            <#list spec.options as option>
                            <button class="spec-btn" data-spec="${spec.specName}" data-value="${option}" onclick="selectSpec('${spec.specName}', '${option}')">
                                ${option}
                            </button>
                            </#list>
                        </div>
                    </div>
                    </#list>
                </div>
                </#if>

                <!-- 数量 -->
                <div class="quantity-section">
                    <span class="quantity-label">数量</span>
                    <div class="quantity-control">
                        <input type="number" id="quantity" value="1" min="1" class="quantity-input">
                        <span class="stock-tip">库存 <span id="maxStock">${defaultStock!'99'}</span> 件</span>
                    </div>
                </div>

                <!-- 操作按钮 -->
                <div class="action-buttons">
                    <button class="btn-buy" onclick="handleBuy()">立即购买</button>
                    <button class="btn-cart" onclick="handleAddCart()">加入购物车</button>
                </div>

                <!-- 服务保障 -->
                <div class="service-info">
                    <div class="service-item">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>
                        <span>正品保障</span>
                    </div>
                    <div class="service-item">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/></svg>
                        <span>7天退换</span>
                    </div>
                    <div class="service-item">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M13 10V3L4 14h7v7l9-11h-7z"/></svg>
                        <span>极速发货</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- 商品详情富文本 -->
        <div class="detail-section">
            <div class="detail-tabs">
                <button class="tab-btn active" onclick="switchTab('detail')">商品详情</button>
                <button class="tab-btn" onclick="switchTab('spec')">规格参数</button>
            </div>

            <div id="tab-detail" class="tab-content">
                <#if goodsDesc?? && goodsDesc.introduction?? && goodsDesc.introduction?length gt 0>
                <div class="rich-content">
                    ${goodsDesc.introduction}
                </div>
                <#else>
                <div class="no-content">
                    <p>暂无商品详情</p>
                </div>
                </#if>
            </div>

            <div id="tab-spec" class="tab-content" style="display: none;">
                <#if specList?? && specList?size gt 0>
                <div class="spec-table">
                    <#list specList as spec>
                    <div class="spec-row">
                        <span class="spec-label">${spec.specName}</span>
                        <span class="spec-values">${spec.options?join('、')}</span>
                    </div>
                    </#list>
                </div>
                <#else>
                <div class="no-content">
                    <p>暂无规格参数</p>
                </div>
                </#if>
            </div>
        </div>
    </div>

    <!-- SKU数据（嵌入JSON供JS使用） -->
    <#if skuDataJson?? && skuDataJson?length gt 0>
    <script id="sku-data" type="application/json">${skuDataJson}</script>
    </#if>

    <!-- 动态交互JS -->
    <script>
        // 商品ID和版本
        const GOODS_ID = "${goods.id}";
        const PAGE_VERSION = "${version}";
        
        // API基础路径（通过网关访问）
        const API_BASE_URL = "http://localhost:10010";

        // SKU数据
        let skuList = [];
        const skuDataEl = document.getElementById('sku-data');
        if (skuDataEl) {
            try {
                skuList = JSON.parse(skuDataEl.textContent);
            } catch (e) {
                console.error('解析SKU数据失败:', e);
            }
        }

        // 当前选中的规格
        let selectedSpecs = {};
        let currentSku = null;

        // 初始化默认SKU
        function initDefaultSku() {
            if (skuList.length > 0) {
                // 找到默认SKU或第一个SKU
                currentSku = skuList.find(item => item.isDefault === '1' || item.defaultFlag) || skuList[0];
                if (currentSku && currentSku.spec) {
                    try {
                        const specs = typeof currentSku.spec === 'string' ? JSON.parse(currentSku.spec) : currentSku.spec;
                        selectedSpecs = {...specs};
                        // 更新UI选中状态
                        for (const [key, value] of Object.entries(specs)) {
                            selectSpec(key, value, false);
                        }
                        updatePriceAndStock();
                    } catch (e) {}
                }
            }
        }

        // 选择规格
        function selectSpec(specName, value, updateUI = true) {
            selectedSpecs[specName] = value;

            if (updateUI) {
                // 更新按钮状态
                const buttons = document.querySelectorAll('.spec-btn[data-spec="' + specName + '"]');
                buttons.forEach(btn => {
                    btn.classList.remove('active');
                    if (btn.dataset.value === value) {
                        btn.classList.add('active');
                    }
                });

                updatePriceAndStock();
            } else {
                // 仅更新UI状态，不触发价格更新
                const buttons = document.querySelectorAll('.spec-btn[data-spec="' + specName + '"]');
                buttons.forEach(btn => {
                    btn.classList.remove('active');
                    if (btn.dataset.value === value) {
                        btn.classList.add('active');
                    }
                });
            }
        }

        // 更新价格和库存
        function updatePriceAndStock() {
            if (skuList.length === 0) return;

            // 检查是否所有规格都已选择
            const specNames = [...new Set(skuList.flatMap(item => {
                try {
                    const specs = typeof item.spec === 'string' ? JSON.parse(item.spec) : item.spec;
                    return Object.keys(specs);
                } catch (e) { return []; }
            }))];

            const allSelected = specNames.every(name => selectedSpecs[name]);

            if (!allSelected) {
                // 未完全选择，显示默认SKU
                currentSku = skuList.find(item => item.isDefault === '1' || item.defaultFlag) || skuList[0];
            } else {
                // 根据选择的规格匹配SKU
                currentSku = skuList.find(item => {
                    if (!item.spec) return false;
                    try {
                        const specs = typeof item.spec === 'string' ? JSON.parse(item.spec) : item.spec;
                        return Object.entries(selectedSpecs).every(([key, value]) => specs[key] === value);
                    } catch (e) { return false; }
                });
            }

            if (currentSku) {
                // 更新价格
                document.getElementById('currentPrice').textContent = currentSku.price.toFixed(2);
                // 更新库存
                const stock = currentSku.num || currentSku.stockCount || 99;
                document.getElementById('stockNum').textContent = stock;
                document.getElementById('maxStock').textContent = stock;
            }
        }

        // 切换Tab
        function switchTab(tabName) {
            document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
            document.querySelector('.tab-btn[onclick="switchTab(\'' + tabName + '\')"]').classList.add('active');
            document.querySelectorAll('.tab-content').forEach(content => content.style.display = 'none');
            document.getElementById('tab-' + tabName).style.display = 'block';
        }

        // 立即购买
        function handleBuy() {
            const token = localStorage.getItem('token');
            if (!token) {
                alert('请先登录后购买');
                window.location.href = '/login?redirect=' + encodeURIComponent(window.location.href);
                return;
            }
            // 跳转到订单确认页
            const quantity = document.getElementById('quantity').value;
            const skuId = currentSku ? currentSku.id : '';
            window.location.href = '/order/confirm?goodsId=' + GOODS_ID + '&skuId=' + skuId + '&quantity=' + quantity;
        }

        // 加入购物车
        async function handleAddCart() {
            const token = localStorage.getItem('token');
            if (!token) {
                alert('请先登录后加入购物车');
                window.location.href = '/login?redirect=' + encodeURIComponent(window.location.href);
                return;
            }

            const quantity = document.getElementById('quantity').value;
            const skuId = currentSku ? currentSku.id : '';

            try {
                const response = await fetch(API_BASE_URL + '/cart/add', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': token
                    },
                    body: JSON.stringify({
                        goodsId: GOODS_ID,
                        itemId: skuId,
                        num: parseInt(quantity)
                    })
                });

                const result = await response.json();
                if (result.code === '10000') {
                    alert('加入购物车成功！');
                } else {
                    alert(result.message || '加入购物车失败');
                }
            } catch (e) {
                console.error('加入购物车失败:', e);
                alert('加入购物车失败，请稍后重试');
            }
        }

        // 页面加载时初始化
        document.addEventListener('DOMContentLoaded', function() {
            initDefaultSku();
        });
    </script>
</body>
</html>