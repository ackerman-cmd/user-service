package com.base.userservice.domain.outbox

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "outbox_dead_letters", schema = "user_service")
class OutboxDeadLetter(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "original_event_id", nullable = false)
    val originalEventId: UUID,
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
    @Column(nullable = false, columnDefinition = "TEXT")
    val error: String,
    @Column(name = "retry_count", nullable = false)
    val retryCount: Int,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,
    @Column(name = "failed_at", nullable = false, updatable = false)
    val failedAt: LocalDateTime = LocalDateTime.now(),
)
