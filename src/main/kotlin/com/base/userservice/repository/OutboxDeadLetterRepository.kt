package com.base.userservice.repository

import com.base.userservice.domain.outbox.OutboxDeadLetter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OutboxDeadLetterRepository : JpaRepository<OutboxDeadLetter, UUID>
