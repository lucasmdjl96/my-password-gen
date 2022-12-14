/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.jsclient.react

import com.mypasswordgen.common.dto.client.UserClientDto
import com.mypasswordgen.common.dto.server.UserServerDto
import com.mypasswordgen.common.routes.UserRoute
import com.mypasswordgen.jsclient.CssClasses
import com.mypasswordgen.jsclient.database
import com.mypasswordgen.jsclient.dto.InitialState
import com.mypasswordgen.jsclient.dto.LoginDto
import com.mypasswordgen.jsclient.dto.UserClient
import com.mypasswordgen.jsclient.externals.*
import com.mypasswordgen.jsclient.jsonClient
import com.mypasswordgen.jsclient.model.Email
import com.mypasswordgen.jsclient.model.User
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
        var importExportType by useState(initialState.importExportType)
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
            div {//to fix infinite autofocus on mobile
                ref = autoAnimateRefCallBack()
                if (!keyboardUp && online) FileManager {
                    this.loggedIn = userClient != null
                    this.username = userClient?.username
                    this.importExportType = importExportType
                    this.updateImportExportType = {
                        importExportType = it
                    }
                }
            }
            MainPopup { }
            main {
                ref = autoAnimateRefCallBack()
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
                        if (ctrlKey && key == "i") {
                            preventDefault()
                            ::click on getHtmlElementById("import")
                        }
                        if (ctrlKey && key == "e") {
                            preventDefault()
                            ::click on getHtmlElementById("export")
                        }
                        if (ctrlKey && key == "q") {
                            preventDefault()
                            ::click on getHtmlElementById("importExportTypeToggle")
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
                        if (ctrlKey && key == "e") {
                            preventDefault()
                            ::click on getHtmlElementById("export")
                        }
                        if (ctrlKey && key == "q") {
                            preventDefault()
                            ::click on getHtmlElementById("importExportTypeToggle")
                        }
                    }
                    LogoutButton {
                        this.reset = {
                            if (online) scope.launch {
                                logoutUser()
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
            ref = autoAnimateRefCallBack()
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
    val response = jsonClient.get(UserRoute.Login(username))
    return if (response.status != HttpStatusCode.OK) null
    else {
        val userClientDto = response.body<UserClientDto>()
        val emailSet = mutableSetOf<String>()
        database.readTransaction<Email> {
            for (emailId in userClientDto.emailIdSet) {
                get<Email>(emailId) { email ->
                    if (email != null) emailSet.add(email.emailAddress)
                }
            }
        }.awaitCompletion()
        UserClient(username, emailSet)
    }
}


suspend fun registerUser(username: String): UserClient? {
    val response = jsonClient.post(UserRoute.Register()) {
        contentType(ContentType.Application.Json)
        setBody(UserServerDto(username))
    }
    return if (response.status != HttpStatusCode.OK) null
    else {
        val userClientDto = response.body<UserClientDto>()
        database.readWriteTransaction<User> {
            add<User>(User(userClientDto.id, username))
        }
        UserClient(username)
    }
}

suspend fun logoutUser() {
    jsonClient.patch(UserRoute.Logout())
}

fun getHtmlElementById(id: String) = document.getElementById(id) as? HTMLElement
