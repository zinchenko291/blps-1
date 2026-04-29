package me.zinch.itmo.mts.service.manager;

import lombok.RequiredArgsConstructor;
import me.zinch.itmo.mts.domain.entity.User;
import me.zinch.itmo.mts.domain.enums.UserRole;
import me.zinch.itmo.mts.repository.UserRepository;
import me.zinch.itmo.mts.service.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerServiceImpl implements ManagerService {

    private final UserRepository userRepository;

    @Override
    public Page<User> getManagersForSenior(UUID requesterId, Pageable pageable) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ServiceException("Пользователь не найден: " + requesterId));
        if (requester.getRole() != UserRole.SENIOR_MANAGER) {
            throw new ServiceException("Только старший менеджер может назначать на обработку заказа");
        }
        return userRepository.findAllByRole(UserRole.MANAGER, pageable);
    }
}
