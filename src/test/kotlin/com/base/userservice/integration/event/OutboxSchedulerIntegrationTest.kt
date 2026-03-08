package com.base.userservice.integration.event

import com.base.userservice.TestDbCleaner
import com.base.userservice.domain.outbox.OutboxEvent
import com.base.userservice.event.OutboxScheduler
import com.base.userservice.integration.AbstractIntegrationTest
import com.base.userservice.repository.OutboxDeadLetterRepository
import com.base.userservice.repository.OutboxEventRepository
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.utils.KafkaTestUtils
import java.time.Duration
import java.util.UUID

class OutboxSchedulerIntegrationTest : AbstractIntegrationTest() {
    @Autowired
    private lateinit var outboxEventRepository: OutboxEventRepository

    @Autowired
    private lateinit var deadLetterRepository: OutboxDeadLetterRepository

    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @Autowired
    private lateinit var dbCleaner: TestDbCleaner

    private lateinit var scheduler: OutboxScheduler

    @BeforeEach
    fun setUp() {
        dbCleaner.clearAllTables()
        scheduler =
            OutboxScheduler(
                outboxEventRepository = outboxEventRepository,
                deadLetterRepository = deadLetterRepository,
                kafkaTemplate = kafkaTemplate,
                maxRetries = 3,
            )
    }

    @Test
    fun `processOutbox sends event to kafka and marks as sent`() {
        val aggregateId = UUID.randomUUID().toString()
        val payload = """{"userId":"$aggregateId","email":"test@example.com"}"""

        val saved =
            outboxEventRepository.save(
                OutboxEvent(
                    aggregateType = "User",
                    aggregateId = aggregateId,
                    eventType = "EmailVerification",
                    topic = "email-verification-test",
                    payload = payload,
                ),
            )

        scheduler.processOutbox()

        val updated = outboxEventRepository.findById(saved.id).orElseThrow()
        assertNotNull(updated.sentAt)
        assertEquals(0, updated.retryCount)

        val consumerProps =
            KafkaTestUtils
                .consumerProps("test-group-${UUID.randomUUID()}", "true", embeddedKafkaBroker)
                .apply { this[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest" }

        val consumer =
            DefaultKafkaConsumerFactory<String, String>(
                consumerProps,
                StringDeserializer(),
                StringDeserializer(),
            ).createConsumer()

        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "email-verification-test")

        val records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10))
        val matching = records.records("email-verification-test").filter { it.key() == aggregateId }

        assertEquals(1, matching.size)
        assertEquals(payload, matching.first().value())

        consumer.close()
    }

    @Test
    fun `processOutbox does not resend already sent events`() {
        outboxEventRepository.save(
            OutboxEvent(
                aggregateType = "User",
                aggregateId = UUID.randomUUID().toString(),
                eventType = "EmailVerification",
                topic = "email-verification-test",
                payload = "{}",
            ),
        )

        scheduler.processOutbox()
        scheduler.processOutbox()

        val allEvents = outboxEventRepository.findAll()
        assertEquals(1, allEvents.size)
        assertNotNull(allEvents.first().sentAt)

        val unsent = outboxEventRepository.findBySentAtIsNullOrderByCreatedAtAsc()
        assertTrue(unsent.isEmpty())
    }
}
