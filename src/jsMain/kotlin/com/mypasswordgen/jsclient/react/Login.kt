package com.mypasswordgen.jsclient.react

import com.mypasswordgen.jsclient.CssClasses
import com.mypasswordgen.jsclient.dto.LoginDto
import react.FC
import react.Props
import react.dom.aria.ariaLabel
import react.dom.html.AutoComplete
import react.dom.html.InputType
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
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
        input {
            id = "username"
            placeholder = "Username"
            ariaLabel = "Username"
            autoComplete = AutoComplete.off
            type = InputType.text
            value = username
            autoFocus = true
            onChange = {
                username = it.target.value
            }
            onKeyDown = withReceiver {
                if (!ctrlKey && key == "Enter") {
                    ::focus on getHtmlElementById("password")!!
                } else if (ctrlKey && key == "ArrowDown") {
                    ::focus on getHtmlElementById("password")!!
                }
            }
        }
    }
    div {
        className = CssClasses.inputContainer
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
            id = "login"
            onClick = {
                if (username != "" && password != "") props.onLogin(LoginDto(username, password))
            }
        }
        button {
            +"Register"
            id = "register"
            onClick = {
                if (username != "" && password != "") props.onRegister(LoginDto(username, password))
            }
        }
    }

}
