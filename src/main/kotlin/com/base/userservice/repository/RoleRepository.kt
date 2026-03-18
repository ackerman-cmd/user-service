package com.base.userservice.repository

import com.base.userservice.domain.role.Role
import com.base.userservice.domain.role.RoleType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
@Transactional(readOnly = true)
interface RoleRepository : JpaRepository<Role, UUID> {
    fun findByName(name: RoleType): Role?

    fun findByNameIn(names: Collection<RoleType>): List<Role>

    fun findAllByOrderByNameAsc(): List<Role>
}
