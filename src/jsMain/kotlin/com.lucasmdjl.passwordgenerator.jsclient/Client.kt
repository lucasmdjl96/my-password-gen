package com.lucasmdjl.passwordgenerator.jsclient

import com.lucasmdjl.passwordgenerator.common.routes.SessionRoute
import com.lucasmdjl.passwordgenerator.jsclient.dto.InitialState
import com.lucasmdjl.passwordgenerator.jsclient.plugins.*
import com.lucasmdjl.passwordgenerator.jsclient.react.App
import com.lucasmdjl.passwordgenerator.jsclient.react.Email
import com.lucasmdjl.passwordgenerator.jsclient.react.Site
import com.lucasmdjl.passwordgenerator.jsclient.react.scope
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.resources.*
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.w3c.workers.RegistrationOptions
import react.create
import react.dom.client.createRoot

private const val defaultBackgroundColor = "#00008A"

lateinit var database: IDBDatabase

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
    if (cookiesAccepted == true) {
        registerServiceWorker()
        openIndexedDB()
    }
}

val clipboard = window.navigator.clipboard

val jsonClient = HttpClient {
    installContentNegotiation()
    installResources()
    installLogging()
    installDefaultRequest()
    installHttpResponseValidator()
}

val resourcesFormat = jsonClient.plugin(Resources).resourcesFormat

suspend fun updateSession() {
    jsonClient.put(SessionRoute())
}

fun registerServiceWorker() {
    window.navigator.serviceWorker.register("/static/js/service-worker.js", RegistrationOptions(scope = "/"))
}

fun openIndexedDB() {
    scope.launch {
        database = openDatabase("database", 1) {
            versionChangeLog {
                version(1) {
                    createObjectStore<Email>(keyPath = "id")
                    createObjectStore<Site>(keyPath = "id")
                }
            }
            onBlocked {
                window.alert(
                    "Another tab is blocking a database update. Please, close all other tabs and reload this page."
                )
            }
            onError {
                window.alert(
                    "Something went wrong while opening database. Please, refresh this page to try again."
                )
            }
        } ifBlocking {
            window.alert(
                "This tab is blocking a database update. Please, close this tab."
            )
        }
    }
}
