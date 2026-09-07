package me.zinch.itmo.mts.security;

import jakarta.persistence.EntityManager;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class XmlAccountSynchronizer {

    @Bean
    ApplicationRunner synchronizeXmlAccounts(XmlAccountStore accounts, EntityManager entityManager,
            TransactionTemplate jtaTransactionTemplate) {
        return arguments -> jtaTransactionTemplate.executeWithoutResult(status -> {
            Long accountCount = entityManager.createQuery("select count(u) from User u", Long.class)
                    .getSingleResult();
            if (accountCount == 0) {
                for (SecurityAccount account : accounts.all()) {
                    var user = new me.zinch.itmo.mts.domain.entity.User();
                    user.setId(account.id());
                    user.setLogin(account.login());
                    user.setPassword(account.passwordHash());
                    user.setName(account.name());
                    user.setRole(account.role());
                    entityManager.persist(user);
                }
            }
        });
    }
}
