package me.zinch.itmo.mts.service.manager;

import me.zinch.itmo.mts.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ManagerService {
    Page<User> getManagersForSenior(UUID requesterId, Pageable pageable);
}
