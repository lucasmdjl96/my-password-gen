import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h4

private val scope = MainScope()

external interface GeneratorProps : Props {
    var username: String
    var emailAddress: String?
    var siteName: String?
    var password: String?
    var updatePassword: (String) -> Unit
}

val Generator = FC<GeneratorProps> { props ->
    h4 {
        +"Generate Password"
    }
    button {
        +"Generate"
        disabled = props.siteName == null
        onClick = {
            scope.launch {
                props.updatePassword(generatePassword(props.username, props.emailAddress!!, props.siteName!!))
            }
        }
    }
    if (props.password != null) {
        div {
            +props.password!!
        }
    }
}

suspend fun generatePassword(username: String, emailAddress: String, siteName: String) =
    jsonClient.get("$endpoint/password/$username/$emailAddress/$siteName").body<String>()
