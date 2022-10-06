package com.lucasmdjl.passwordgenerator.jsclient.react

import com.lucasmdjl.passwordgenerator.common.dto.client.EmailClientDto
import com.lucasmdjl.passwordgenerator.common.dto.client.SiteClientDto
import com.lucasmdjl.passwordgenerator.common.dto.client.UserClientDto
import com.lucasmdjl.passwordgenerator.common.dto.server.EmailServerDto
import com.lucasmdjl.passwordgenerator.common.dto.server.SiteServerDto
import com.lucasmdjl.passwordgenerator.common.routes.EmailRoute
import com.lucasmdjl.passwordgenerator.common.routes.SiteRoute
import com.lucasmdjl.passwordgenerator.jsclient.jsonClient
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.InputType
import react.useState

external interface PasswordGenProps : Props {
    var userClientDto: UserClientDto
    var masterPassword: String
    var addEmail: (String) -> Unit
    var removeEmail: (String) -> Unit
    var online: Boolean
}

val PasswordGen = FC<PasswordGenProps> { props ->
    var emailClientDto by useState<EmailClientDto>()
    var siteClientDto by useState<SiteClientDto>()
    var password by useState<String>()

    DropList {
        this.name = "email"
        this.inputType = InputType.email
        this.list = props.userClientDto.emailList
        this.doOnChange = { emailAddress ->
            if (props.online) {
                emailClientDto = null
                siteClientDto = null
                password = null
                if (props.userClientDto.hasEmail(emailAddress)) {
                    emailClientDto = EmailClientDto(emailAddress)
                    scope.launch {
                        emailClientDto = checkEmail(props.userClientDto, emailAddress)
                    }
                }
            } else {
                password = null
                emailClientDto = if (emailClientDto != null) {
                    EmailClientDto(emailAddress, emailClientDto!!.siteList)
                } else {
                    EmailClientDto(emailAddress)
                }
            }
        }
        this.disableAdd = emailClientDto != null
        this.doOnAdd = { emailAddress ->
            if (emailAddress != "" && !props.userClientDto.hasEmail(emailAddress)) {
                props.addEmail(emailAddress)
                emailClientDto = EmailClientDto(emailAddress)
                if (props.online) scope.launch {
                    val emailDtoTemp = addEmail(props.userClientDto, emailAddress)
                    emailClientDto = emailDtoTemp
                    if (emailDtoTemp == null) {
                        props.removeEmail(emailAddress)
                    }
                }
            }
        }
        this.doOnRemove = { emailAddress ->
            emailClientDto = null
            siteClientDto = null
            password = null
            if (emailAddress != "" && props.userClientDto.hasEmail(emailAddress)) {
                props.removeEmail(emailAddress)
                if (props.online) scope.launch {
                    val result = removeEmail(props.userClientDto, emailAddress)
                    if (result == null) {
                        props.addEmail(emailAddress)
                    }
                }
            }
        }
    }
    if (emailClientDto != null) {
        DropList {
            this.name = "site"
            this.inputType = InputType.text
            this.list = emailClientDto!!.siteList
            this.doOnChange = { siteName ->
                siteClientDto = null
                password = null
                if (emailClientDto!!.hasSite(siteName)) {
                    siteClientDto = SiteClientDto(siteName)
                    if (props.online) scope.launch {
                        siteClientDto = checkSite(props.userClientDto.username, emailClientDto!!, siteName)
                    }
                }
            }
            this.disableAdd = siteClientDto != null
            this.doOnAdd = { siteName ->
                if (siteName != "" && !emailClientDto!!.hasSite(siteName)) {
                    emailClientDto!!.addSite(siteName)
                    siteClientDto = SiteClientDto(siteName)
                    if (props.online) scope.launch {
                        val siteDtoTemp = addSite(props.userClientDto.username, emailClientDto!!, siteName)
                        siteClientDto = siteDtoTemp
                        if (siteDtoTemp == null) emailClientDto!!.removeSite(siteName)
                    }
                }
            }
            this.doOnRemove = { siteName ->
                siteClientDto = null
                password = null
                if (siteName != "" && emailClientDto!!.hasSite(siteName)) {
                    emailClientDto!!.removeSite(siteName)
                    if (props.online) scope.launch {
                        val result = removeSite(props.userClientDto.username, emailClientDto!!, siteName)
                        if (result == null) {
                            emailClientDto!!.addSite(siteName)
                        }
                    }
                }
            }
        }
    }
    Generator {
        this.username = props.userClientDto.username
        this.masterPassword = props.masterPassword
        this.emailAddress = emailClientDto?.emailAddress
        this.siteName = siteClientDto?.siteName
        this.password = password
        this.updatePassword = {
            password = it
        }
    }
}

suspend fun checkEmail(userClientDto: UserClientDto, emailAddress: String): EmailClientDto? =
    jsonClient.get(EmailRoute.Find(emailAddress, userClientDto.username)) {
    }.body()

suspend fun addEmail(userClientDto: UserClientDto, emailAddress: String): EmailClientDto? =
    jsonClient.post(EmailRoute.New()) {
        contentType(ContentType.Application.Json)
        setBody(EmailServerDto(emailAddress, userClientDto.username))
    }.body()

suspend fun removeEmail(userClientDto: UserClientDto, emailAddress: String): Unit? =
    jsonClient.delete(EmailRoute.Delete(emailAddress, userClientDto.username)) {
    }.body()

suspend fun checkSite(username: String, emailClientDto: EmailClientDto, siteName: String): SiteClientDto? =
    jsonClient.get(SiteRoute.Find(siteName, emailClientDto.emailAddress, username)) {
    }.body()

suspend fun addSite(username: String, emailClientDto: EmailClientDto, siteName: String): SiteClientDto? =
    jsonClient.post(SiteRoute.New()) {
        contentType(ContentType.Application.Json)
        setBody(SiteServerDto(siteName, emailClientDto.emailAddress, username))
    }.body()

suspend fun removeSite(username: String, emailClientDto: EmailClientDto, siteName: String): Unit? =
    jsonClient.delete(SiteRoute.Delete(siteName, emailClientDto.emailAddress, username)) {
    }.body()
