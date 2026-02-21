package com.base.userservice.repository.role

import com.base.userservice.domain.role.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
@Transactional(readOnly = true)
interface RoleRepository : JpaRepository<Role, UUID> {
    fun findByName(name: String): Role?
}
