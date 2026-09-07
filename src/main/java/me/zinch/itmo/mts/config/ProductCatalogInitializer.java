package me.zinch.itmo.mts.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ProductCatalogInitializer implements ApplicationRunner {

    private static final List<CatalogProduct> PRODUCTS = List.of(
            product("2c2ebb74-7f0b-4c20-aaf3-f138f9755a46", "Виртуальная АТС",
                    "Облачная телефония для бизнеса с гибкой настройкой", 1500),
            product("7891394c-67bd-4102-aed5-264ef3d77509", "Корпоративная мобильная связь",
                    "Тариф для сотрудников с пакетами минут и интернета", 800),
            product("fd3894b3-5074-4569-9404-74aac27f08a6", "Интернет для офиса", "Высокоскоростной проводной интернет",
                    1200),
            product("e555cc38-812a-4be0-b6f2-d5ffa4b4c66f", "Облачное хранилище",
                    "Безопасное хранение и доступ к данным", 950),
            product("4d75904b-3484-4d75-bf91-97f109bcc7c8", "Видеонаблюдение", "Система удалённого видеоконтроля",
                    2000),
            product("08b69d44-b8e1-435d-a29f-4c3888609e78", "Кибербезопасность", "Комплексная защита IT-инфраструктуры",
                    3000),
            product("7429b33c-4492-4ece-b3f3-5e40418a4f50", "IoT платформа", "Управление устройствами интернета вещей",
                    2700),
            product("dcf3244b-0bda-4a1c-acde-1f689482cb15", "SMS-рассылки", "Сервис массовых уведомлений клиентам",
                    500),
            product("54f1b970-5b5e-4e3d-9198-32594d4148f3", "Email-рассылки", "Маркетинговые email-кампании", 400),
            product("a5a82af8-f9a4-4882-95e0-bc640b27290c", "VPN для бизнеса",
                    "Защищённый удалённый доступ сотрудников", 1100),
            product("10d13943-8cee-4c1e-8c1c-0e6fd392e7d8", "CRM система", "Управление клиентской базой и продажами",
                    2200),
            product("9bfbe77d-2377-4f16-9d21-fe7e2cd51963", "Облачные серверы", "Виртуальные машины для бизнеса", 3500),
            product("8bd07d68-ba04-4530-9ad9-1ec67ad4d55d", "Резервное копирование", "Автоматический бэкап данных",
                    1300),
            product("5c645b56-9cc3-4e8e-9577-d87c4ffb35be", "IP-телефоны", "Оборудование для корпоративной связи",
                    5000),
            product("9abdb5d7-00fe-4315-bae6-22bb5d92846a", "Контроль сотрудников",
                    "Система мониторинга рабочего времени", 1700),
            product("f0d8a50d-3e42-4950-beed-19b26548d9e3", "Wi-Fi для бизнеса", "Организация корпоративной Wi-Fi сети",
                    1400),
            product("0ee72e3a-ba58-4065-9cd2-2d2bd7349aba", "Аналитика данных", "Инструменты бизнес-аналитики", 2600),
            product("d0239808-4b78-4f91-be7f-464ca52eea74", "Онлайн-кассы", "Решение для приёма платежей", 4800),
            product("795d0e18-2531-4eed-941b-1c74ff48a72e", "Электронный документооборот",
                    "Обмен документами в цифровом виде", 900),
            product("2dede89c-c492-45ab-8082-8bbd0a19a4e7", "Хостинг сайтов", "Размещение и поддержка веб-сайтов",
                    700));

    private final EntityManager entityManager;
    private final TransactionTemplate jtaTransactionTemplate;

    public ProductCatalogInitializer(EntityManager entityManager, TransactionTemplate jtaTransactionTemplate) {
        this.entityManager = entityManager;
        this.jtaTransactionTemplate = jtaTransactionTemplate;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        jtaTransactionTemplate.executeWithoutResult(status -> {
            Long productCount = entityManager.createQuery("select count(p) from Product p", Long.class)
                    .getSingleResult();
            if (productCount == 0) {
                PRODUCTS.forEach(product -> entityManager.persist(product.toEntity()));
            }
        });
    }

    private static CatalogProduct product(String id, String name, String description, int price) {
        return new CatalogProduct(UUID.fromString(id), name, description, BigDecimal.valueOf(price));
    }

    private record CatalogProduct(UUID id, String name, String description, BigDecimal price) {
        me.zinch.itmo.mts.domain.entity.Product toEntity() {
            return me.zinch.itmo.mts.domain.entity.Product.builder()
                    .id(id).name(name).description(description).price(price).build();
        }
    }
}
