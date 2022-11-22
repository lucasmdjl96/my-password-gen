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
import com.mypasswordgen.server.tables.Sites
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class Email(id: EntityID<UUID>) : Entity<UUID>(id) {

    var emailAddress by Emails.emailAddress
    var user by User referencedOn Emails.userId
    val sites by Site referrersOn Sites.emailId

    override fun toString(): String {
        return "[Email#${id.value}: $emailAddress]"
    }

    companion object : EntityClass<UUID, Email>(Emails)

}
