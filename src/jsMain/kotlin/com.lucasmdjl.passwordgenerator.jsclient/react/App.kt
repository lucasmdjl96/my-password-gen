package com.lucasmdjl.passwordgenerator.jsclient.react

import com.lucasmdjl.passwordgenerator.common.dto.client.UserClientDto
import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.common.routes.SessionRoute
import com.lucasmdjl.passwordgenerator.common.routes.UserRoute
import com.lucasmdjl.passwordgenerator.jsclient.CssClasses
import com.lucasmdjl.passwordgenerator.jsclient.dto.InitialState
import com.lucasmdjl.passwordgenerator.jsclient.dto.LoginDto
import com.lucasmdjl.passwordgenerator.jsclient.jsonClient
import csstype.Color
import emotion.react.css
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.main
import react.useState

val App = { initialState: InitialState ->
    FC<Props> {
        var userClientDto by useState<UserClientDto>()
        var masterPassword by useState<String>()
        var online by useState(initialState.online)
        var background by useState(initialState.initialBackgroundColor)
        var cookiesAccepted by useState(initialState.cookiesAccepted)
        var showCookieBanner by useState(initialState.cookiesAccepted == null)

        div {
            css(CssClasses.background) {
                backgroundColor = Color(background)
            }
            ColorPicker {
                this.cookiesAccepted = cookiesAccepted
                this.background = background
                this.updateBackground = { color -> background = color }
            }
            main {
                className = CssClasses.container
                TitleContainer {
                    this.loggedIn = userClientDto != null
                    this.online = online
                }
                if (userClientDto == null) {
                    OnlineToggle {
                        this.cookiesAccepted = cookiesAccepted
                        this.online = online
                        this.updateOnline = { newOnline -> online = newOnline }
                    }
                    Login {
                        this.onLogin = { loginData: LoginDto ->
                            if (online) scope.launch {
                                userClientDto = loginUser(loginData.username)
                                masterPassword = loginData.password
                            } else {
                                userClientDto = UserClientDto(loginData.username)
                                masterPassword = loginData.password
                            }
                        }
                        this.onRegister = { loginData: LoginDto ->
                            if (online) scope.launch {
                                userClientDto = registerUser(loginData.username)
                                masterPassword = loginData.password
                            } else {
                                userClientDto = UserClientDto(loginData.username)
                                masterPassword = loginData.password
                            }
                        }
                    }
                } else {
                    LogoutButton {
                        this.reset = {
                            userClientDto = null
                            masterPassword = null
                        }
                    }
                    PasswordGen {
                        this.userClientDto = userClientDto!!
                        this.masterPassword = masterPassword!!
                        this.addEmail = { emailAddress ->
                            userClientDto!!.addEmail(emailAddress)
                        }
                        this.removeEmail = { emailAddress ->
                            userClientDto!!.removeEmail(emailAddress)
                        }
                        this.online = online
                    }
                }
            }
            if (showCookieBanner) {
                CookieBanner {
                    this.background = background
                    this.updateCookie = { accepted -> cookiesAccepted = accepted }
                    this.dismiss = { showCookieBanner = false }
                }
            }
        }
    }
}

suspend fun loginUser(username: String): UserClientDto? =
    if (username == "") null
    else jsonClient.post(UserRoute.Login()) {
        contentType(ContentType.Application.Json)
        setBody(UserServerDto(username))
    }.body()


suspend fun registerUser(username: String): UserClientDto? =
    if (username == "") null
    else jsonClient.post(UserRoute.Register()) {
        contentType(ContentType.Application.Json)
        setBody(UserServerDto(username))
    }.body()

suspend fun updateSession() {
    jsonClient.put(SessionRoute())
}
