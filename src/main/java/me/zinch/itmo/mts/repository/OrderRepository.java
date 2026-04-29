package me.zinch.itmo.mts.repository;

import me.zinch.itmo.mts.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Override
    @EntityGraph(attributePaths = {"customer", "manager", "items", "items.product"})
    Page<Order> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"customer", "manager", "items", "items.product"})
    Optional<Order> findById(UUID id);

    @EntityGraph(attributePaths = {"customer", "manager", "items", "items.product"})
    Page<Order> findAllByManagerId(UUID managerId, Pageable pageable);
}
