package com.base.userservice.unit.event

import com.base.userservice.domain.outbox.OutboxDeadLetter
import com.base.userservice.domain.outbox.OutboxEvent
import com.base.userservice.domain.outbox.OutboxEventType
import com.base.userservice.event.OutboxScheduler
import com.base.userservice.repository.OutboxDeadLetterRepository
import com.base.userservice.repository.OutboxEventRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.util.UUID
import java.util.concurrent.CompletableFuture

class OutboxSchedulerTest {
    private val outboxEventRepository: OutboxEventRepository = mockk(relaxed = true)
    private val deadLetterRepository: OutboxDeadLetterRepository = mockk(relaxed = true)
    private val kafkaTemplate: KafkaTemplate<String, String> = mockk()

    init {
        every { outboxEventRepository.save(any<OutboxEvent>()) } answers { firstArg() }
        every { deadLetterRepository.save(any<OutboxDeadLetter>()) } answers { firstArg() }
    }

    private val scheduler =
        OutboxScheduler(
            outboxEventRepository = outboxEventRepository,
            deadLetterRepository = deadLetterRepository,
            kafkaTemplate = kafkaTemplate,
            maxRetries = 3,
        )

    @Test
    fun `processOutbox does nothing when no events`() {
        every { outboxEventRepository.findBySentAtIsNullOrderByCreatedAtAsc() } returns emptyList()

        scheduler.processOutbox()

        verify(exactly = 0) { kafkaTemplate.send(any<ProducerRecord<String, String>>()) }
    }

    @Test
    fun `processOutbox sends event and marks as sent`() {
        val event = createEvent()
        every { outboxEventRepository.findBySentAtIsNullOrderByCreatedAtAsc() } returns listOf(event)
        every { kafkaTemplate.send(any<ProducerRecord<String, String>>()) } returns completedFuture()

        scheduler.processOutbox()

        assertNotNull(event.sentAt)
        verify { outboxEventRepository.save(event) }
        verify(exactly = 0) { deadLetterRepository.save(any()) }
    }

    @Test
    fun `processOutbox increments retry on failure`() {
        val event = createEvent()
        every { outboxEventRepository.findBySentAtIsNullOrderByCreatedAtAsc() } returns listOf(event)
        every { kafkaTemplate.send(any<ProducerRecord<String, String>>()) } returns failedFuture("Kafka unavailable")

        scheduler.processOutbox()

        assertNull(event.sentAt)
        assertEquals(1, event.retryCount)
        assertNotNull(event.lastError)
        assertTrue(event.lastError!!.contains("Kafka unavailable"))
        verify { outboxEventRepository.save(event) }
        verify(exactly = 0) { deadLetterRepository.save(any()) }
    }

    @Test
    fun `processOutbox moves to dead letter after max retries`() {
        val event = createEvent(retryCount = 2)
        every { outboxEventRepository.findBySentAtIsNullOrderByCreatedAtAsc() } returns listOf(event)
        every { kafkaTemplate.send(any<ProducerRecord<String, String>>()) } returns failedFuture("Kafka unavailable")

        scheduler.processOutbox()

        val dlSlot = slot<OutboxDeadLetter>()
        verify { deadLetterRepository.save(capture(dlSlot)) }
        verify { outboxEventRepository.delete(event) }

        val deadLetter = dlSlot.captured
        assertEquals(event.id, deadLetter.originalEventId)
        assertEquals(event.aggregateType, deadLetter.aggregateType)
        assertEquals(event.aggregateId, deadLetter.aggregateId)
        assertEquals(event.eventType, deadLetter.eventType)
        assertEquals(event.payload, deadLetter.payload)
        assertEquals(3, deadLetter.retryCount)
    }

    @Test
    fun `processOutbox processes multiple events independently`() {
        val successEvent = createEvent()
        val failEvent = createEvent()

        every { outboxEventRepository.findBySentAtIsNullOrderByCreatedAtAsc() } returns
            listOf(successEvent, failEvent)
        every { kafkaTemplate.send(any<ProducerRecord<String, String>>()) } answers {
            val record = firstArg<ProducerRecord<String, String>>()
            if (record.key() == successEvent.aggregateId) {
                completedFuture()
            } else {
                failedFuture("Kafka unavailable")
            }
        }

        scheduler.processOutbox()

        assertNotNull(successEvent.sentAt)
        assertNull(failEvent.sentAt)
        assertEquals(1, failEvent.retryCount)
    }

    private fun createEvent(retryCount: Int = 0) =
        OutboxEvent(
            aggregateType = "User",
            aggregateId = UUID.randomUUID().toString(),
            eventType = OutboxEventType.EMAIL_VERIFICATION,
            topic = "test-topic",
            payload = """{"test":true}""",
        ).apply { this.retryCount = retryCount }

    private fun completedFuture(): CompletableFuture<SendResult<String, String>> = CompletableFuture.completedFuture(mockk(relaxed = true))

    private fun failedFuture(message: String): CompletableFuture<SendResult<String, String>> =
        CompletableFuture<SendResult<String, String>>().apply {
            completeExceptionally(RuntimeException(message))
        }
}
