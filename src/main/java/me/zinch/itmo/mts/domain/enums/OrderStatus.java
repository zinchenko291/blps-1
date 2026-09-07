package me.zinch.itmo.mts.domain.enums;

public enum OrderStatus {
    NEW, // Пользователь создал заказ
    REJECTED, // Пользователь отказался от заказа
    APPROVED, // Пользователь согласился на заказ
    PLACED // Ссылка оплаты создана, заказ завершён
}
