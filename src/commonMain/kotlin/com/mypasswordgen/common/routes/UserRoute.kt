/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.common.routes

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/user")
class UserRoute {

    @Serializable
    @Resource("/login/{username}")
    class Login(val username: String, val parent: UserRoute = UserRoute())

    @Serializable
    @Resource("/register")
    class Register(val parent: UserRoute = UserRoute())

    @Serializable
    @Resource("/logout")
    class Logout(val parent: UserRoute = UserRoute())

    @Serializable
    @Resource("/import")
    class Import(val parent: UserRoute = UserRoute())

    @Serializable
    @Resource("/export/{username}")
    class Export(val username: String, val parent: UserRoute = UserRoute())

}
