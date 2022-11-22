package com.mypasswordgen.jsclient.plugins

import com.mypasswordgen.jsclient.react.click
import com.mypasswordgen.jsclient.react.getHtmlElementById
import com.mypasswordgen.jsclient.react.on
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
            } else {
                val errorPopup = getHtmlElementById("errorPopup")
                val regex = Regex("""
                    Text: "(.*)"
                """.trimIndent())
                val (message) = regex.find(clientException.message)?.destructured ?: return@handleResponseExceptionWithRequest
                errorPopup?.innerText = message
                ::click on errorPopup
            }
        }
    }
}
