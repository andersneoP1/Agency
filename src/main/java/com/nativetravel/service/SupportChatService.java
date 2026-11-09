package com.nativetravel.service;

import com.nativetravel.dto.SupportMessageDto;
import com.nativetravel.dto.SupportMessageRequest;

import java.util.List;

/**
 * Сервис онлайн-чата службы поддержки с rule-based обработкой и эскалацией.
 */
public interface SupportChatService {

    /**
     * Обработать входящее сообщение от клиента и сгенерировать ответ бота.
     *
     * @param request запрос с сессией и текстом сообщения
     * @param userEmail email авторизованного пользователя или null для гостя
     * @return ответное сообщение бота
     */
    SupportMessageDto processMessage(SupportMessageRequest request, String userEmail);

    /**
     * Получить полную историю сообщений для указанной сессии чата.
     *
     * @param sessionId идентификатор сессии
     * @return список сообщений
     */
    List<SupportMessageDto> getSessionHistory(String sessionId);

    /**
     * Эскалировать обращение на живого оператора.
     *
     * @param sessionId идентификатор сессии
     * @return системное сообщение о передаче оператору
     */
    SupportMessageDto escalateToOperator(String sessionId);

    /**
     * Зафиксировать системное уведомление о занятости всех операторов.
     *
     * @param sessionId идентификатор сессии
     * @return системное сообщение
     */
    SupportMessageDto recordOperatorBusy(String sessionId);
}

