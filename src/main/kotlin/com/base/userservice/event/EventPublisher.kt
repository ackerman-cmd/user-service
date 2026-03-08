package com.base.userservice.event

import com.base.userservice.domain.outbox.OutboxEvent
import com.base.userservice.repository.OutboxEventRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class EventPublisher(
    private val outboxEventRepository: OutboxEventRepository,
    private val objectMapper: ObjectMapper,
    @Value("\${app.kafka.topics.email-verification}") private val emailVerificationTopic: String,
) {
    fun publishEmailVerification(event: EmailVerificationEvent) {
        outboxEventRepository.save(
            OutboxEvent(
                aggregateType = "User",
                aggregateId = event.userId.toString(),
                eventType = "EmailVerification",
                topic = emailVerificationTopic,
                payload = objectMapper.writeValueAsString(event),
            ),
        )
    }
}
