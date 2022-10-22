package com.lucasmdjl.passwordgenerator.jsclient.react

import com.lucasmdjl.passwordgenerator.jsclient.CssClasses
import com.lucasmdjl.passwordgenerator.jsclient.dto.LoginDto
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
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
            autoFocus = true
            onChange = {
                username = it.target.value
            }
            onKeyDown = { event ->
                if (!event.ctrlKey && event.key == "Enter") {
                    (document.getElementById("password")!! as HTMLElement).focus()
                } else if (event.ctrlKey && event.key == "ArrowDown") {
                    (document.getElementById("password")!! as HTMLElement).focus()
                }
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
            id = "login"
            onClick = {
                props.onLogin(LoginDto(username, password))
            }
        }
        button {
            +"Register"
            id = "register"
            onClick = {
                props.onRegister(LoginDto(username, password))
            }
        }
    }

}
