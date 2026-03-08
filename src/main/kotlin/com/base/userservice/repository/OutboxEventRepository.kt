package com.base.userservice.repository

import com.base.userservice.domain.outbox.OutboxEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OutboxEventRepository : JpaRepository<OutboxEvent, UUID> {
    fun findBySentAtIsNullOrderByCreatedAtAsc(): List<OutboxEvent>
}
