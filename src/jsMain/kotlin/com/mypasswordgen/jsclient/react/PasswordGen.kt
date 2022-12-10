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

import com.mypasswordgen.common.dto.client.EmailClientDto
import com.mypasswordgen.common.dto.client.SiteClientDto
import com.mypasswordgen.common.dto.server.EmailServerDto
import com.mypasswordgen.common.dto.server.SiteServerDto
import com.mypasswordgen.common.routes.EmailRoute
import com.mypasswordgen.common.routes.SiteRoute
import com.mypasswordgen.jsclient.database
import com.mypasswordgen.jsclient.dto.EmailClient
import com.mypasswordgen.jsclient.dto.SiteClient
import com.mypasswordgen.jsclient.dto.UserClient
import com.mypasswordgen.jsclient.externals.*
import com.mypasswordgen.jsclient.jsonClient
import com.mypasswordgen.jsclient.model.Email
import com.mypasswordgen.jsclient.model.Site
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import react.FC
import react.Props
import react.dom.html.InputType
import react.useState

external interface PasswordGenProps : Props {
    var userClient: UserClient
    var masterPassword: String
    var addEmail: (String) -> Unit
    var removeEmail: (String) -> Unit
    var online: Boolean
}

val PasswordGen = FC<PasswordGenProps> { props ->
    var emailClient by useState<EmailClient>()
    var siteClient by useState<SiteClient>()
    var password by useState<String>()

    DropList {
        this.name = "email"
        this.inputType = InputType.email
        this.set = props.userClient.emailSet
        this.autoFocus = true
        this.doOnChange = { emailAddress ->
            if (props.online) {
                emailClient = null
                siteClient = null
                password = null
                if (props.userClient.hasEmail(emailAddress)) {
                    emailClient = EmailClient(emailAddress)
                    scope.launch {
                        emailClient = checkEmail(emailAddress)
                    }
                }
            } else {
                password = null
                emailClient = if (emailClient != null) {
                    EmailClient(emailAddress, emailClient!!.siteSet)
                } else {
                    EmailClient(emailAddress)
                }
            }
        }
        this.disableAdd = emailClient != null
        this.doOnAdd = { emailAddress ->
            if (!props.userClient.hasEmail(emailAddress)) {
                props.addEmail(emailAddress)
                emailClient = EmailClient(emailAddress)
                if (props.online) scope.launch {
                    val emailDtoTemp = addEmail(emailAddress)
                    emailClient = emailDtoTemp
                    if (emailDtoTemp == null) {
                        props.removeEmail(emailAddress)
                    }
                }
            }
        }
        this.doOnRemove = { emailAddress ->
            emailClient = null
            siteClient = null
            password = null
            if (props.userClient.hasEmail(emailAddress)) {
                props.removeEmail(emailAddress)
                if (props.online) scope.launch {
                    val result = removeEmail(emailAddress)
                    if (result == null) {
                        props.addEmail(emailAddress)
                    }
                }
            }
        }
        this.doOnEnter = withReceiver {
            if (key == "Enter" && emailClient == null) {
                ::click on getHtmlElementById("emailAdd")!!
            } else if (key == "Enter" && emailClient != null) {
                ::focus on getHtmlElementById("site")!!
            } else if (ctrlKey && key == "Delete") {
                ::click on getHtmlElementById("emailRemove")!!
            } else if (ctrlKey && key == "ArrowDown") {
                ::focus on getHtmlElementById("site")!!
            }
        }
    }
    if (emailClient != null && emailClient?.emailAddress != "") {
        DropList {
            this.name = "site"
            this.inputType = InputType.text
            this.set = emailClient!!.siteSet
            this.autoFocus = false
            this.doOnChange = { siteName ->
                siteClient = null
                password = null
                if (emailClient!!.hasSite(siteName)) {
                    siteClient = SiteClient(siteName)
                    if (props.online) scope.launch {
                        siteClient = checkSite(siteName)
                    }
                }
            }
            this.disableAdd = siteClient != null
            this.doOnAdd = { siteName ->
                if (!emailClient!!.hasSite(siteName)) {
                    emailClient!!.addSite(siteName)
                    siteClient = SiteClient(siteName)
                    if (props.online) scope.launch {
                        val siteDtoTemp = addSite(siteName)
                        siteClient = siteDtoTemp
                        if (siteDtoTemp == null) emailClient!!.removeSite(siteName)
                    }
                }
            }
            this.doOnRemove = { siteName ->
                siteClient = null
                password = null
                if (emailClient!!.hasSite(siteName)) {
                    emailClient!!.removeSite(siteName)
                    if (props.online) scope.launch {
                        val result = removeSite(siteName)
                        if (result == null) {
                            emailClient!!.addSite(siteName)
                        }
                    }
                }
            }
            this.doOnEnter = withReceiver {
                if (key == "Enter" && siteClient == null) {
                    ::click on getHtmlElementById("siteAdd")!!
                } else if (key == "Enter" && siteClient != null) {
                    ::click on getHtmlElementById("passwordGenerator")!!
                } else if (ctrlKey && key == "c") {
                    ::click on getHtmlElementById("copyButton")!!
                } else if (ctrlKey && key == "ArrowUp") {
                    ::focus on getHtmlElementById("email")!!
                } else if (ctrlKey && key == "Delete") {
                    ::click on getHtmlElementById("siteRemove")!!
                }
            }
        }
    }
    Generator {
        this.username = props.userClient.username
        this.masterPassword = props.masterPassword
        this.emailAddress = emailClient?.emailAddress
        this.siteName = siteClient?.siteName
        this.password = password
        this.updatePassword = {
            password = it
        }
    }
}

