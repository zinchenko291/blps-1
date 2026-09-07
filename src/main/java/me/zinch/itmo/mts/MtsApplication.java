package me.zinch.itmo.mts;

import me.zinch.itmo.mts.domain.entity.Customer;
import me.zinch.itmo.mts.repository.payment.PaymentRepository;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ConfigurationPropertiesScan
@EntityScan(basePackageClasses = Customer.class)
@EnableJpaRepositories(
        basePackages = "me.zinch.itmo.mts.repository",
        excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PaymentRepository.class)
)
public class MtsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MtsApplication.class, args);
    }

}
