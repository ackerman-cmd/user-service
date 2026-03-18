package com.base.userservice.domain.outbox

enum class OutboxEventType {
    EMAIL_VERIFICATION,
    USER_EMAIL_VERIFIED,
    USER_PROFILE_UPDATED,
    USER_REGISTERED,
    USER_PASSWORD_CHANGED,
    USER_STATUS_CHANGED,
    USER_ROLES_CHANGED,
}
