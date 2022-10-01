import dto.LoginDto
import react.FC
import react.Props
import react.dom.html.AutoComplete
import react.dom.html.InputType
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.useState

external interface LoginProps : Props {
    var onLogin: (LoginDto) -> Unit
    var onRegister: (LoginDto) -> Unit
}

val Login = FC<LoginProps> { props ->
    var username by useState("")
    var password by useState("")


    div {
        className = CssClasses.inputContainer
        label {
            +"Username"
            htmlFor = "username"
            hidden = true
        }
        input {
            id = "username"
            placeholder = "Username"
            autoComplete = AutoComplete.username
            type = InputType.text
            value = username
            onChange = {
                username = it.target.value
            }
        }
    }
    div {
        className = CssClasses.inputContainer
        label {
            +"Password"
            htmlFor = "password"
            hidden = true
        }
        Password {
            this.password = password
            this.onChange = {
                password = it
            }
        }
    }
    div {
        className = CssClasses.buttonContainer
        button {
            +"Log in"
            onClick = {
                props.onLogin(LoginDto(username, password))
            }
        }
        button {
            +"Register"
            onClick = {
                props.onRegister(LoginDto(username, password))
            }
        }
    }

}
