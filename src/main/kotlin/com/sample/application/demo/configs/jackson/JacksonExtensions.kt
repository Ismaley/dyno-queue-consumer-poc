package com.sample.application.demo.configs.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

object JacksonExtensions {

    val jacksonObjectMapper: ObjectMapper by lazy {
        ObjectMapper().registerModule(KotlinModule())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

}

fun <T> String.jsonToObject(t: Class<T>): T =
    JacksonExtensions.jacksonObjectMapper.readValue(this, t)

fun String.jsonToNode(): JsonNode =
    JacksonExtensions.jacksonObjectMapper.readTree(this)

fun ByteArray.jsonToNode(): JsonNode =
    JacksonExtensions.jacksonObjectMapper.readTree(this)

fun <T> T.toJson(): String =
    JacksonExtensions.jacksonObjectMapper.writeValueAsString(this)

fun <T, R : JsonNode> T.toJsonNode(): R =
    JacksonExtensions.jacksonObjectMapper.valueToTree(this)
