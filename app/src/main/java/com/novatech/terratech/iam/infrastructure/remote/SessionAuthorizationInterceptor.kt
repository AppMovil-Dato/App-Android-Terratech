package com.novatech.terratech.iam.infrastructure.remote

import com.novatech.terratech.iam.domain.repository.AccountRepository
import java.io.IOException
import javax.inject.Provider
import okhttp3.Interceptor
import okhttp3.Response

class SessionAuthorizationInterceptor(private val account: Provider<AccountRepository>) :
  Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    val session = account.get().session.value
    val auth = request.url.encodedPath.contains("/authentication/")
    if (!auth && session?.expired() == true) throw IOException("SESSION_EXPIRED")
    return chain.proceed(
      request
        .newBuilder()
        .apply {
          if (!auth && session != null) header("Authorization", "Bearer ${session.token}")
        }
        .build()
    )
  }
}
