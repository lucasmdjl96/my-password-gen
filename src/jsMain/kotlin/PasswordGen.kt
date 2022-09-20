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
import react.dom.html.ReactHTML.h3
import react.useState

private val scope = MainScope()

external interface PasswordGenProps : Props {
    var userDto: UserDto
    var reloadUser: (UserDto) -> Unit
}

val PasswordGen = FC<PasswordGenProps> { props ->
    var emailDto by useState<EmailDto>()
    var siteDto by useState<SiteDto>()
    var password by useState<String>()

    h3 {
        +"Password Generator"
    }
        DropList {
            this.name = "email"
            this.inputType = InputType.email
            this.list = props.userDto.emailDtoList
            this.doOnChange = { emailAddress ->
                scope.launch {
                    emailDto = checkEmailAndGetPages(props.userDto, emailAddress)
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
        }
    if (emailDto != null) {
        DropList {
            this.name = "site"
            this.inputType = InputType.text
            this.list = emailDto!!.siteDtoList
            this.doOnChange = { siteName ->
                scope.launch {
                    siteDto = checkPage(emailDto!!, siteName)
                    password = null
                }
            }
            this.disableAdd = siteDto != null
            this.doOnAdd = { siteName ->
                if (siteName != "") {
                    scope.launch {
                        val emailDtoTemp = addPage(emailDto!!.name, siteName)
                        emailDto = emailDtoTemp
                        siteDto = emailDtoTemp.siteDtoList.find { it.name == siteName }
                    }
                }
            }
        }
    }
    Generator {
        this.username = props.userDto.name
        this.emailAddress = emailDto?.name
        this.siteName = siteDto?.name
        this.password = password
        this.updatePassword = {
            password = it
        }
    }
}

suspend fun checkEmailAndGetPages(userDto: UserDto, emailAddress: String): EmailDto? =
    if (emailAddress !in userDto.emailDtoList.map(EmailDto::name)) null
    else jsonClient.post("$endpoint/find/email/${userDto.name}") {
        contentType(ContentType.Text.Plain)
        setBody(emailAddress)
    }.body()


suspend fun addEmail(username: String, emailAddress: String): UserDto =
    jsonClient.post("$endpoint/new/email/$username") {
        contentType(ContentType.Text.Plain)
        setBody(emailAddress)
    }.body()


suspend fun checkPage(emailDto: EmailDto, siteName: String): SiteDto? =
    if (siteName !in emailDto.siteDtoList.map(SiteDto::name)) null
    else jsonClient.post("$endpoint/find/site/${emailDto.name}") {
        contentType(ContentType.Text.Plain)
        setBody(siteName)
    }.body()


suspend fun addPage(emailAddress: String, siteName: String): EmailDto =
    jsonClient.post("$endpoint/new/site/$emailAddress") {
        contentType(ContentType.Text.Plain)
        setBody(siteName)
    }.body()

