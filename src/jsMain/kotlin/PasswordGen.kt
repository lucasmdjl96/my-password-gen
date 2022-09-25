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
    var reloadUser: (UserDto) -> Unit
}

val PasswordGen = FC<PasswordGenProps> { props ->
    var emailDto by useState<EmailDto>()
    var siteDto by useState<SiteDto>()
    var password by useState<String>()

    DropList {
        this.name = "email"
        this.inputType = InputType.email
        this.list = props.userDto.emailDtoList
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
                    val userDto = addEmail(props.userDto.name, emailAddress)
                    emailDto = userDto.emailDtoList.find { it.name == emailAddress }
                    props.reloadUser(userDto)
                }
            }
        }
        this.doOnRemove = { emailAddress ->
            if (emailAddress != "") {
                scope.launch {
                    val userDto = removeEmail(props.userDto.name, emailAddress)
                    emailDto = null
                    props.reloadUser(userDto)
                }
            }
        }
    }
    if (emailDto != null) {
        DropList {
            this.name = "site"
            this.inputType = InputType.text
            this.list = emailDto!!.siteDtoList
            this.doOnChange = { siteName ->
                scope.launch {
                    siteDto = checkSite(props.userDto.name, emailDto!!, siteName)
                    password = null
                }
            }
            this.disableAdd = siteDto != null
            this.doOnAdd = { siteName ->
                if (siteName != "") {
                    scope.launch {
                        val emailDtoTemp = addSite(props.userDto.name, emailDto!!.name, siteName)
                        emailDto = emailDtoTemp
                        siteDto = emailDtoTemp.siteDtoList.find { it.name == siteName }
                    }
                }
            }
            this.doOnRemove = { siteName ->
                if (siteName != "") {
                    scope.launch {
                        emailDto = removeSite(props.userDto.name, emailDto!!.name, siteName)
                        siteDto = null
                    }
                }
            }
        }
    }
    Generator {
        this.username = props.userDto.name
        this.masterPassword = props.masterPassword
        this.emailAddress = emailDto?.name
        this.siteName = siteDto?.name
        this.password = password
        this.updatePassword = {
            password = it
        }
    }
}

suspend fun checkEmail(userDto: UserDto, emailAddress: String): EmailDto? =
    if (!userDto.hasEmail(emailAddress)) null
    else jsonClient.get("$endpoint/email/find/$emailAddress") {
        parameter("username", userDto.name)
    }.body()


suspend fun addEmail(username: String, emailAddress: String): UserDto =
    jsonClient.post("$endpoint/email/new") {
        contentType(ContentType.Text.Plain)
        setBody(emailAddress)
        parameter("username", username)
    }.body()

suspend fun removeEmail(username: String, emailAddress: String): UserDto =
    jsonClient.delete("$endpoint/email/delete/$emailAddress") {
        parameter("username", username)
    }.body()

suspend fun checkSite(username: String, emailDto: EmailDto, siteName: String): SiteDto? =
    if (!emailDto.hasSite(siteName)) null
    else jsonClient.get("$endpoint/site/find/$siteName") {
        parameter("username", username)
        parameter("emailAddress", emailDto.name)
    }.body()


suspend fun addSite(username: String, emailAddress: String, siteName: String): EmailDto =
    jsonClient.post("$endpoint/site/new") {
        contentType(ContentType.Text.Plain)
        setBody(siteName)
        parameter("username", username)
        parameter("emailAddress", emailAddress)
    }.body()

suspend fun removeSite(username: String, emailAddress: String, siteName: String): EmailDto =
    jsonClient.delete("$endpoint/site/delete/$siteName") {
        parameter("username", username)
        parameter("emailAddress", emailAddress)
    }.body()
