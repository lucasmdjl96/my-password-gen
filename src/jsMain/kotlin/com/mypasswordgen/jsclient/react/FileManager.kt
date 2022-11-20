package com.mypasswordgen.jsclient.react

import com.mypasswordgen.common.dto.*
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
import org.w3c.dom.url.URL
import org.w3c.files.FileReader
import org.w3c.files.get
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.useEffect
import react.useState
import kotlin.js.Promise

external interface FileManagerProps : Props {
    var loggedIn: Boolean
    var username: String?
}

val FileManager = FC<FileManagerProps> { props ->
    var sessionData by useState<FullSessionServerDto>()
    var userData by useState<FullUserServerDto>()

    lateinit var exportPromise: Promise<Unit>

    useEffect(props.loggedIn) {
        sessionData = null
        userData = null
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
            onChange = { event ->
                if (event.target.files != null && event.target.files!!.length == 1) {
                    val file = event.target.files!![0]!!
                    if (file.type == "application/json") {
                        val reader = FileReader()
                        reader.onload = {
                            val text = reader.result as String
                            console.log(text)
                            when (DownloadCode.fromText(text)) {

                                SESSION -> run {
                                    console.log("SESSION")
                                    val session = DownloadSession.dataFromText(text)
                                    val usernames = session.users.map(FullUserServerDto::username)
                                    if (usernames.none { username -> username == "FILL_THIS_IN" }) scope.launch {
                                        uploadData(session)
                                    }
                                }

                                USER -> run {
                                    console.log("USER")
                                    val user = DownloadUser.dataFromText(text)
                                    scope.launch {
                                        uploadData(user)
                                    }
                                }

                                null -> run {
                                    console.log("NULL")
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
            onClick = {
                if (sessionData == null && !props.loggedIn) scope.launch {
                    sessionData = downloadSessionData()
                } else if (userData == null && props.loggedIn) scope.launch {
                    userData = downloadUserData(props.username!!)
                } else {
                    exportPromise.then {
                        ::click on getHtmlElementById("exportButton")!!
                    }
                }
            }
        }
        ReactHTML.a {
            id = "exportButton"
            hidden = true
            exportPromise = Promise { resolve, reject ->
                if (sessionData != null && !props.loggedIn) {
                    download = "my-password-gen-session.json"
                    val file = DownloadSession(sessionData!!).toFile()
                    href = URL.createObjectURL(file)
                    resolve(Unit)
                }
                if (userData != null && props.loggedIn) {
                    download = "my-password-gen-user.json"
                    val file = DownloadUser(userData!!).toFile()
                    href = URL.createObjectURL(file)
                    resolve(Unit)
                }
            }.then {
                ::click on getHtmlElementById("exportButton")!!
            }
        }
    }
}


suspend fun downloadSessionData(): FullSessionServerDto? {
    val response = jsonClient.get(SessionRoute.Export())
    if (response.status != HttpStatusCode.OK) return null
    val fullSessionClient = response.body<FullSessionClientDto>()
    var fullSessionServer: FullSessionServerDto? = null
    database.biReadTransaction<EmailIDBDto, SiteIDBDto> {
        fullSessionServer = FullSessionServerDto {
            for (fullUserClient in fullSessionClient.users) {
                +FullUserServerDto("FILL_THIS_IN") {
                    loadUserData(fullUserClient, this)
                }
            }
        }
    }.awaitCompletion()
    return fullSessionServer
}

private inline fun IDBTransaction.loadUserData(
    fromFullUserClient: FullUserClientDto,
    fullUserServerBuilder: FullUserServerDto.Builder
) {
    with(fullUserServerBuilder) {
        for (fullEmailClient in fromFullUserClient.emails) {
            get<EmailIDBDto>(fullEmailClient.id) { email ->
                if (email != null) +FullEmailServerDto(email.emailAddress) {
                    for (fullSiteClient in fullEmailClient.sites) {
                        get<SiteIDBDto>(fullSiteClient.id) { site ->
                            if (site != null) +FullSiteServerDto(site.siteName)
                        }
                    }
                }
            }
        }
    }
}

suspend fun downloadUserData(username: String): FullUserServerDto? {
    val response = jsonClient.get(UserRoute.Export(username))
    if (response.status != HttpStatusCode.OK) return null
    val fullUserClient = response.body<FullUserClientDto>()
    var fullUserServer: FullUserServerDto? = null
    database.biReadTransaction<EmailIDBDto, SiteIDBDto> {
        fullUserServer = FullUserServerDto(username) {
            loadUserData(fullUserClient, this)
        }
    }.awaitCompletion()
    return fullUserServer
}

suspend fun uploadData(fullSessionServerDto: FullSessionServerDto) {
    val result = jsonClient.post(SessionRoute.Import()) {
        setBody(fullSessionServerDto)
        contentType(ContentType.Application.Json)
    }
    if (result.status != HttpStatusCode.OK) return
    val session = result.body<SessionIDBDto>()
    database.biReadWriteTransaction<EmailIDBDto, SiteIDBDto> {
        onSuccessOf(clear<EmailIDBDto>(), clear<SiteIDBDto>()) {
            for (user in session.users) {
                for (email in user.emails) {
                    add(email)
                    for (site in email.sites) {
                        add(site)
                    }
                }
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
    database.biReadWriteTransaction<EmailIDBDto, SiteIDBDto> {
        for (email in user.emails) {
            add(email)
            for (site in email.sites) {
                add(site)
            }
        }
    }.awaitCompletion()
}
