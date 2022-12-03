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
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useEffect
import react.useState

private val importButton: HTMLElement?
    get() = getHtmlElementById("importButton")
private val exportButton: HTMLElement?
    get() = getHtmlElementById("exportButton")

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
                ::click on importButton!!
            }
        }
        if (!props.loggedIn) ImportButton {
            this.exportType = exportType
        }
        div {
            +"Export"
            id = "export"
            onClick = {
                if (sessionData == null && !props.loggedIn) scope.launch {
                    sessionData = downloadSessionData()
                } else if (userData == null && props.loggedIn) scope.launch {
                    userData = downloadUserData(props.username!!)
                } else {
                    ::click on exportButton!!
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
            +if (exportType == ExportType.FILE) "description" else "qr_code"
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
