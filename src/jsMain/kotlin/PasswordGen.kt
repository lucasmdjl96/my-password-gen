import dto.EmailDto
import dto.SiteDto
import dto.UserDto
import io.ktor.client.call.*
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
            scope.launch {
                emailDto = checkEmail(props.userDto, emailAddress)
                siteDto = null
                password = null
            }
        }
        this.disableAdd = emailDto != null
        this.doOnAdd = { emailAddress ->
            if (emailAddress != "") {
                scope.launch {
                    val emailDtoTemp = addEmail(props.userDto, emailAddress)
                    emailDto = emailDtoTemp
                    if (emailDtoTemp != null) {
                        props.addEmail(emailAddress)
                    }
                }
            }
        }
        this.doOnRemove = { emailAddress ->
            if (emailAddress != "") {
                scope.launch {
                    val result = removeEmail(props.userDto, emailAddress)
                    if (result != null) {
                        emailDto = null
                        siteDto = null
                        password = null
                        props.removeEmail(emailAddress)
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
                scope.launch {
                    siteDto = checkSite(props.userDto.username, emailDto!!, siteName)
                    password = null
                }
            }
            this.disableAdd = siteDto != null
            this.doOnAdd = { siteName ->
                if (siteName != "") {
                    scope.launch {
                        val siteDtoTemp = addSite(props.userDto.username, emailDto!!, siteName)
                        siteDto = siteDtoTemp
                        if (siteDtoTemp != null) emailDto!!.addSite(siteName)
                    }
                }
            }
            this.doOnRemove = { siteName ->
                if (siteName != "") {
                    scope.launch {
                        val result = removeSite(props.userDto.username, emailDto!!, siteName)
                        if (result != null) {
                            emailDto!!.removeSite(siteName)
                            siteDto = null
                            password = null
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
    if (!userDto.hasEmail(emailAddress)) null
    else jsonClient.get("$endpoint/email/find/$emailAddress") {
        parameter("username", userDto.username)
    }.body()


suspend fun addEmail(userDto: UserDto, emailAddress: String): EmailDto? =
    if (userDto.hasEmail(emailAddress)) null
    else jsonClient.post("$endpoint/email/new") {
        contentType(ContentType.Text.Plain)
        setBody(emailAddress)
        parameter("username", userDto.username)
    }.body()

suspend fun removeEmail(userDto: UserDto, emailAddress: String): Unit? =
    if (!userDto.hasEmail(emailAddress)) null
    else jsonClient.delete("$endpoint/email/delete/$emailAddress") {
        parameter("username", userDto.username)
    }.body()

suspend fun checkSite(username: String, emailDto: EmailDto, siteName: String): SiteDto? =
    if (!emailDto.hasSite(siteName)) null
    else jsonClient.get("$endpoint/site/find/$siteName") {
        parameter("username", username)
        parameter("emailAddress", emailDto.emailAddress)
    }.body()


suspend fun addSite(username: String, emailDto: EmailDto, siteName: String): SiteDto? =
    if (emailDto.hasSite(siteName)) null
    else jsonClient.post("$endpoint/site/new") {
        contentType(ContentType.Text.Plain)
        setBody(siteName)
        parameter("username", username)
        parameter("emailAddress", emailDto.emailAddress)
    }.body()

suspend fun removeSite(username: String, emailDto: EmailDto, siteName: String): Unit? =
    if (!emailDto.hasSite(siteName)) null
    else jsonClient.delete("$endpoint/site/delete/$siteName") {
        parameter("username", username)
        parameter("emailAddress", emailDto.emailAddress)
    }.body()
