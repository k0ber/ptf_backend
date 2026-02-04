package org.patifiner.base

data class PtfJwtConfig(
    val secret: String,
    val audience: String,
    val realm: String,
    val issuer: String,
    val accessTokenExpirationMs: Long,
    val refreshTokenExpirationMs: Long
)

data class PtfDbConfig(
    val url: String,
    val driver: String,
    val user: String,
    val password: String,
)

data class PtfUploadConfig(
    val uploadPath: String,
    val baseUrl: String,
    val maxFileSizeMB: Int
)
