package com.lucasmdjl.application


import SessionCookie
import com.lucasmdjl.application.service.EmailService
import com.lucasmdjl.application.service.SessionService
import com.lucasmdjl.application.service.SiteService
import com.lucasmdjl.application.service.UserService
import com.lucasmdjl.application.service.impl.EmailServiceImpl
import com.lucasmdjl.application.service.impl.SessionServiceImpl
import com.lucasmdjl.application.service.impl.SiteServiceImpl
import com.lucasmdjl.application.service.impl.UserServiceImpl
import dto.LoginDto
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.util.*
import kotlinx.html.*
import java.security.MessageDigest
import java.util.*

val emailService: EmailService = EmailServiceImpl
val userService: UserService = UserServiceImpl
val siteService: SiteService = SiteServiceImpl
val sessionService: SessionService = SessionServiceImpl

fun HTML.index() {
    head {
        title("Password Generator")
    }
    body {
        div {
            id = "root"
        }
        script(src = "/static/password-manager.js") {}
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "localhost") {
        install(ContentNegotiation) {
            json()
        }
        install(CORS) {
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            anyHost()
        }
        install(Compression) {
            gzip()
        }
        install(Sessions) {
            cookie<SessionCookie>("session") {
                cookie.maxAgeInSeconds = 30 * 24 * 60 * 60
                cookie.secure = false
                cookie.path = "/"
            }
        }
        DatabaseFactory.init()
        routing {
            get("/") {
                val sessionCookieTemp = call.sessions.get<SessionCookie>()
                val sessionDto = if (sessionCookieTemp != null) {
                    sessionService.getById(UUID.fromString(sessionCookieTemp.sessionId)) ?: sessionService.create()
                } else {
                    sessionService.create()
                }
                sessionService.updatePasswordById(sessionDto.sessionId, null)
                call.sessions.set(SessionCookie(sessionDto.sessionId.toString()))
                call.respondHtml(HttpStatusCode.OK, HTML::index)
            }
            get("/password/{username}/{emailAddress}/{siteName}") {
                val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
                val sessionDto = sessionService.getById(sessionId)!!
                call.respondText(
                    sha256(
                        """
                    ${call.parameters.getOrFail("username")}
                    ${call.parameters.getOrFail("emailAddress")}
                    ${call.parameters.getOrFail("siteName")}
                    ${sessionDto.password}
                """.trimIndent()
                    )
                )
            }
            post("/new/email/{username}") {
                val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
                val userDto = emailService.addEmailToUser(
                    call.receiveText().trim('"'),
                    call.parameters.getOrFail("username"),
                    sessionId
                )
                call.respond(userDto)
            }
            post("/new/site/{username}/{emailAddress}") {
                val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
                val emailDto = siteService.addSiteToEmail(
                    call.receiveText().trim('"'),
                    call.parameters.getOrFail("emailAddress"),
                    call.parameters.getOrFail("username"),
                    sessionId
                )
                call.respond(emailDto)
            }
            post("/find/email/{username}") {
                val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
                val emailDto = emailService.getEmailFromUser(
                    call.receiveText().trim('"'),
                    call.parameters.getOrFail("username"),
                    sessionId
                )
                call.respondNullable(emailDto)
            }
            post("/find/site/{username}/{emailAddress}") {
                val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
                val siteDto = siteService.getSiteFromEmail(
                    call.receiveText().trim('"'),
                    call.parameters.getOrFail("emailAddress"),
                    call.parameters.getOrFail("username"),
                    sessionId
                )
                call.respondNullable(siteDto)
            }
            post("/login") {
                val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
                val login = call.receive<LoginDto>()
                sessionService.updatePasswordById(sessionId, login.password)
                val userDto = userService.getByName(
                    login.username,
                    sessionId
                )
                call.respondNullable(userDto)
            }
            post("/new/user") {
                val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
                val login = call.receive<LoginDto>()
                sessionService.updatePasswordById(sessionId, login.password)
                val userDto = userService.create(
                    login.username,
                    sessionId
                )
                call.respondNullable(userDto)
            }
            get("/logout") {
                val sessionId = UUID.fromString(call.sessions.get<SessionCookie>()!!.sessionId)
                sessionService.updatePasswordById(sessionId, null)
                call.respond(HttpStatusCode.OK)
            }
            static("/static") {
                resources()
            }
        }
    }.start(wait = true)
}

fun sha256(message: String): String {
    val input = message.toByteArray()
    val bytes = MessageDigest.getInstance("SHA-256").digest(input)
    return Base64.getUrlEncoder().encodeToString(bytes)
}