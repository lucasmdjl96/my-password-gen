package com.lucasmdjl.application


import com.lucasmdjl.application.mapper.EmailMapper
import com.lucasmdjl.application.mapper.SessionMapper
import com.lucasmdjl.application.mapper.SiteMapper
import com.lucasmdjl.application.mapper.UserMapper
import com.lucasmdjl.application.mapper.impl.EmailMapperImpl
import com.lucasmdjl.application.mapper.impl.SessionMapperImpl
import com.lucasmdjl.application.mapper.impl.SiteMapperImpl
import com.lucasmdjl.application.mapper.impl.UserMapperImpl
import com.lucasmdjl.application.plugins.*
import com.lucasmdjl.application.plugins.routing.installRoutes
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

val sessionService: SessionService = SessionServiceImpl
val userService: UserService = UserServiceImpl
val emailService: EmailService = EmailServiceImpl
val siteService: SiteService = SiteServiceImpl

val sessionMapper: SessionMapper = SessionMapperImpl
val userMapper: UserMapper = UserMapperImpl
val emailMapper: EmailMapper = EmailMapperImpl
val siteMapper: SiteMapper = SiteMapperImpl


fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    installContentNegotiation()
    installCORS()
    installCompression()
    installSessions()
    installAuthentication()
    installHttpsRedirect()
    installResources()
    installRoutes()
    initDatabase()
}
