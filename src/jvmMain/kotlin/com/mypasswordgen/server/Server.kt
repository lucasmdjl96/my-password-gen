package com.mypasswordgen.server


import com.mypasswordgen.server.plugins.*
import com.mypasswordgen.server.plugins.routing.installRoutes
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    installKoin()
    installContentNegotiation()
    installCompression()
    installSessions()
    installAuthentication()
    installResources()
    installStatusPages()
    installRoutes()
    installCallLogging()
    initDatabase()
}
