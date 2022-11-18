package com.mypasswordgen.jsclient.react

import com.mypasswordgen.common.dto.*
import com.mypasswordgen.common.routes.SessionRoute
import com.mypasswordgen.jsclient.*
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader
import org.w3c.files.get
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.useState

val jsonFormatter = Json {
    prettyPrint = true
}

external interface FileManagerProps : Props {
    var loggedIn: Boolean
}

val FileManager = FC<FileManagerProps> { props ->
    var sessionData by useState<FullSessionServerDto>()

    div {
        className = CssClasses.fileManagerContainer
        if (!props.loggedIn) div {
            +"Import"
            onClick = {
                ::click on getHtmlElementById("import")!!
            }
            input {
                id = "import"
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
                                val fullSessionServer =
                                    jsonFormatter.decodeFromString<FullSessionServerDto>(reader.result as String)
                                val usernames = fullSessionServer.users.map(FullUserServerDto::username)
                                if (usernames.none { username -> username == "FILL_THIS_IN" }
                                    && usernames.allUnique()
                                ) scope.launch {
                                    uploadData(fullSessionServer)
                                }
                            }
                            reader.readAsText(file)
                        }
                    }
                }
            }
        }
        div {
            +"Export"
            id = "test"
            onClick = {
                if (sessionData == null) scope.launch {
                    sessionData = downloadSessionData()
                    ::click on getHtmlElementById("export")!!
                } else {
                    ::click on getHtmlElementById("export")!!
                }
            }
            ReactHTML.a {
                id = "export"
                hidden = true
                if (sessionData != null) {
                    download = "my-password-gen.json"
                    val file = Blob(
                        arrayOf(jsonFormatter.encodeToString(sessionData)),
                        BlobPropertyBag(type = "application/json")
                    )
                    href = URL.createObjectURL(file)
                }
            }
        }
    }
}

suspend fun downloadSessionData(): FullSessionServerDto? {
    val response = jsonClient.get(SessionRoute.Export()) {
    }
    if (response.status != HttpStatusCode.OK) return null
    val fullSessionClient = response.body<FullSessionClientDto>()
    val fullSessionServer = FullSessionServerDto()
    database.biReadTransaction<EmailIDBDto, SiteIDBDto> {
        for (fullUserClient in fullSessionClient.users) {
            val fullUserServerDto = FullUserServerDto("FILL_THIS_IN")
            fullSessionServer.addUser(fullUserServerDto)
            for (fullEmailClient in fullUserClient.emails) {
                get<EmailIDBDto>(fullEmailClient.id) { email ->
                    if (email != null) {
                        val fullEmailServerDto = FullEmailServerDto(email.emailAddress)
                        fullUserServerDto.addEmail(fullEmailServerDto)
                        for (fullSiteClient in fullEmailClient.sites) {
                            get<SiteIDBDto>(fullSiteClient.id) { site ->
                                if (site != null) {
                                    val fullSiteServerDto = FullSiteServerDto(site.siteName)
                                    fullEmailServerDto.addSite(fullSiteServerDto)
                                }
                            }
                        }
                    }
                }
            }
        }
    }.awaitCompletion()
    return fullSessionServer
}

suspend fun mockDownloadData(): FullSessionServerDto? {
    return FullSessionServerDto(
        mutableListOf(
            FullUserServerDto(
                "abc",
                mutableListOf(
                    FullEmailServerDto(
                        "a@b",
                        mutableListOf(
                            FullSiteServerDto("ABC"),
                            FullSiteServerDto("DEF"),
                        )
                    ),
                    FullEmailServerDto("c@d")
                )
            ),
            FullUserServerDto("def")
        )
    )
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

suspend fun dummyUploadData(fullSessionServerDto: FullSessionServerDto) {
    console.log(Json.encodeToString(fullSessionServerDto))
}

fun <T> List<T>.allUnique(): Boolean {
    val seen = mutableListOf<T>()
    for (t in this) {
        if (t in seen) return false
        else seen.add(t)
    }
    return true
}
