package com.example.finalprojectdtomarket.cart;

import com.example.finalprojectdtomarket.product.Product;
import lombok.Data;

public class CartResponse {

    @Data
    public static class ListDTO {
        private Integer id;     //cartId
        private Integer orderQty;       //구매수량

        private String img;
        private Integer productId;
        private String pName;       //상품명
        private Integer price;      //상품단가

        private Integer sum;        //가공하기
        private boolean isChecked;
        private Integer indexNum;   //번호 이쁘게 가공용

        public ListDTO(Cart cart) {
            this.id = cart.getId();
            this.orderQty = cart.getOrderQty();
            this.productId = cart.getProduct().getId();
            this.img = cart.getProduct().getImg();
            this.isChecked  = false;
            this.pName = cart.getProduct().getName();
            this.price = cart.getProduct().getPrice();
            setSum();
        }

        //sum 가공용!! 단가 * 구매수량용
        public Integer setSum() {
            sum = price * orderQty;
            return sum;
        }


    }


}