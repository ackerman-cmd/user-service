package com.base.userservice.event

import com.base.userservice.domain.outbox.OutboxEvent
import com.base.userservice.domain.outbox.OutboxEventType
import com.base.userservice.domain.user.User
import com.base.userservice.repository.OutboxEventRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class EventPublisher(
    private val outboxEventRepository: OutboxEventRepository,
    private val objectMapper: ObjectMapper,
    @Value("\${app.kafka.topics.email-verification}") private val emailVerificationTopic: String,
    @Value("\${app.kafka.topics.user-sync}") private val userSyncTopic: String,
) {
    fun publishEmailVerification(event: EmailVerificationEvent) {
        outboxEventRepository.save(
            OutboxEvent(
                aggregateType = "User",
                aggregateId = event.userId.toString(),
                eventType = OutboxEventType.EMAIL_VERIFICATION,
                topic = emailVerificationTopic,
                payload = objectMapper.writeValueAsString(event),
            ),
        )
    }

    fun publishUserSync(
        user: User,
        eventType: OutboxEventType,
    ) {
        val event = UserSyncEvent.from(user, eventType)
        outboxEventRepository.save(
            OutboxEvent(
                aggregateType = "User",
                aggregateId = event.userId.toString(),
                eventType = eventType,
                topic = userSyncTopic,
                payload = objectMapper.writeValueAsString(event),
            ),
        )
    }
}
