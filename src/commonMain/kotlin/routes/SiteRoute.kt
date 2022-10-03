package routes

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/site")
class SiteRoute {

    @Serializable
    @Resource("new")
    class New(val emailAddress: String, val username: String)

    @Serializable
    @Resource("find/{siteName}")
    class Find(val siteName: String, val emailAddress: String, val username: String)

    @Serializable
    @Resource("delete/{siteName}")
    class Delete(val siteName: String, val emailAddress: String, val username: String)

}
