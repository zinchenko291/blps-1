package me.zinch.itmo.mts.config;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import me.zinch.itmo.mts.domain.payment.Payment;
import me.zinch.itmo.mts.repository.payment.PaymentRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.util.Map;

@Configuration
@EnableJpaRepositories(basePackageClasses = PaymentRepository.class, entityManagerFactoryRef = "paymentsEntityManagerFactory", transactionManagerRef = "transactionManager")
public class PaymentsPersistenceConfig {

        @Bean(name = "paymentsEntityManagerFactory")
        public LocalContainerEntityManagerFactoryBean paymentsEntityManagerFactory(
                        @Qualifier("paymentsDataSource") AtomikosDataSourceBean paymentsDataSource) {
                var entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
                entityManagerFactory.setPersistenceUnitName("payments");
                entityManagerFactory.setJtaDataSource(paymentsDataSource);
                entityManagerFactory.setManagedTypes(PersistenceManagedTypes.of(Payment.class.getName()));
                entityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
                entityManagerFactory.setJpaPropertyMap(Map.of(
                                "hibernate.hbm2ddl.auto", "update",
                                "hibernate.transaction.coordinator_class", "jta",
                                "hibernate.transaction.jta.platform",
                                "org.hibernate.engine.transaction.jta.platform.internal.AtomikosJtaPlatform"));
                return entityManagerFactory;
        }
}
