/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.kotest.dto

import com.mypasswordgen.common.dto.client.EmailClientDto
import com.mypasswordgen.common.dto.client.SiteClientDto
import com.mypasswordgen.common.dto.client.UserClientDto
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.uuid


fun SiteClientDto.Companion.arb() = Arb.uuid().map { siteId -> SiteClientDto(siteId.toString()) }
fun EmailClientDto.Companion.arb() =
    Arb.bind(Arb.uuid(), Arb.set(Arb.uuid().map { it.toString() })) { emailId, siteIdList ->
        EmailClientDto(emailId.toString(), siteIdList)
    }

fun UserClientDto.Companion.arb() =
    Arb.bind(Arb.uuid(), Arb.set(Arb.uuid().map { it.toString() })) { userId, emailIdList ->
        UserClientDto(userId.toString(), emailIdList)
    }
