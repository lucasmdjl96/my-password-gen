/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.model

import com.mypasswordgen.server.tables.Emails
import com.mypasswordgen.server.tables.Users
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class User(id: EntityID<UUID>) : Entity<UUID>(id) {

    var username by Users.username
    val emails by Email referrersOn Emails.userId
    var session by Session referencedOn Users.sessionId
    var lastEmail by Email optionalReferencedOn Users.lastEmailId


    companion object : EntityClass<UUID, User>(Users)

}
