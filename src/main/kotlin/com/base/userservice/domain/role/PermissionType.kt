package com.base.userservice.domain.role

enum class PermissionType {
    USER_READ,
    USER_WRITE,
    USER_DELETE,
    ADMIN_ACCESS,

    /** Работа с обращениями: просмотр, ответ клиенту, закрытие, переназначение на коллег */
    APPEAL_READ,
    APPEAL_WRITE,

    /** Просмотр и мониторинг загруженности операторов */
    OPERATOR_MONITOR,
}
