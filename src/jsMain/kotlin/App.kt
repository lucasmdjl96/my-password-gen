import dto.UserDto
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.main
import react.useState

private val scope = MainScope()

val App = FC<Props> {
    var userDto by useState<UserDto>()
    var masterPassword by useState<String>()

    div {
        className = CssClasses.background
        main {
            className = CssClasses.container
            h1 {
                className = CssClasses.title
                +"Password Generator"
            }
            if (userDto == null) {
                Login {
                    this.onLogin = { loginData ->
                        scope.launch {
                            userDto = loginUser(loginData.username)
                            masterPassword = loginData.password
                        }
                    }
                    this.onRegister = { loginData ->
                        scope.launch {
                            userDto = registerUser(loginData.username)
                            masterPassword = loginData.password
                        }
                    }
                }
            }

            if (userDto != null) {
                ReactHTML.button {
                    className = CssClasses.logOut
                    +"\u23FB"
                    onClick = {
                        userDto = null
                        masterPassword = null
                    }
                }
                PasswordGen {
                    this.userDto = userDto!!
                    this.masterPassword = masterPassword!!
                    this.reloadUser = {
                        userDto = it
                    }
                }
            }
        }
    }
}

suspend fun loginUser(username: String): UserDto? =
    if (username == "") null
    else jsonClient.post("$endpoint/user/login") {
        contentType(ContentType.Text.Plain)
        setBody(username)
    }.body()


suspend fun registerUser(username: String): UserDto? =
    if (username == "") null
    else jsonClient.post("$endpoint/user/register") {
        contentType(ContentType.Text.Plain)
        setBody(username)
    }.body()



