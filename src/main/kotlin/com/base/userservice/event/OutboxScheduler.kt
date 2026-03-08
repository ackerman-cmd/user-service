package com.base.userservice.event

import com.base.userservice.domain.outbox.OutboxDeadLetter
import com.base.userservice.domain.outbox.OutboxEvent
import com.base.userservice.repository.OutboxDeadLetterRepository
import com.base.userservice.repository.OutboxEventRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@ConditionalOnProperty(name = ["app.outbox.enabled"], havingValue = "true", matchIfMissing = true)
class OutboxScheduler(
    private val outboxEventRepository: OutboxEventRepository,
    private val deadLetterRepository: OutboxDeadLetterRepository,
    private val kafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${app.outbox.max-retries:5}") private val maxRetries: Int,
) {
    private val log = LoggerFactory.getLogger(OutboxScheduler::class.java)

    @Scheduled(fixedDelayString = "\${app.outbox.poll-interval-ms:5000}")
    fun processOutbox() {
        val events = outboxEventRepository.findBySentAtIsNullOrderByCreatedAtAsc()
        if (events.isEmpty()) return

        log.debug("Processing {} outbox events", events.size)

        events.forEach(::processEvent)
    }

    private fun processEvent(event: OutboxEvent) {
        runCatching {
            kafkaTemplate.send(event.topic, event.aggregateId, event.payload).get()
        }.onSuccess {
            event.sentAt = LocalDateTime.now()
            outboxEventRepository.save(event)
            log.info(
                "Outbox event sent: type={}, aggregateId={}, topic={}",
                event.eventType,
                event.aggregateId,
                event.topic,
            )
        }.onFailure { ex ->
            handleFailure(event, ex)
        }
    }

    private fun handleFailure(
        event: OutboxEvent,
        cause: Throwable,
    ) {
        event.retryCount++
        event.lastError = cause.message

        if (event.retryCount >= maxRetries) {
            moveToDeadLetter(event, cause)
        } else {
            outboxEventRepository.save(event)
            log.warn(
                "Outbox event {} failed (attempt {}/{}): {}",
                event.id,
                event.retryCount,
                maxRetries,
                cause.message,
            )
        }
    }

    private fun moveToDeadLetter(
        event: OutboxEvent,
        cause: Throwable,
    ) {
        deadLetterRepository.save(
            OutboxDeadLetter(
                originalEventId = event.id,
                aggregateType = event.aggregateType,
                aggregateId = event.aggregateId,
                eventType = event.eventType,
                topic = event.topic,
                payload = event.payload,
                error = cause.stackTraceToString(),
                retryCount = event.retryCount,
                createdAt = event.createdAt,
            ),
        )
        outboxEventRepository.delete(event)
        log.error(
            "Outbox event {} moved to dead letters after {} retries: {}",
            event.id,
            event.retryCount,
            cause.message,
        )
    }
}
