import crypto.sha256
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div

private val scope = MainScope()

external interface GeneratorProps : Props {
    var username: String
    var masterPassword: String
    var emailAddress: String?
    var siteName: String?
    var password: String?
    var updatePassword: (String) -> Unit
}

val Generator = FC<GeneratorProps> { props ->
    div {
        className = buttonContainer
        button {
            +"Generate Password"
            disabled = props.siteName == null
            onClick = {
                scope.launch {
                    props.updatePassword(generatePassword2(
                        props.username,
                        props.emailAddress!!,
                        props.siteName!!,
                        props.masterPassword
                    ))
                }
            }
        }
    }
    if (props.password != null) {
        div {
            className = passwordClass
            +props.password!!
            button {
                +"\uD83D\uDCCB"
                onClick = {
                    navigator.clipboard.writeText(props.password!!)
                }
            }
        }
    }
}

suspend fun generatePassword(username: String, emailAddress: String, siteName: String) =
    jsonClient.get("$endpoint/password/$username/$emailAddress/$siteName").body<String>()

suspend fun generatePassword2(username: String, emailAddress: String, siteName: String, masterPassword: String) =
    sha256(
        """
            $username
            $emailAddress
            $siteName
            $masterPassword
        """.trimIndent()
    )
