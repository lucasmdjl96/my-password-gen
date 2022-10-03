package com.lucasmdjl.passwordgenerator.jsclient.react

import com.lucasmdjl.passwordgenerator.common.dto.UserDto
import com.lucasmdjl.passwordgenerator.common.routes.UserRoute
import com.lucasmdjl.passwordgenerator.jsclient.CssClasses
import com.lucasmdjl.passwordgenerator.jsclient.dto.LoginDto
import com.lucasmdjl.passwordgenerator.jsclient.jsonClient
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.main
import react.dom.html.ReactHTML.span
import react.useState

val App = FC<Props> {
    var userDto by useState<UserDto>()
    var masterPassword by useState<String>()

    div {
        className = CssClasses.background
        main {
            className = CssClasses.container
            if (userDto == null) {
                div {
                    className = CssClasses.titleContainer
                    h1 {
                        className = CssClasses.title
                        +"Password Generator"
                    }
                }
                Login {
                    this.onLogin = { loginData: LoginDto ->
                        scope.launch {
                            userDto = loginUser(loginData.username)
                            masterPassword = loginData.password
                        }
                    }
                    this.onRegister = { loginData: LoginDto ->
                        scope.launch {
                            userDto = registerUser(loginData.username)
                            masterPassword = loginData.password
                        }
                    }
                }
            }

            if (userDto != null) {
                div {
                    className = CssClasses.titleContainer
                    h1 {
                        className = CssClasses.title
                        +"Password Generator"
                    }
                    button {
                        className = CssClasses.logOut
                        span {
                            className = CssClasses.materialIcon
                            +"logout"
                        }
                        onClick = {
                            userDto = null
                            masterPassword = null
                        }
                    }
                }
                PasswordGen {
                    this.userDto = userDto!!
                    this.masterPassword = masterPassword!!
                    this.addEmail = { emailAddress ->
                        userDto!!.addEmail(emailAddress)
                    }
                    this.removeEmail = { emailAddress ->
                        userDto!!.removeEmail(emailAddress)
                    }
                }
            }
        }
    }
}

suspend fun loginUser(username: String): UserDto? =
    if (username == "") null
    else jsonClient.post(UserRoute.Login()) {
        contentType(ContentType.Text.Plain)
        setBody(username)
    }.body()


suspend fun registerUser(username: String): UserDto? =
    if (username == "") null
    else jsonClient.post(UserRoute.Register()) {
        contentType(ContentType.Text.Plain)
        setBody(username)
    }.body()
