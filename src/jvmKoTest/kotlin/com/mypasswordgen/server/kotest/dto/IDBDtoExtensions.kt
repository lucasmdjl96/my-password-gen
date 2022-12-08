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

import com.mypasswordgen.common.dto.idb.EmailIDBDto
import com.mypasswordgen.common.dto.idb.SessionIDBDto
import com.mypasswordgen.common.dto.idb.SiteIDBDto
import com.mypasswordgen.common.dto.idb.UserIDBDto
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import java.util.*


fun SiteIDBDto.Companion.arb() = Arb.bind(Arb.uuid(), Arb.string()) { siteId, siteName ->
    SiteIDBDto(siteId.toString(), siteName)
}

fun EmailIDBDto.Companion.arb(range: IntRange) = Arb.bind(Arb.uuid(), Arb.string(), Arb.set(SiteIDBDto.arb(), range)) { emailId, emailAddress, sites ->
    EmailIDBDto(emailId.toString(), emailAddress, sites)
}

fun UserIDBDto.Companion.arb(range: IntRange) = arb(range, range)
fun UserIDBDto.Companion.arb(emailRange: IntRange, siteRange: IntRange) = Arb.bind(Arb.uuid(), Arb.string(), Arb.set(EmailIDBDto.arb(siteRange), emailRange)) { userId, username, emails ->
    UserIDBDto(userId.toString(), username, emails)
}

fun SessionIDBDto.Companion.arb(range: IntRange) = arb(range, range, range)
fun SessionIDBDto.Companion.arb(userRange: IntRange, emailRange: IntRange, siteRange: IntRange) = Arb.set(UserIDBDto.arb(emailRange, siteRange), userRange).map { users ->
    SessionIDBDto(users)
}

fun SiteIDBDto.toFullSiteDto() = FullSiteDto(UUID.fromString(id), siteName)
fun EmailIDBDto.toFullEmailDto() = FullEmailDto(
    UUID.fromString(id),
    emailAddress,
    sites.map { it.toFullSiteDto() }.toSet()
)
fun UserIDBDto.toFullUserDto() = FullUserDto(
    UUID.fromString(id),
    username,
    emails.map { it.toFullEmailDto() }.toSet()
)
fun SessionIDBDto.toFullSessionDto(id: UUID) = FullSessionDto(
    id,
    users.map { it.toFullUserDto() }.toSet()
)
