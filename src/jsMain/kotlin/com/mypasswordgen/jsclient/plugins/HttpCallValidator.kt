package com.mypasswordgen.jsclient.plugins

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.browser.window

fun HttpClientConfig<*>.installHttpResponseValidator() {
    expectSuccess = true
    install(HttpCallValidator) {
        handleResponseExceptionWithRequest { exception, _ ->
            val clientException = exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
            val exceptionResponse = clientException.response
            if (exceptionResponse.status == HttpStatusCode.Unauthorized) {
                window.location.reload()
            }
        }
    }
}
