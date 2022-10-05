package com.lucasmdjl.passwordgenerator.jsclient

import com.lucasmdjl.passwordgenerator.jsclient.plugins.*
import com.lucasmdjl.passwordgenerator.jsclient.react.App
import io.ktor.client.*
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import react.create
import react.dom.client.createRoot

private const val defaultBackgroundColor = "#00008A"

fun main() {
    val container = document.getElementById("root")!!
    val backgroundColor = localStorage.getItem("backgroundColor") ?: defaultBackgroundColor
    val app = App(backgroundColor).create()
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
