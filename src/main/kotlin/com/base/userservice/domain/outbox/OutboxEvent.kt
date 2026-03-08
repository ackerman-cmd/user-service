package com.base.userservice.domain.outbox

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "outbox_events", schema = "user_service")
class OutboxEvent(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "aggregate_type", nullable = false)
    val aggregateType: String,
    @Column(name = "aggregate_id", nullable = false)
    val aggregateId: String,
    @Column(name = "event_type", nullable = false)
    val eventType: String,
    @Column(nullable = false)
    val topic: String,
    @Column(nullable = false, columnDefinition = "TEXT")
    val payload: String,
    @Column(name = "retry_count", nullable = false)
    var retryCount: Int = 0,
    @Column(name = "last_error", columnDefinition = "TEXT")
    var lastError: String? = null,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "sent_at")
    var sentAt: LocalDateTime? = null,
)
