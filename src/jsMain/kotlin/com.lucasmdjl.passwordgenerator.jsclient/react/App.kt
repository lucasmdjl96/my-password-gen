package com.lucasmdjl.passwordgenerator.jsclient.react

import com.lucasmdjl.passwordgenerator.common.dto.client.UserClientDto
import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.common.routes.SessionRoute
import com.lucasmdjl.passwordgenerator.common.routes.UserRoute
import com.lucasmdjl.passwordgenerator.jsclient.CssClasses
import com.lucasmdjl.passwordgenerator.jsclient.dto.InitialState
import com.lucasmdjl.passwordgenerator.jsclient.dto.LoginDto
import com.lucasmdjl.passwordgenerator.jsclient.hsl
import com.lucasmdjl.passwordgenerator.jsclient.jsonClient
import csstype.Color
import emotion.react.css
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.browser.localStorage
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.main
import react.dom.html.ReactHTML.span
import react.useState

private val toggleOnColor = hsl(200, 100, 45)
private val toggleOffColor = hsl(0, 0, 50)

val App = { initialState: InitialState ->
    FC<Props> {
        var userClientDto by useState<UserClientDto>()
        var masterPassword by useState<String>()
        var online by useState(initialState.online)
        var background by useState(initialState.initialBackgroundColor)
        var cookiesAccepted by useState(initialState.cookiesAccepted)

        div {
            css(CssClasses.background) {
                backgroundColor = Color(background)
            }
            div {
                className = CssClasses.colorPickerContainer
                label {
                    +"Background Color"
                    htmlFor = "backgroundColor"
                    hidden = true
                }
                input {
                    className = CssClasses.colorPicker
                    id = "backgroundColor"
                    type = InputType.color
                    value = background
                    onChange = { event ->
                        val color = event.target.value
                        background = color
                        if (cookiesAccepted == true) localStorage.setItem("backgroundColor", color)
                    }
                }
            }
            main {
                className = CssClasses.container
                if (userClientDto == null) {
                    div {
                        className = CssClasses.titleContainer
                        h1 {
                            className = CssClasses.title
                            +"Password Generator"
                        }
                    }
                    div {
                        className = CssClasses.onlineToggle
                        div {
                            +"Offline"
                            onClick = {
                                if (cookiesAccepted == true) {
                                    online = false
                                    localStorage.setItem("online", "false")
                                }
                            }
                        }
                        div {
                            css(CssClasses.toggleContainer) {
                                backgroundColor = if (online) toggleOnColor else toggleOffColor
                            }
                            span {
                                className = CssClasses.materialIconOutlined
                                +if (online) "toggle_on" else "toggle_off"
                                onClick = {
                                    if (cookiesAccepted == true) {
                                        val newOnline = !online
                                        online = newOnline
                                        localStorage.setItem("online", "$newOnline")
                                        if (newOnline) scope.launch {
                                            updateSession()
                                        }
                                    }
                                }
                            }
                        }
                        div {
                            +"Online"
                            onClick = {
                                if (cookiesAccepted == true) {
                                    online = true
                                    localStorage.setItem("online", "true")
                                    scope.launch {
                                        updateSession()
                                    }
                                }
                            }
                        }
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
                }
                if (userClientDto != null) {
                    div {
                        className = CssClasses.titleContainer
                        h1 {
                            className = CssClasses.title
                            +"${if (!online) "Offline " else ""}Password Generator"
                        }
                        button {
                            className = CssClasses.logOut
                            span {
                                className = CssClasses.materialIcon
                                +"logout"
                            }
                            onClick = {
                                userClientDto = null
                                masterPassword = null
                            }
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
            if (cookiesAccepted == null) {
                CookieBanner {
                    this.background = background
                    this.updateCookie = { accepted -> cookiesAccepted = accepted }
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
