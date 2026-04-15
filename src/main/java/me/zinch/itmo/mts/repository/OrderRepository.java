package me.zinch.itmo.mts.repository;

import me.zinch.itmo.mts.domain.entity.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findAllByManagerId(UUID managerId, Sort sort);
}
