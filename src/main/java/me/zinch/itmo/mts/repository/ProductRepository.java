package me.zinch.itmo.mts.repository;

import me.zinch.itmo.mts.domain.entity.Product;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}
