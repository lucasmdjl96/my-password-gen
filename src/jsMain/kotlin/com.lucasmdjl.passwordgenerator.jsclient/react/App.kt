package com.lucasmdjl.passwordgenerator.jsclient.react

import com.lucasmdjl.passwordgenerator.common.dto.client.UserClientDto
import com.lucasmdjl.passwordgenerator.common.dto.server.UserServerDto
import com.lucasmdjl.passwordgenerator.common.routes.UserRoute
import com.lucasmdjl.passwordgenerator.jsclient.CssClasses
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

val App = { initialBackgroundColor: String ->
    FC<Props> {
        var userClientDto by useState<UserClientDto>()
        var masterPassword by useState<String>()
        var online by useState(true)
        var background by useState(initialBackgroundColor)

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
                        localStorage.setItem("backgroundColor", color)
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
                            onClick = { online = false }
                        }
                        div {
                            css(CssClasses.toggleContainer) {
                                backgroundColor = if (online) toggleOnColor else toggleOffColor
                            }
                            span {
                                className = CssClasses.materialIconOutlined
                                +if (online) "toggle_on" else "toggle_off"
                                onClick = {
                                    online = !online
                                }
                            }
                        }
                        div {
                            +"Online"
                            onClick = { online = true }
                        }
                    }
                    Login {
                        this.onLogin = { loginData: LoginDto ->
                            if (online) {
                                scope.launch {
                                    userClientDto = loginUser(loginData.username)
                                    masterPassword = loginData.password
                                }
                            } else {
                                userClientDto = UserClientDto(loginData.username)
                                masterPassword = loginData.password
                            }
                        }
                        this.onRegister = { loginData: LoginDto ->
                            if (online) {
                                scope.launch {
                                    userClientDto = registerUser(loginData.username)
                                    masterPassword = loginData.password
                                }
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
