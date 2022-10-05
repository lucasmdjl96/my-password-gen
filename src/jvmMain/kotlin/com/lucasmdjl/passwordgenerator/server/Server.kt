package com.lucasmdjl.passwordgenerator.server


import com.lucasmdjl.passwordgenerator.server.mapper.EmailMapper
import com.lucasmdjl.passwordgenerator.server.mapper.SessionMapper
import com.lucasmdjl.passwordgenerator.server.mapper.SiteMapper
import com.lucasmdjl.passwordgenerator.server.mapper.UserMapper
import com.lucasmdjl.passwordgenerator.server.mapper.impl.EmailMapperImpl
import com.lucasmdjl.passwordgenerator.server.mapper.impl.SessionMapperImpl
import com.lucasmdjl.passwordgenerator.server.mapper.impl.SiteMapperImpl
import com.lucasmdjl.passwordgenerator.server.mapper.impl.UserMapperImpl
import com.lucasmdjl.passwordgenerator.server.plugins.*
import com.lucasmdjl.passwordgenerator.server.plugins.routing.installRoutes
import com.lucasmdjl.passwordgenerator.server.service.EmailService
import com.lucasmdjl.passwordgenerator.server.service.SessionService
import com.lucasmdjl.passwordgenerator.server.service.SiteService
import com.lucasmdjl.passwordgenerator.server.service.UserService
import com.lucasmdjl.passwordgenerator.server.service.impl.EmailServiceImpl
import com.lucasmdjl.passwordgenerator.server.service.impl.SessionServiceImpl
import com.lucasmdjl.passwordgenerator.server.service.impl.SiteServiceImpl
import com.lucasmdjl.passwordgenerator.server.service.impl.UserServiceImpl
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
    installCallLogging()
    initDatabase()
}
