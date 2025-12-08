package org.patifiner.user

import java.time.LocalDate

class UserInfoDto(
    val id: Long,
    val name: String,
    val avatarUrl: String? = null,

    val surname: String,
    val birthDate: LocalDate,
    val email: String,
)
