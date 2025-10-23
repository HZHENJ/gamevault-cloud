package com.sg.nusiss.shopping.controller.library;

import com.sg.nusiss.shopping.dto.library.*;
import com.sg.nusiss.shopping.entity.ENUM.PaymentMethod;
import com.sg.nusiss.shopping.entity.shopping.*;
import com.sg.nusiss.shopping.repository.library.*;
import com.sg.nusiss.shopping.service.shopping.CartService;
import com.sg.nusiss.shopping.service.shopping.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统一使用 Spring Security 的 Jwt 注入方式，与队友风格保持一致
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;
    private final OrderItemRepository orderItemRepo;
    private final PurchasedGameActivationCodeRepository pacRepo;

    @PostMapping("/checkout")
    public ResponseEntity<OrderDTO> checkout(@AuthenticationPrincipal Jwt jwt,
                                             @RequestParam(value = "method") PaymentMethod method) {

        Long userId = ((Number) jwt.getClaims().get("uid")).longValue();
        var cart = cartService.getCartEntity(userId);
        var dto  = orderService.createPendingOrder(cart, method);
        cartService.markCheckedOut(userId, method);

        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getOrders(@AuthenticationPrincipal Jwt jwt) {
        Long userId = ((Number) jwt.getClaims().get("uid")).longValue();
        return ResponseEntity.ok(orderService.findByUserId(userId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderDetail(@PathVariable(value = "orderId") Long orderId,
                                                              @AuthenticationPrincipal Jwt jwt) {
        Long uid = ((Number) jwt.getClaims().get("uid")).longValue();

        List<OrderItem> items = orderItemRepo.findByUserIdOrderByOrderDateDesc(uid).stream()
                .filter(oi -> oi.getOrderId().equals(orderId))
                .collect(Collectors.toList());
        if (items.isEmpty()) return ResponseEntity.notFound().build();

        List<String> allActivationCodes = items.stream()
                .flatMap(oi -> pacRepo.findByOrderItemId(oi.getOrderItemId()).stream())
                .map(x -> x.getActivationCode())
                .collect(Collectors.toList());

        OrderItem first = items.get(0);
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("createdAt", first.getOrderDate());
        result.put("status", first.getOrderStatus());
        result.put("total", items.stream()
                .map(x -> x.getDiscountPrice() != null ? x.getDiscountPrice() : x.getUnitPrice())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        result.put("items", items.stream().map(oi -> {
            OrderDetailDTO d = new OrderDetailDTO();
            d.orderItemId = oi.getOrderItemId();
            d.orderId = oi.getOrderId();
            d.userId = oi.getUserId();
            d.gameId = oi.getGameId();
            d.unitPrice = oi.getUnitPrice();
            d.discountPrice = oi.getDiscountPrice();
            d.orderDate = oi.getOrderDate();
            d.orderStatus = String.valueOf(oi.getOrderStatus());
            d.activationCodes = pacRepo.findByOrderItemId(oi.getOrderItemId()).stream()
                    .map(x -> x.getActivationCode()).collect(Collectors.toList());
            return d;
        }).collect(Collectors.toList()));
        result.put("activationCodes", allActivationCodes);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(@AuthenticationPrincipal Jwt jwt) {
        Long uid = ((Number) jwt.getClaims().get("uid")).longValue();
        var grouped = orderItemRepo.findByUserIdOrderByOrderDateDesc(uid).stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId, Collectors.toList()));

        List<OrderGroupSummaryDTO> list = grouped.entrySet().stream().map(e -> {
                    var arr = e.getValue();
                    OrderGroupSummaryDTO s = new OrderGroupSummaryDTO();
                    s.orderId = e.getKey();
                    s.createdAt = arr.get(0).getOrderDate();
                    s.status = String.valueOf(arr.get(0).getOrderStatus());
                    s.total = arr.stream()
                            .map(x -> x.getDiscountPrice() != null ? x.getDiscountPrice() : x.getUnitPrice())
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                    return s;
                }).sorted((a, b) -> b.createdAt.compareTo(a.createdAt))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("items", list));
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderDTO> pay(@PathVariable(value = "orderId") Long orderId,
                                        @AuthenticationPrincipal Jwt jwt) {
        Long userId = ((Number) jwt.getClaims().get("uid")).longValue();
        return ResponseEntity.ok(orderService.captureAndFulfill(orderId, userId));
    }

    @PostMapping("/{orderId}/fail")
    public ResponseEntity<Void> fail(@PathVariable(value = "orderId") Long orderId,
                                     @AuthenticationPrincipal Jwt jwt) {
        Long userId = ((Number) jwt.getClaims().get("uid")).longValue();
        orderService.markFailed(orderId, userId);
        return ResponseEntity.ok().build();
    }
}
