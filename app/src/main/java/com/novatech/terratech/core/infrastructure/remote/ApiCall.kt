package com.novatech.terratech.core.infrastructure.remote

import com.google.gson.JsonParser
import com.novatech.terratech.core.domain.Failure
import java.io.IOException
import retrofit2.HttpException

suspend fun <T> apiCall(block: suspend () -> T): T =
  try {
    block()
  } catch (e: HttpException) {
    val code = runCatching {
      JsonParser.parseString(e.response()?.errorBody()?.string()).asJsonObject.get("code")?.asString
    }
      .getOrNull()
    throw Failure(code ?: if (e.code() == 401) "UNAUTHENTICATED" else "HTTP_ERROR", e.code())
  } catch (e: IOException) {
    throw Failure(if (e.message == "SESSION_EXPIRED") "UNAUTHENTICATED" else "OFFLINE")
  }
