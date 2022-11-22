/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

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
