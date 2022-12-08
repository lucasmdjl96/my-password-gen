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

import com.mypasswordgen.common.dto.fullServer.FullEmailServerDto
import com.mypasswordgen.common.dto.fullServer.FullSessionServerDto
import com.mypasswordgen.common.dto.fullServer.FullSiteServerDto
import com.mypasswordgen.common.dto.fullServer.FullUserServerDto
import com.mypasswordgen.server.repository.crypto.encode
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.string


fun FullSiteServerDto.Companion.arb() = Arb.string().map { siteName ->
    FullSiteServerDto(siteName)
}

fun FullEmailServerDto.Companion.arb(range: IntRange) = Arb.bind(Arb.string(), Arb.set(FullSiteServerDto.arb(), range)) { emailAddress, sites ->
    FullEmailServerDto(emailAddress, sites.toMutableSet())
}

fun FullUserServerDto.Companion.arb(range: IntRange) = arb(range, range)
fun FullUserServerDto.Companion.arb(emailRange: IntRange, siteRange: IntRange) = Arb.bind(Arb.string(), Arb.set(FullEmailServerDto.arb(siteRange), emailRange)) { username, emails ->
    FullUserServerDto(username, emails.toMutableSet())
}

fun FullSessionServerDto.Companion.arb(range: IntRange) = arb(range, range, range)
fun FullSessionServerDto.Companion.arb(userRange: IntRange, emailRange: IntRange, siteRange: IntRange) = Arb.set(FullUserServerDto.arb(emailRange, siteRange), userRange).map { users ->
    FullSessionServerDto(users.toMutableSet())
}

fun FullSiteServerDto.encode() = FullSiteServerDto(siteName.encode())
fun FullEmailServerDto.encode() = FullEmailServerDto(emailAddress.encode()) {
    for (site in sites) +site.encode()
}
fun FullUserServerDto.encode() = FullUserServerDto(username.encode()) {
    for (email in emails) +email.encode()
}
fun FullSessionServerDto.encode() = FullSessionServerDto {
    for (user in users) +user.encode()
}
