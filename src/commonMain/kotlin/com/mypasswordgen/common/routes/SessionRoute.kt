package com.mypasswordgen.common.routes

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/session")
class SessionRoute {

    @Serializable
    @Resource("/update")
    class Update(val parent: SessionRoute = SessionRoute())

    @Serializable
    @Resource("/import")
    class Import(val parent: SessionRoute = SessionRoute())

    @Serializable
    @Resource("/export")
    class Export(val parent: SessionRoute = SessionRoute())

}
