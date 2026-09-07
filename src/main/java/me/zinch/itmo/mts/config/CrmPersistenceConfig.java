package me.zinch.itmo.mts.config;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import me.zinch.itmo.mts.domain.entity.Customer;
import me.zinch.itmo.mts.domain.entity.Order;
import me.zinch.itmo.mts.domain.entity.OrderItem;
import me.zinch.itmo.mts.domain.entity.Product;
import me.zinch.itmo.mts.domain.entity.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.util.Map;

@Configuration
public class CrmPersistenceConfig {

        @Bean(name = "entityManagerFactory")
        @Primary
        public LocalContainerEntityManagerFactoryBean entityManagerFactory(
                        @Qualifier("dataSource") AtomikosDataSourceBean crmDataSource) {
                var entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
                entityManagerFactory.setPersistenceUnitName("crm");
                entityManagerFactory.setJtaDataSource(crmDataSource);
                entityManagerFactory.setManagedTypes(PersistenceManagedTypes.of(
                                Customer.class.getName(),
                                Order.class.getName(),
                                OrderItem.class.getName(),
                                Product.class.getName(),
                                User.class.getName()));
                entityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
                entityManagerFactory.setJpaPropertyMap(Map.of(
                                "hibernate.hbm2ddl.auto", "update",
                                "hibernate.transaction.coordinator_class", "jta",
                                "hibernate.transaction.jta.platform",
                                "org.hibernate.engine.transaction.jta.platform.internal.AtomikosJtaPlatform"));
                return entityManagerFactory;
        }
}
