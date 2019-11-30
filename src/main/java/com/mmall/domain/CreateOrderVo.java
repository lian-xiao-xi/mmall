package com.mmall.domain;

import java.util.List;

public class CreateOrderVo {
    public class ProductInfo {
        private Integer quantity;
        private Integer productId;

        public Integer getQuantity() {
            return quantity;
        }

        public Integer getProductId() {
            return productId;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public void setProductId(Integer productId) {
            this.productId = productId;
        }
    }

    private Integer shippingId;
    private List<ProductInfo> productList;

    public Integer getShippingId() {
        return shippingId;
    }

    public void setShippingId(Integer shippingId) {
        this.shippingId = shippingId;
    }

    public List<ProductInfo> getProductList() {
        return productList;
    }

    public void setProductList(List<ProductInfo> productList) {
        this.productList = productList;
    }
}
