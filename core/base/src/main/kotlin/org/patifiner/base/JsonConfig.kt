package org.patifiner.base

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object PtfJson {
    val mapper: ObjectMapper = jacksonObjectMapper().apply {
        configurePtfDefaults()
    }
}

fun ObjectMapper.configurePtfDefaults(): ObjectMapper = this.apply {
    registerModule(JavaTimeModule())
    registerKotlinModule()

    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) // Не падать на неизвестных полях
    configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false) // Использовать ISO-8601 для дат вместо чисел
    configure(SerializationFeature.INDENT_OUTPUT, true) // Красивый вывод (отступы)

    setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL) // Игнорировать null-поля в JSON (экономит трафик)
}
