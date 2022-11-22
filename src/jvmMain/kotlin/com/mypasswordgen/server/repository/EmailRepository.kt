/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.repository

import com.mypasswordgen.server.model.Email
import com.mypasswordgen.server.model.User
import java.util.*

interface EmailRepository {

    fun createAndGetId(emailAddress: String, userId: UUID): UUID
    fun createAndGetId(emailAddress: String, user: User) = createAndGetId(emailAddress, user.id.value)

    fun getById(id: UUID): Email?

    fun getByAddressAndUser(emailAddress: String, userId: UUID): Email?
    fun getByAddressAndUser(emailAddress: String, user: User) = getByAddressAndUser(emailAddress, user.id.value)

    fun delete(email: Email)

}
