package com.lucasmdjl.passwordgenerator.jsclient

import com.lucasmdjl.passwordgenerator.jsclient.dto.InitialState
import com.lucasmdjl.passwordgenerator.jsclient.plugins.*
import com.lucasmdjl.passwordgenerator.jsclient.react.App
import com.lucasmdjl.passwordgenerator.jsclient.react.scope
import com.lucasmdjl.passwordgenerator.jsclient.react.updateSession
import io.ktor.client.*
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.launch
import react.create
import react.dom.client.createRoot

private const val defaultBackgroundColor = "#00008A"

fun main() {
    val container = document.getElementById("root")!!
    val backgroundColor = localStorage.getItem("backgroundColor") ?: defaultBackgroundColor
    val cookiesAccepted = localStorage.getItem("cookiesAccepted")?.toBooleanStrict()
    val online = localStorage.getItem("online").toBoolean()
    val initialState = InitialState(cookiesAccepted, online, backgroundColor)
    if (cookiesAccepted == true && online) scope.launch {
        updateSession()
    }
    val app = App(initialState).create()
    createRoot(container).render(app)
}

val clipboard = window.navigator.clipboard

val jsonClient = HttpClient {
    installContentNegotiation()
    installResources()
    installLogging()
    installDefaultRequest()
    installHttpResponseValidator()
}
