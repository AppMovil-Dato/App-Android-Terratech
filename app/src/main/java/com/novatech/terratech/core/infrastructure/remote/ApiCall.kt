package com.novatech.terratech.core.infrastructure.remote

import com.google.gson.JsonParser
import com.novatech.terratech.core.domain.Failure
import java.io.IOException
import retrofit2.HttpException

suspend fun <T> apiCall(block: suspend () -> T): T =
    try {
        block()
    } catch (exception: HttpException) {
        val code =
            runCatching {
                    JsonParser.parseString(exception.response()?.errorBody()?.string())
                        .asJsonObject
                        .get("code")
                        ?.asString
                }
                .getOrNull()
        throw Failure(
            code ?: if (exception.code() == 401) "UNAUTHENTICATED" else "HTTP_ERROR",
            exception.code(),
        )
    } catch (exception: IOException) {
        throw Failure(if (exception.message == "SESSION_EXPIRED") "UNAUTHENTICATED" else "OFFLINE")
    }
