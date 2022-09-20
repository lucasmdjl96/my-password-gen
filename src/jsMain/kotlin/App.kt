import dto.LoginDto
import dto.UserDto
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.useState

private val scope = MainScope()

val App = FC<Props> {
    var userDto by useState<UserDto>()

    if (userDto == null) {
        Login {
            this.onLogin = { loginData ->
                scope.launch {
                    userDto = loginUser(loginData)
                    console.log(userDto)
                }
            }
            this.onRegister = { loginData ->
                scope.launch {
                    userDto = registerUser(loginData)
                }
            }
        }
    }

    if (userDto != null) {
        PasswordGen {
            this.userDto = userDto!!
            this.reloadUser = {
                userDto = it
            }
        }
    }
}

suspend fun loginUser(login: LoginDto): UserDto? =
    if (login.username == "") null
    else jsonClient.post("$endpoint/login") {
        contentType(ContentType.Application.Json)
        setBody(login)
    }.body()


suspend fun registerUser(login: LoginDto): UserDto? =
    if (login.username == "") null
    else jsonClient.post("$endpoint/new/user") {
        contentType(ContentType.Application.Json)
        setBody(login)
    }.body()

