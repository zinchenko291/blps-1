package me.zinch.itmo.mts.service.manager;

import me.zinch.itmo.mts.domain.entity.User;
import java.util.List;
import java.util.UUID;

public interface ManagerService {
    List<User> getManagersForSenior(UUID requesterId);
}
