package me.zinch.itmo.mts.web.order;

import me.zinch.itmo.mts.domain.entity.Order;
import me.zinch.itmo.mts.service.order.OrderService;
import me.zinch.itmo.mts.service.order.dto.CreateOrderRequest;
import me.zinch.itmo.mts.service.order.dto.UpdateOrderRequest;
import me.zinch.itmo.mts.service.auth.SessionAuthService;
import me.zinch.itmo.mts.service.auth.SessionUser;
import me.zinch.itmo.mts.web.order.dto.AssignManagerApiRequest;
import me.zinch.itmo.mts.web.order.dto.ChangeOrderStatusApiRequest;
import me.zinch.itmo.mts.web.order.dto.CreateOrderApiRequest;
import me.zinch.itmo.mts.web.order.dto.OrderApiResponse;
import me.zinch.itmo.mts.web.order.dto.UpdateOrderApiRequest;
import me.zinch.itmo.mts.web.order.mapper.OrderMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @Operation(
            summary = "Create order",
            description = "Who can call: anonymous buyer (no manager session required)."
    )
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
    @Operation(
            summary = "Get orders list",
            description = "Who can call: MANAGER or SENIOR_MANAGER from active session. SENIOR_MANAGER sees all orders, MANAGER sees only assigned orders."
    )
    public List<OrderApiResponse> getOrders(HttpSession session) {
        SessionUser user = sessionAuthService.requireAuthenticated(session);
        List<Order> orders = orderService.getOrdersForUser(user.id());
        return orders.stream().map(orderMapper::toResponse).toList();
    }

    @PostMapping("/{orderId}/manager")
    @Operation(
            summary = "Assign manager to order",
            description = "Who can call: only SENIOR_MANAGER from active session."
    )
    public OrderApiResponse assignManager(
            @PathVariable java.util.UUID orderId,
            @Valid @RequestBody AssignManagerApiRequest request,
            HttpSession session
    ) {
        SessionUser user = sessionAuthService.requireAuthenticated(session);
        Order updated = orderService.assignManager(orderId, user.id(), request.getManagerId());
        return orderMapper.toResponse(updated);
    }

//    @PutMapping("/{orderId}")
//    @Operation(
//            summary = "Edit order",
//            description = "Who can call: only MANAGER from active session, and only for orders assigned to this manager."
//    )
//    public OrderApiResponse editOrder(
//            @PathVariable java.util.UUID orderId,
//            @Valid @RequestBody UpdateOrderApiRequest request,
//            HttpSession session
//    ) {
//        SessionUser user = sessionAuthService.requireAuthenticated(session);
//        Order updated = orderService.editOrder(orderId, user.id(), new UpdateOrderRequest(
//                request.getCustomerName(),
//                request.getPhoneNumber(),
//                request.getEmail(),
//                orderMapper.toServiceItems(request.getItems())
//        ));
//        return orderMapper.toResponse(updated);
//    }

    @PatchMapping("/{orderId}/status")
    @Operation(
            summary = "Change order status",
            description = "Who can call: only MANAGER from active session, and only for orders assigned to this manager."
    )
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
