package me.zinch.itmo.mts.repository;

import me.zinch.itmo.mts.domain.entity.Order;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findAllByManagerId(UUID managerId, Sort sort);
}
