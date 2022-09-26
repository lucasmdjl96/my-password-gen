import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.document
import kotlinx.browser.window
import react.create
import react.dom.client.createRoot

fun main() {
    val container = document.getElementById("root")!!
    console.log(container)
    val app = App.create()
    createRoot(container).render(app)
}

val endpoint = window.location.origin

val navigator = window.navigator

val jsonClient = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}