package com.lucasmdjl.application


import com.lucasmdjl.application.plugins.installCORS
import com.lucasmdjl.application.plugins.installCompression
import com.lucasmdjl.application.plugins.installContentNegotiation
import com.lucasmdjl.application.plugins.installSessions
import com.lucasmdjl.application.routes.installRoutes
import com.lucasmdjl.application.service.EmailService
import com.lucasmdjl.application.service.SessionService
import com.lucasmdjl.application.service.SiteService
import com.lucasmdjl.application.service.UserService
import com.lucasmdjl.application.service.impl.EmailServiceImpl
import com.lucasmdjl.application.service.impl.SessionServiceImpl
import com.lucasmdjl.application.service.impl.SiteServiceImpl
import com.lucasmdjl.application.service.impl.UserServiceImpl
import io.ktor.server.application.*
import io.ktor.server.netty.*

val emailService: EmailService = EmailServiceImpl
val userService: UserService = UserServiceImpl
val siteService: SiteService = SiteServiceImpl
val sessionService: SessionService = SessionServiceImpl


fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    installContentNegotiation()
    installCORS()
    installCompression()
    installSessions()
    DatabaseFactory.init(environment.config)
    installRoutes()
}
