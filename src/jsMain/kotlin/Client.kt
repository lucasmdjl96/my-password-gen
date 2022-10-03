import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.browser.document
import kotlinx.browser.window
import react.App
import react.create
import react.dom.client.createRoot

fun main() {
    val container = document.getElementById("root")!!
    val app = App.create()
    createRoot(container).render(app)
}

val clipboard = window.navigator.clipboard

val jsonClient = HttpClient {
    install(ContentNegotiation) {
        json()
    }
    install(Resources)
    defaultRequest {
        host = "localhost"
        port = 8443
        url { protocol = URLProtocol.HTTPS }
    }
    expectSuccess = true
    HttpResponseValidator {
        handleResponseExceptionWithRequest { exception, _ ->
            val clientException = exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
            val exceptionResponse = clientException.response
            if (exceptionResponse.status == HttpStatusCode.Unauthorized) {
                window.location.reload()
            }
        }
    }
}
