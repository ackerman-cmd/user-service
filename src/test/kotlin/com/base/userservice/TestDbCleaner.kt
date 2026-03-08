package com.base.userservice

import com.base.userservice.repository.PermissionRepository
import com.base.userservice.repository.RoleRepository
import com.base.userservice.repository.UserRepository
import com.base.userservice.util.TestConstants
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class TestDbCleaner(
    private val jdbc: JdbcTemplate,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val permissionRepository: PermissionRepository,
) {
    private val defaultSchema = TestConstants.DEFAULT_SCHEMA

    @Transactional
    fun clearAllTables() {
        jdbc.execute("""DELETE FROM "$defaultSchema"."outbox_dead_letters"""")
        jdbc.execute("""DELETE FROM "$defaultSchema"."outbox_events"""")
        jdbc.execute("""DELETE FROM "$defaultSchema"."user_verification_tokens"""")
        jdbc.execute("""DELETE FROM "$defaultSchema"."user_roles"""")
        jdbc.execute("""DELETE FROM "$defaultSchema"."role_permissions"""")

        userRepository.deleteAll()
        roleRepository.deleteAll()
        permissionRepository.deleteAll()
    }
}
