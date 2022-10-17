package com.lucasmdjl.passwordgenerator.server


import com.lucasmdjl.passwordgenerator.server.plugins.*
import com.lucasmdjl.passwordgenerator.server.plugins.routing.installRoutes
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    installKoin()
    installContentNegotiation()
    installCORS()
    installCompression()
    installSessions()
    installAuthentication()
    installHttpsRedirect()
    installResources()
    installStatusPages()
    installRoutes()
    installCallLogging()
    initDatabase()
}
