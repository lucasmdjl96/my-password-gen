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

import com.mypasswordgen.common.dto.fullClient.FullSessionClientDto
import com.mypasswordgen.common.dto.fullClient.FullUserClientDto
import com.mypasswordgen.common.dto.fullServer.FullEmailServerDto
import com.mypasswordgen.common.dto.fullServer.FullSessionServerDto
import com.mypasswordgen.common.dto.fullServer.FullSiteServerDto
import com.mypasswordgen.common.dto.fullServer.FullUserServerDto
import com.mypasswordgen.common.dto.idb.SessionIDBDto
import com.mypasswordgen.common.dto.idb.UserIDBDto
import com.mypasswordgen.common.routes.SessionRoute
import com.mypasswordgen.common.routes.UserRoute
import com.mypasswordgen.jsclient.*
import com.mypasswordgen.jsclient.dto.DownloadCode
import com.mypasswordgen.jsclient.dto.DownloadCode.SESSION
import com.mypasswordgen.jsclient.dto.DownloadCode.USER
import com.mypasswordgen.jsclient.dto.DownloadSession
import com.mypasswordgen.jsclient.dto.DownloadUser
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import org.w3c.files.FileReader
import org.w3c.files.get
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.useEffect
import react.useState

external interface FileManagerProps : Props {
    var loggedIn: Boolean
    var username: String?
}

val FileManager = FC<FileManagerProps> { props ->
    var sessionData by useState<FullSessionServerDto>()
    var userData by useState<FullUserServerDto>()
    var exportType: ExportType by useState(ExportType.FILE)

    useEffect(props.loggedIn) {
        if (props.loggedIn) sessionData = null
        else userData = null
    }

    div {
        className = CssClasses.fileManagerContainer
        if (!props.loggedIn) div {
            +"Import"
            id = "import"
            onClick = {
                ::click on getHtmlElementById("importButton")!!
            }
        }
        if (!props.loggedIn) input {
            id = "importButton"
            hidden = true
            type = InputType.file
            accept = ".json,application/json"
            multiple = false
            value = ""
            onChange = { event ->
                if (event.target.files != null && event.target.files!!.length == 1) {
                    val file = event.target.files!![0]!!
                    if (file.type == "application/json") {
                        val reader = FileReader()
                        reader.onload = {
                            val text = reader.result as String
                            val successPopup = getHtmlElementById("successPopup")
                            val errorPopup = getHtmlElementById("errorPopup")
                            when (DownloadCode.fromText(text)) {
                                SESSION -> run {
                                    try {
                                        val session = DownloadSession.dataFromText(text)
                                        scope.launch {
                                            uploadData(session)
                                            successPopup?.innerText = "Import successful."
                                            ::click on successPopup
                                        }
                                    } catch (e: SerializationException) {
                                        errorPopup?.innerText = "Import failed. Malformed session data."
                                        ::click on errorPopup
                                    }
                                }

                                USER -> run {
                                    try {
                                        val user = DownloadUser.dataFromText(text)
                                        scope.launch {
                                            uploadData(user)
                                            successPopup?.innerText = "Import successful."
                                            ::click on successPopup
                                        }
                                    } catch (e: SerializationException) {
                                        errorPopup?.innerText = "Import failed. Malformed user data."
                                        ::click on errorPopup
                                    }
                                }

                                null -> run {
                                    errorPopup?.innerText = "Import failed. Message code not recognized."
                                    ::click on errorPopup
                                }
                            }
                        }
                        reader.readAsText(file)
                    }
                }
            }
        }
        div {
            +"Export"
            id = "export"
            onClick = { event ->
                event.stopPropagation()
                if (sessionData == null && !props.loggedIn) scope.launch {
                    sessionData = downloadSessionData()
                } else if (userData == null && props.loggedIn) scope.launch {
                    userData = downloadUserData(props.username!!)
                } else {
                    ::click on getHtmlElementById("exportButton")!!
                }
            }
        }
        ExportButton {
            this.loggedIn = props.loggedIn
            this.sessionData = sessionData
            this.userData = userData
            this.unsetUserData = { userData = null }
            this.exportType = exportType
        }
        div {
            className = CssClasses.materialIconOutlined
            +if (exportType == ExportType.FILE) "radio_button_unchecked" else "radio_button_checked"
            onClick = {
                exportType = if (exportType == ExportType.FILE) ExportType.QR else ExportType.FILE
            }
        }
    }
}


suspend fun downloadSessionData(): FullSessionServerDto? {
    val response = jsonClient.get(SessionRoute.Export())
    if (response.status != HttpStatusCode.OK) return null
    val fullSessionClient = response.body<FullSessionClientDto>()
    val fullSessionServer = FullSessionServerDto()
    database.triReadTransaction<User, Email, Site> {
        for (fullUserClient in fullSessionClient.users) {
            get<User>(fullUserClient.id) { user ->
                if (user != null) {
                    val fullUserServer = FullUserServerDto(user.username)
                    fullSessionServer.addUser(fullUserServer)
                    loadUserData(fullUserClient, fullUserServer)
                }
            }
        }
    }.awaitCompletion()
    return fullSessionServer
}

suspend fun downloadUserData(username: String): FullUserServerDto? {
    val response = jsonClient.get(UserRoute.Export(username))
    if (response.status != HttpStatusCode.OK) return null
    val fullUserClient = response.body<FullUserClientDto>()
    val fullUserServer = FullUserServerDto(username)
    database.biReadTransaction<Email, Site> {
        loadUserData(fullUserClient, fullUserServer)
    }.awaitCompletion()
    return fullUserServer
}

private inline fun IDBTransaction.loadUserData(
    fromFullUserClient: FullUserClientDto,
    intoFullUserServer: FullUserServerDto
) {
    for (fullEmailClient in fromFullUserClient.emails) {
        get<Email>(fullEmailClient.id) { email ->
            if (email != null) {
                val fullEmailServer = FullEmailServerDto(email.emailAddress)
                intoFullUserServer.addEmail(fullEmailServer)
                for (fullSiteClient in fullEmailClient.sites) {
                    get<Site>(fullSiteClient.id) { site ->
                        if (site != null) {
                            val fullSiteServer = FullSiteServerDto(site.siteName)
                            fullEmailServer.addSite(fullSiteServer)
                        }
                    }
                }
            }
        }
    }
}

suspend fun uploadData(fullSessionServerDto: FullSessionServerDto) {
    val result = jsonClient.post(SessionRoute.Import()) {
        setBody(fullSessionServerDto)
        contentType(ContentType.Application.Json)
    }
    if (result.status != HttpStatusCode.OK) return
    val session = result.body<SessionIDBDto>()
    database.triReadWriteTransaction<User, Email, Site> {
        onSuccessOf(clear<User>(), clear<Email>(), clear<Site>()) {
            for (user in session.users) {
                storeUserData(user)
            }
        }
    }.awaitCompletion()
}

suspend fun uploadData(fullUserServerDto: FullUserServerDto) {
    val result = jsonClient.post(UserRoute.Import()) {
        setBody(fullUserServerDto)
        contentType(ContentType.Application.Json)
    }
    if (result.status != HttpStatusCode.OK) return
    val user = result.body<UserIDBDto>()
    database.triReadWriteTransaction<User, Email, Site> {
        storeUserData(user)
    }.awaitCompletion()
}

inline fun IDBTransaction.storeUserData(user: UserIDBDto) {
    add<User>(user.toUser())
    for (email in user.emails) {
        add<Email>(email.toEmail())
        for (site in email.sites) {
            add<Site>(site.toSite())
        }
    }
}
