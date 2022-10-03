package com.lucasmdjl.passwordgenerator.jsclient.react

import com.lucasmdjl.passwordgenerator.common.dto.EmailDto
import com.lucasmdjl.passwordgenerator.common.dto.SiteDto
import com.lucasmdjl.passwordgenerator.common.dto.UserDto
import com.lucasmdjl.passwordgenerator.common.routes.EmailRoute
import com.lucasmdjl.passwordgenerator.common.routes.SiteRoute
import com.lucasmdjl.passwordgenerator.jsclient.jsonClient
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.InputType
import react.useState

private val scope = MainScope()

external interface PasswordGenProps : Props {
    var userDto: UserDto
    var masterPassword: String
    var addEmail: (String) -> Unit
    var removeEmail: (String) -> Unit
}

val PasswordGen = FC<PasswordGenProps> { props ->
    var emailDto by useState<EmailDto>()
    var siteDto by useState<SiteDto>()
    var password by useState<String>()

    DropList {
        this.name = "email"
        this.inputType = InputType.email
        this.list = props.userDto.emailList
        this.doOnChange = { emailAddress ->
            emailDto = null
            siteDto = null
            password = null
            if (props.userDto.hasEmail(emailAddress)) {
                emailDto = EmailDto(emailAddress, mutableListOf())
                scope.launch {
                    emailDto = checkEmail(props.userDto, emailAddress)
                }
            }
        }
        this.disableAdd = emailDto != null
        this.doOnAdd = { emailAddress ->
            if (emailAddress != "" && !props.userDto.hasEmail(emailAddress)) {
                props.addEmail(emailAddress)
                emailDto = EmailDto(emailAddress, mutableListOf())
                scope.launch {
                    val emailDtoTemp = addEmail(props.userDto, emailAddress)
                    emailDto = emailDtoTemp
                    if (emailDtoTemp == null) {
                        props.removeEmail(emailAddress)
                    }
                }
            }
        }
        this.doOnRemove = { emailAddress ->
            if (emailAddress != "" && props.userDto.hasEmail(emailAddress)) {
                props.removeEmail(emailAddress)
                emailDto = null
                siteDto = null
                password = null
                scope.launch {
                    val result = removeEmail(props.userDto, emailAddress)
                    if (result == null) {
                        props.addEmail(emailAddress)
                    }
                }
            }
        }
    }
    if (emailDto != null) {
        DropList {
            this.name = "site"
            this.inputType = InputType.text
            this.list = emailDto!!.siteList
            this.doOnChange = { siteName ->
                siteDto = null
                password = null
                if (emailDto!!.hasSite(siteName)) {
                    siteDto = SiteDto(siteName)
                    scope.launch {
                        siteDto = checkSite(props.userDto.username, emailDto!!, siteName)
                    }
                }
            }
            this.disableAdd = siteDto != null
            this.doOnAdd = { siteName ->
                if (siteName != "" && !emailDto!!.hasSite(siteName)) {
                    emailDto!!.addSite(siteName)
                    siteDto = SiteDto(siteName)
                    scope.launch {
                        val siteDtoTemp = addSite(props.userDto.username, emailDto!!, siteName)
                        siteDto = siteDtoTemp
                        if (siteDtoTemp == null) emailDto!!.removeSite(siteName)
                    }
                }
            }
            this.doOnRemove = { siteName ->
                if (siteName != "" && emailDto!!.hasSite(siteName)) {
                    emailDto!!.removeSite(siteName)
                    siteDto = null
                    password = null
                    scope.launch {
                        val result = removeSite(props.userDto.username, emailDto!!, siteName)
                        if (result == null) {
                            emailDto!!.addSite(siteName)
                        }
                    }
                }
            }
        }
    }
    Generator {
        this.username = props.userDto.username
        this.masterPassword = props.masterPassword
        this.emailAddress = emailDto?.emailAddress
        this.siteName = siteDto?.siteName
        this.password = password
        this.updatePassword = {
            password = it
        }
    }
}

suspend fun checkEmail(userDto: UserDto, emailAddress: String): EmailDto? =
    jsonClient.get(EmailRoute.Find(emailAddress, userDto.username)) {
    }.body()

suspend fun addEmail(userDto: UserDto, emailAddress: String): EmailDto? =
    jsonClient.post(EmailRoute.New(userDto.username)) {
        contentType(ContentType.Text.Plain)
        setBody(emailAddress)
    }.body()

suspend fun removeEmail(userDto: UserDto, emailAddress: String): Unit? =
    jsonClient.delete(EmailRoute.Delete(emailAddress, userDto.username)) {
    }.body()

suspend fun checkSite(username: String, emailDto: EmailDto, siteName: String): SiteDto? =
    jsonClient.get(SiteRoute.Find(siteName, emailDto.emailAddress, username)) {
    }.body()

suspend fun addSite(username: String, emailDto: EmailDto, siteName: String): SiteDto? =
    jsonClient.post(SiteRoute.New(emailDto.emailAddress, username)) {
        contentType(ContentType.Text.Plain)
        setBody(siteName)
    }.body()

suspend fun removeSite(username: String, emailDto: EmailDto, siteName: String): Unit? =
    jsonClient.delete(SiteRoute.Delete(siteName, emailDto.emailAddress, username)) {
    }.body()
