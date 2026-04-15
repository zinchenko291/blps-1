package me.zinch.itmo.mts.web.order;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import me.zinch.itmo.mts.domain.entity.Order;
import me.zinch.itmo.mts.service.auth.SessionAuthService;
import me.zinch.itmo.mts.service.auth.SessionUser;
import me.zinch.itmo.mts.service.order.OrderService;
import me.zinch.itmo.mts.service.order.dto.CreateOrderRequest;
import me.zinch.itmo.mts.web.order.dto.AssignManagerApiRequest;
import me.zinch.itmo.mts.web.order.dto.ChangeOrderStatusApiRequest;
import me.zinch.itmo.mts.web.order.dto.CreateOrderApiRequest;
import me.zinch.itmo.mts.web.order.dto.OrderApiResponse;
import me.zinch.itmo.mts.web.order.mapper.OrderMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final SessionAuthService sessionAuthService;

    public OrderController(
            OrderService orderService,
            OrderMapper orderMapper,
            SessionAuthService sessionAuthService
    ) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.sessionAuthService = sessionAuthService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createOrder(@Valid @RequestBody CreateOrderApiRequest request) {
        orderService.createOrder(new CreateOrderRequest(
                request.getCustomerName(),
                request.getPhoneNumber(),
                request.getEmail(),
                orderMapper.toServiceItems(request.getItems())
        ));
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<OrderApiResponse> getOrders(HttpSession session) {
        SessionUser user = sessionAuthService.requireAuthenticated(session);
        List<Order> orders = orderService.getOrdersForUser(user.id());
        return orders.stream().map(orderMapper::toResponse).toList();
    }

    @PostMapping("/{orderId}/manager")
    public OrderApiResponse assignManager(
            @PathVariable java.util.UUID orderId,
            @Valid @RequestBody AssignManagerApiRequest request,
            HttpSession session
    ) {
        SessionUser user = sessionAuthService.requireAuthenticated(session);
        Order updated = orderService.assignManager(orderId, user.id(), request.getManagerId());
        return orderMapper.toResponse(updated);
    }

    @PatchMapping("/{orderId}/status")
    public OrderApiResponse changeStatus(
            @PathVariable java.util.UUID orderId,
            @Valid @RequestBody ChangeOrderStatusApiRequest request,
            HttpSession session
    ) {
        SessionUser user = sessionAuthService.requireAuthenticated(session);
        Order updated = orderService.changeStatus(orderId, user.id(), request.getStatus());
        return orderMapper.toResponse(updated);
    }
}
