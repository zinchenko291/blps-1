package me.zinch.itmo.mts.repository;

import me.zinch.itmo.mts.domain.entity.User;
import me.zinch.itmo.mts.domain.enums.UserRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByLogin(String login);
    List<User> findAllByRole(UserRole role);
}
