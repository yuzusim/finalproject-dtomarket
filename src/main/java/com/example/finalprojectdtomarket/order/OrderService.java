package com.example.finalprojectdtomarket.order;

import com.example.finalprojectdtomarket._core.errors.exception.Exception404;
import com.example.finalprojectdtomarket.cart.Cart;
import com.example.finalprojectdtomarket.cart.CartJPARepository;
import com.example.finalprojectdtomarket.cart.CartResponse;
import com.example.finalprojectdtomarket.orderItem.OrderItem;
import com.example.finalprojectdtomarket.orderItem.OrderItemJPARepository;
import com.example.finalprojectdtomarket.product.Product;
import com.example.finalprojectdtomarket.product.ProductJPARepository;
import com.example.finalprojectdtomarket.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderService {
    private final OrderJPARepository orderJPARepository;
    private final CartJPARepository cartJPARepository;
    private final OrderItemJPARepository orderItemJPARepository;
    private final ProductJPARepository productJPARepository;

    public List<OrderResponse.orderItemDTO> orderList(Integer sessionUserId) {
        //여러번 도는 애 조회
        List<OrderItem> orderItemList = orderJPARepository.findOrderList(sessionUserId);
        System.out.println("test " + orderItemList);

        // 1번 도는 애 조회
        // orderId가 중복되어서 촤차아악 나오길래 중복제거 (대표 물품만 1개 나오게)
        Map<Integer, OrderItem> orderDistinct =
                orderItemList.stream().collect(Collectors.toMap(
                        list -> list.getOrder().getId(),  //orderId가 키값
                        list -> list,           // 값
                        (first, second) -> first    //같은 키를 가진 요소가 있으면 첫번째 값 사용
                ));

        System.out.println("오더디스팅트" + orderDistinct);

        // Map의 values 컬렉션을 List로 변환하여 반환
        List<OrderItem> distinctOrderList = new ArrayList<>(orderDistinct.values());
        // 주문 ID(orderId)를 기준으로 내림차순 정렬
        distinctOrderList.sort((order1, order2) -> order2.getOrder().getId().compareTo(order1.getOrder().getId()));
        System.out.println("중복처리 됐니" + distinctOrderList);

        List<OrderResponse.orderItemDTO> resultList = new ArrayList<>();
        for (OrderItem orderItem : distinctOrderList) {
            resultList.add(new OrderResponse.orderItemDTO(orderItem, orderItemList));
        }

        System.out.println("리절트" + resultList);

        return resultList;

    }

    public List<OrderResponse.orderItemDTO> adminList() {
        //여러번 도는 애 조회
        List<OrderItem> orderItemList = orderItemJPARepository.findAll();
        System.out.println("test " + orderItemList);

        // 1번 도는 애 조회
        // orderId가 중복되어서 촤차아악 나오길래 중복제거 (대표 물품만 1개 나오게)
        Map<Integer, OrderItem> orderDistinct =
                orderItemList.stream().collect(Collectors.toMap(
                        list -> list.getOrder().getId(),  //orderId가 키값
                        list -> list,           // 값
                        (first, second) -> first    //같은 키를 가진 요소가 있으면 첫번째 값 사용
                ));

        System.out.println("오더디스팅트" + orderDistinct);

        // Map의 values 컬렉션을 List로 변환하여 반환
        List<OrderItem> distinctOrderList = new ArrayList<>(orderDistinct.values());
        // 주문 ID(orderId)를 기준으로 내림차순 정렬
        distinctOrderList.sort((order1, order2) -> order2.getOrder().getId().compareTo(order1.getOrder().getId()));
        System.out.println("중복처리 됐니" + distinctOrderList);

        List<OrderResponse.orderItemDTO> resultList = new ArrayList<>();
        for (OrderItem orderItem : distinctOrderList) {
            resultList.add(new OrderResponse.orderItemDTO(orderItem, orderItemList));
        }

        System.out.println("리절트" + resultList);

        return resultList;

    }

    //order-save-form
    @Transactional
    public List<CartResponse.ListDTO> orderCartList(Integer userId) {
        Boolean isChecked = true;
        //user는 sessionUser, isChecked는 true인 list 조회
        List<Cart> carts = cartJPARepository.findByUserIdAndChecked(userId, isChecked);
        List<CartResponse.ListDTO> cartList = carts.stream().map(cart -> new CartResponse.ListDTO(cart)).toList();

        Integer indexNum = 1;
        for (CartResponse.ListDTO list : cartList) {
            list.setIndexNum(indexNum++);
        }

        //카트 롤백
        for (Cart cart : carts) {
            cart.setIsChecked(false);
        }

        return cartList;
    }

    //구매하기
    @Transactional
    public void saveOrder(OrderRequest.SaveDTO requestDTO, User user) {
        System.out.println("값확인" + requestDTO);

        //오더 저장 //TODO : save 부분
        Order order = orderJPARepository.save(requestDTO.toOrderEntity(user));

        //오더 아이템 저장
        for (int i = 0; i < requestDTO.getProductId().size(); i++) {
            Product product = productJPARepository.findById(requestDTO.getProductId().get(i))
                    .orElseThrow(() -> new Exception404("상품을 찾을 수 없습니다."));

            Integer quantity = requestDTO.getOrderQty().get(i);

            orderItemJPARepository.save(requestDTO.toOrderItemEntity(order, product, quantity));

//            order.addOrderItem(requestDTO.toOrderItemEntity(order, product, quantity));

            //상품 수량 변경 //더티체킹
            product.setQty(product.getQty() - quantity);

            //선택한 카트 딜리트
            cartJPARepository.deleteByCartId(requestDTO.getCartId().get(i));

        }
    }

    @Transactional
    public void orderCancel(List<OrderRequest.CancelDTO> requestDTO) {
        OrderStatus status;

        //값이 1개밖에 안들어오니까 (중복제거 해놔서) 해당 값으로 orderItem을 찾음
        for (int i = 0; i < requestDTO.size(); i++) {
            List<OrderItem> orderItemList = orderItemJPARepository.findByOrderId(requestDTO.get(i).getOrderId());
            System.out.println("아이템리스트 " + orderItemList);

            status = OrderStatus.ORDER_CANCEL;

            //안쪽에서 for문 돌려야함
            for (OrderItem orderItem : orderItemList) {
                // order 테이블 상태변경
                orderItem.getOrder().setStatus(status);

                // product 테이블 재고 변경
                Integer productQty = orderItem.getProduct().getQty();
                orderItem.getProduct().setQty(productQty + orderItem.getOrderQty());
            }

        }
    }
}
