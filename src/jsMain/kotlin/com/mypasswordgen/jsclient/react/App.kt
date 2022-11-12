package com.mypasswordgen.jsclient.react

import com.mypasswordgen.common.dto.client.UserClientDto
import com.mypasswordgen.common.dto.server.UserServerDto
import com.mypasswordgen.common.routes.UserRoute
import com.mypasswordgen.jsclient.*
import com.mypasswordgen.jsclient.dto.InitialState
import com.mypasswordgen.jsclient.dto.LoginDto
import com.mypasswordgen.jsclient.dto.UserClient
import csstype.Color
import emotion.react.css
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.main
import react.useState

val App = { initialState: InitialState ->
    FC<Props> {
        var userClient by useState<UserClient>()
        var masterPassword by useState<String>()
        var online by useState(initialState.online && window.navigator.onLine)
        var background by useState(initialState.initialBackgroundColor)
        var cookiesAccepted by useState(initialState.cookiesAccepted)
        var showCookieBanner by useState(initialState.cookiesAccepted == null)
        var keyboardUp by useState(false)
        var connectionOn by useState(window.navigator.onLine)

        window.visualViewport.addEventListener("resize", {
            keyboardUp = window.visualViewport.height < 500 && window.visualViewport.width < 500
        })

        window.addEventListener("offline", {
            connectionOn = false
            online = false
        })

        window.addEventListener("online", {
            connectionOn = true
        })


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
                    this.loggedIn = userClient != null
                    this.online = online
                }
                if (userClient == null) {
                    onKeyDown = withReceiver {
                        if (ctrlKey && key == "Enter") {
                            ::click on getHtmlElementById("onlineToggle")!!
                        }
                    }
                    OnlineToggle {
                        this.cookiesAccepted = cookiesAccepted
                        this.online = online
                        this.updateOnline = { newOnline -> online = newOnline }
                        this.connectionOn = connectionOn
                    }
                    Login {
                        this.onLogin = { loginData: LoginDto ->
                            if (online) scope.launch {
                                userClient = loginUser(loginData.username)
                                masterPassword = loginData.password
                            } else {
                                userClient = UserClient(loginData.username)
                                masterPassword = loginData.password
                            }
                        }
                        this.onRegister = { loginData: LoginDto ->
                            if (online) scope.launch {
                                userClient = registerUser(loginData.username)
                                masterPassword = loginData.password
                            } else {
                                userClient = UserClient(loginData.username)
                                masterPassword = loginData.password
                            }
                        }
                    }
                } else {
                    onKeyDown = withReceiver {
                        if (ctrlKey && key == "Backspace") {
                            ::click on getHtmlElementById("logout")!!
                        }
                    }
                    LogoutButton {
                        this.reset = {
                            if (online) scope.launch {
                                logoutUser(userClient!!.username)
                            }
                            userClient = null
                            masterPassword = null
                        }
                    }
                    PasswordGen {
                        this.userClient = userClient!!
                        this.masterPassword = masterPassword!!
                        this.addEmail = { emailAddress ->
                            userClient!!.addEmail(emailAddress)
                        }
                        this.removeEmail = { emailAddress ->
                            userClient!!.removeEmail(emailAddress)
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
            } else if (!keyboardUp) {
                Foot {}
            }
        }
    }
}

suspend fun loginUser(username: String): UserClient? {
    val response = jsonClient.post(UserRoute.Login()) {
        contentType(ContentType.Application.Json)
        setBody(UserServerDto(username))
    }
    return if (response.status != HttpStatusCode.OK) null
    else {
        val userClientDto = response.body<UserClientDto>()
        val emailList = mutableListOf<String>()
        database.readTransaction<Email>() {
            for (emailId in userClientDto.emailIdList) {
                get<Email>(emailId) { email ->
                    if (email != null) emailList.add(email.emailAddress)
                }
            }
        }.awaitCompletion()
        UserClient(username, emailList)
    }
}


suspend fun registerUser(username: String): UserClient? {
    val response = jsonClient.post(UserRoute.Register()) {
        contentType(ContentType.Application.Json)
        setBody(UserServerDto(username))
    }
    return if (response.status != HttpStatusCode.OK) null
    else UserClient(username)
}

suspend fun logoutUser(username: String) {
    jsonClient.patch(UserRoute.Logout()) {
        contentType(ContentType.Application.Json)
        setBody(UserServerDto(username))
    }
}

fun getHtmlElementById(id: String) = document.getElementById(id) as? HTMLElement