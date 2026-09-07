package me.zinch.itmo.mts.config;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Properties;

@Configuration
public class AtomikosConfig {
    @Value("${spring.datasource.url}")
    private String crmUrl;

    @Value("${spring.datasource.username}")
    private String crmUsername;

    @Value("${spring.datasource.password}")
    private String crmPassword;

    @Value("${app.datasource.payments.url}")
    private String paymentsUrl;

    @Value("${app.datasource.payments.username}")
    private String paymentsUsername;

    @Value("${app.datasource.payments.password}")
    private String paymentsPassword;

    @Bean(name = "dataSource", initMethod = "init", destroyMethod = "close")
    @Primary
    @DependsOn("atomikosTransactionManager")
    public AtomikosDataSourceBean crmDataSource() {
        return xaDataSource("mts-crm", crmUrl, crmUsername, crmPassword);
    }

    @Bean(name = "paymentsDataSource", initMethod = "init", destroyMethod = "close")
    @DependsOn("atomikosTransactionManager")
    public AtomikosDataSourceBean paymentsDataSource() {
        return xaDataSource("mts-payments", paymentsUrl, paymentsUsername, paymentsPassword);
    }

    private AtomikosDataSourceBean xaDataSource(String resourceName, String url, String username, String password) {
        var ds = new AtomikosDataSourceBean();
        ds.setUniqueResourceName(resourceName);
        ds.setXaDataSourceClassName("org.postgresql.xa.PGXADataSource");
        Properties props = new Properties();
        props.setProperty("user", username);
        props.setProperty("password", password);
        props.setProperty("url", url);
        ds.setXaProperties(props);
        ds.setPoolSize(5);
        return ds;
    }

    @Bean
    public PlatformTransactionManager transactionManager(UserTransactionManager atomikosTransactionManager) {
        return new JtaTransactionManager(atomikosTransactionManager, atomikosTransactionManager);
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    public UserTransactionManager atomikosTransactionManager() {
        var transactionManager = new UserTransactionManager();
        transactionManager.setStartupTransactionService(true);
        return transactionManager;
    }

    @Bean
    public TransactionTemplate jtaTransactionTemplate(PlatformTransactionManager transactionManager) {
        var template = new TransactionTemplate(transactionManager);
        template.setName("order-placed-jta-transaction");
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        template.setTimeout(30);
        return template;
    }
}
