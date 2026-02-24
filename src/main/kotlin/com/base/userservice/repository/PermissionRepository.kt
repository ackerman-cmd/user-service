package com.base.userservice.repository

import com.base.userservice.domain.role.Permission
import com.base.userservice.domain.role.PermissionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
@Transactional(readOnly = true)
interface PermissionRepository : JpaRepository<Permission, UUID> {
    fun findByName(name: PermissionType): Permission?
}
