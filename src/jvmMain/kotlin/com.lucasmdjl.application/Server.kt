package com.lucasmdjl.application


import com.lucasmdjl.application.service.EmailService
import com.lucasmdjl.application.service.SiteService
import com.lucasmdjl.application.service.UserService
import com.lucasmdjl.application.service.impl.EmailServiceImpl
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
import io.ktor.server.util.*
import kotlinx.html.*
import java.security.MessageDigest
import java.util.*

val emailService: EmailService = EmailServiceImpl
val userService: UserService = UserServiceImpl
val siteService: SiteService = SiteServiceImpl

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

lateinit var password: String

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
        DatabaseFactory.init()
        routing {
            get("/") {
                call.respondHtml(HttpStatusCode.OK, HTML::index)
            }
            get("/password/{username}/{emailAddress}/{siteName}") {
                call.respondText(sha256("""
                    ${call.parameters.getOrFail("username")}
                    ${call.parameters.getOrFail("emailAddress")}
                    ${call.parameters.getOrFail("siteName")}
                    $password
                """.trimIndent()))
            }
            post("/new/email/{username}") {
                val userDto = emailService.addEmailToUser(
                    call.receiveText().trim('"'),
                    call.parameters.getOrFail("username")
                )
                call.respond(userDto)
            }
            post("/new/site/{emailAddress}") {
                val emailDto = siteService.addSiteToEmail(
                    call.receiveText().trim('"'),
                    call.parameters.getOrFail("emailAddress")
                )
                call.respond(emailDto)
            }
            post("/find/email/{username}") {
                val emailDto = emailService.getEmailFromUser(
                    call.receiveText().trim('"'),
                    call.parameters.getOrFail("username")
                )
                call.respondNullable(emailDto)
            }
            post("/find/site/{emailAddress}") {
                val siteDto = siteService.getSiteFromEmail(
                    call.receiveText().trim('"'),
                    call.parameters.getOrFail("emailAddress")
                )
                call.respondNullable(siteDto)
            }
            post("/login") {
                val login = call.receive<LoginDto>()
                password = login.password
                val userDto = userService.getByName(login.username)
                call.respondNullable(userDto)
            }
            post("/new/user") {
                val login = call.receive<LoginDto>()
                password = login.password
                val userDto = userService.create(login.username)
                call.respondNullable(userDto)
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