import csstype.*
import dto.LoginDto
import emotion.react.css
import react.FC
import react.Props
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
        css {
            center()
        }
        div {
            css {
                padding = 10.px
            }
            div {
                label {
                    css {
                        fontWeight = FontWeight.bold
                    }
                    +"Username"
                    htmlFor = "username"
                }
            }
            div {
                input {
                    id = "username"
                    type = InputType.text
                    value = username
                    onChange = {
                        username = it.target.value
                    }
                }
            }
        }
        div {
            css {
                padding = 10.px
            }
            div {
                label {
                    css {
                        fontWeight = FontWeight.bold
                    }
                    +"Password"
                    htmlFor = "password"
                }
            }
            div {
                input {
                    id = "password"
                    type = InputType.password
                    value = password
                    onChange = {
                        password = it.target.value
                    }
                }
            }
        }
        div {
            button {
                css {
                    marginRight = 20.px
                }
                +"Login"
                onClick = {
                    props.onLogin(LoginDto(username, password))
                }
            }
            button {
                css {
                    marginLeft = 20.px
                }
                +"Register"
                onClick = {
                    props.onRegister(LoginDto(username, password))
                }
            }
        }
    }
}

fun PropertiesBuilder.center() {
    left = 40.pc
    width = 15.pc
    border = Border(3.px, LineStyle.solid, NamedColor.black)
    padding = 10.px
    textAlign = TextAlign.center
}