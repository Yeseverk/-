package com.cjc.vo;

import com.cjc.pojo.TbAddress;
import com.cjc.pojo.TbOrder;
import com.cjc.pojo.TbOrderItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单预览VO
 */
public class OrderPreviewVo {

    // 待结算商品列表
    private List<OrderItemPreviewVo> cartList;

    // 收货地址
    private AddressVo address;

    // 商品总价
    private BigDecimal totalPrice;

    // 邮费
    private BigDecimal postFee;

    // 实付金额
    private BigDecimal payment;

    public List<OrderItemPreviewVo> getCartList() {
        return cartList;
    }

    public void setCartList(List<OrderItemPreviewVo> cartList) {
        this.cartList = cartList;
    }

    public AddressVo getAddress() {
        return address;
    }

    public void setAddress(AddressVo address) {
        this.address = address;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public BigDecimal getPostFee() {
        return postFee;
    }

    public void setPostFee(BigDecimal postFee) {
        this.postFee = postFee;
    }

    public BigDecimal getPayment() {
        return payment;
    }

    public void setPayment(BigDecimal payment) {
        this.payment = payment;
    }

    /**
     * 订单商品预览项
     */
    public static class OrderItemPreviewVo {
        private Long cartId;          // 购物车ID
        private Long goodsId;         // 商品ID
        private Long itemId;          // SKU ID
        private String goodsName;     // 商品名称
        private String image;         // 商品图片
        private BigDecimal price;     // 价格
        private Integer num;          // 数量
        private String spec;          // 规格
        private String sellerId;      // 商家ID
        private BigDecimal totalPrice; // 小计
        private Integer stockCount;   // 库存（用于校验）

        public Long getCartId() {
            return cartId;
        }

        public void setCartId(Long cartId) {
            this.cartId = cartId;
        }

        public Long getGoodsId() {
            return goodsId;
        }

        public void setGoodsId(Long goodsId) {
            this.goodsId = goodsId;
        }

        public Long getItemId() {
            return itemId;
        }

        public void setItemId(Long itemId) {
            this.itemId = itemId;
        }

        public String getGoodsName() {
            return goodsName;
        }

        public void setGoodsName(String goodsName) {
            this.goodsName = goodsName;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getNum() {
            return num;
        }

        public void setNum(Integer num) {
            this.num = num;
        }

        public String getSpec() {
            return spec;
        }

        public void setSpec(String spec) {
            this.spec = spec;
        }

        public String getSellerId() {
            return sellerId;
        }

        public void setSellerId(String sellerId) {
            this.sellerId = sellerId;
        }

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }

        public Integer getStockCount() {
            return stockCount;
        }

        public void setStockCount(Integer stockCount) {
            this.stockCount = stockCount;
        }
    }
}