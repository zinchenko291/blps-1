package me.zinch.itmo.mts.web.order.mapper;

import me.zinch.itmo.mts.domain.entity.Order;
import me.zinch.itmo.mts.domain.entity.OrderItem;
import me.zinch.itmo.mts.service.order.dto.OrderItemRequest;
import me.zinch.itmo.mts.web.order.dto.CreateOrderItemApiRequest;
import me.zinch.itmo.mts.web.order.dto.OrderApiResponse;
import me.zinch.itmo.mts.web.order.dto.OrderItemApiResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "customerEmail", source = "customer.email")
    @Mapping(target = "customerPhoneNumber", source = "customer.phoneNumber")
    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "manager.name", source = "manager.name")
    @Mapping(target = "totalAmount", expression = "java(calculateOrderTotal(order.getItems()))")
    OrderApiResponse toResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "unitPrice", source = "product.price")
    @Mapping(target = "lineTotal", expression = "java(calculateLineTotal(item))")
    OrderItemApiResponse toResponse(OrderItem item);

    List<OrderItemRequest> toServiceItems(List<CreateOrderItemApiRequest> items);

    default BigDecimal calculateLineTotal(OrderItem item) {
        return item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    default BigDecimal calculateOrderTotal(List<OrderItem> items) {
        return items.stream()
                .map(this::calculateLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
