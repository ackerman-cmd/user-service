package com.base.userservice.repository

import com.base.userservice.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
@Transactional(readOnly = true)
interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?

    fun findByUsername(username: String): User?

    @Query(
        """
        select distinct u
        from User u
        left join fetch u.roles r
        left join fetch r.permissions p
        where u.id = :id
    """,
    )
    fun findByIdWithRolesAndPermissions(id: UUID): User?

    @Query(
        """
        select distinct u
        from User u
        left join fetch u.roles r
        left join fetch r.permissions p
        where u.username = :username
    """,
    )
    fun findByUsernameWithRolesAndPermissions(username: String): User?

    @Query(
        """
        select distinct p.name
        from User u
        join u.roles r
        join r.permissions p
        where u.username = :username
    """,
    )
    fun findPermissionsByUsername(username: String): List<String>

    @Query(
        """
        select distinct r.name
        from User u
        join u.roles r
        where u.username = :username
    """,
    )
    fun findRolesByUsername(username: String): List<String>
}