suspend fun checkEmail(emailAddress: String): EmailClient? {
    val response = jsonClient.get(EmailRoute.Find(emailAddress))
    return if (response.status != HttpStatusCode.OK) null
    else {
        val emailClientDto = response.body<EmailClientDto>()
        val siteSet = mutableSetOf<String>()
        database.readTransaction<Site> {
            for (siteId in emailClientDto.siteIdSet) {
                get<Site>(siteId) { site ->
                    if (site != null) siteSet.add(site.siteName)
                }
            }
        }.awaitCompletion()
        EmailClient(emailAddress, siteSet)
    }
}

suspend fun addEmail(emailAddress: String): EmailClient? {
    val response = jsonClient.post(EmailRoute.New()) {
        contentType(ContentType.Application.Json)
        setBody(EmailServerDto(emailAddress))
    }
    return if (response.status != HttpStatusCode.OK) null
    else {
        val emailClientDto = response.body<EmailClientDto>()
        database.readWriteTransaction<Email> {
            add<Email>(Email(emailClientDto.id, emailAddress))
        }.awaitCompletion()
        EmailClient(emailAddress, mutableSetOf())
    }
}

suspend fun removeEmail(emailAddress: String): Unit? {
    val response = jsonClient.delete(EmailRoute.Delete(emailAddress))
    return if (response.status != HttpStatusCode.OK) null
    else {
        val emailClientDto = response.body<EmailClientDto>()
        database.biReadWriteTransaction<Email, Site> {
            delete<Email>(emailClientDto.id)
            for (siteId in emailClientDto.siteIdSet) {
                delete<Site>(siteId)
            }
        }.awaitCompletion()
    }
}

suspend fun checkSite(siteName: String): SiteClient? {
    val response = jsonClient.get(SiteRoute.Find(siteName))
    return if (response.status != HttpStatusCode.OK) null
    else SiteClient(siteName)
}

suspend fun addSite(siteName: String): SiteClient? {
    val response = jsonClient.post(SiteRoute.New()) {
        contentType(ContentType.Application.Json)
        setBody(SiteServerDto(siteName))
    }
    return if (response.status != HttpStatusCode.OK) null
    else {
        val siteClientDto = response.body<SiteClientDto>()
        database.readWriteTransaction<Site> {
            add<Site>(Site(siteClientDto.id, siteName))
        }.awaitCompletion()
        SiteClient(siteName)
    }
}

suspend fun removeSite(siteName: String): Unit? {
    val response = jsonClient.delete(SiteRoute.Delete(siteName))
    return if (response.status != HttpStatusCode.OK) null
    else {
        val siteClientDto = response.body<SiteClientDto>()
        database.readWriteTransaction<Site> {
            delete<Site>(siteClientDto.id)
        }.awaitCompletion()
    }
}

inline fun <T, S> withReceiver(crossinline block: T.() -> S): (T) -> S = {
    it.block()
}

infix fun ((HTMLElement?) -> Unit?).on(element: HTMLElement?): Unit? = this(element)

fun click(element: HTMLElement?) = element?.click()
fun focus(element: HTMLElement?) = element?.focus()
