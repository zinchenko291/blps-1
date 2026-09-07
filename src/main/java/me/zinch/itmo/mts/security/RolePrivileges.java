package me.zinch.itmo.mts.security;

import me.zinch.itmo.mts.domain.enums.UserRole;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class RolePrivileges {
    private RolePrivileges() { }

    public static Set<Privilege> privileges(UserRole role) {
        return switch (role) {
            case CLIENT -> EnumSet.of(Privilege.ORDER_CREATE);
            case MANAGER -> EnumSet.of(Privilege.ORDER_LIST_OWN, Privilege.ORDER_READ,
                    Privilege.ORDER_UPDATE, Privilege.ORDER_CHANGE_STATUS);
            case SENIOR_MANAGER -> EnumSet.of(Privilege.ORDER_LIST_ALL, Privilege.ORDER_READ,
                    Privilege.ORDER_DELETE, Privilege.ORDER_ASSIGN_MANAGER, Privilege.MANAGER_LIST);
        };
    }

    public static Set<SimpleGrantedAuthority> authorities(SecurityAccount account) {
        Set<SimpleGrantedAuthority> result = privileges(account.role()).stream()
                .map(privilege -> new SimpleGrantedAuthority(privilege.name()))
                .collect(Collectors.toSet());
        result.add(new SimpleGrantedAuthority("ROLE_" + account.role().name()));
        return result;
    }
}
