import crypto.sha256
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
        className = CssClasses.buttonContainer
        button {
            +"Generate Password"
            disabled = props.siteName == null
            onClick = {
                scope.launch {
                    props.updatePassword(
                        generatePassword(
                            props.username,
                            props.emailAddress!!,
                            props.siteName!!,
                            props.masterPassword
                        )
                    )
                }
            }
        }
    }
    if (props.password != null) {
        div {
            className = CssClasses.password
            +props.password!!
            button {
                +"\uD83D\uDCCB"
                onClick = {
                    clipboard.writeText(props.password!!)
                }
            }
        }
    }
}

suspend fun generatePassword(username: String, emailAddress: String, siteName: String, masterPassword: String) =
    sha256(
        """
            $username
            $emailAddress
            $siteName
            $masterPassword
        """.trimIndent()
    )
