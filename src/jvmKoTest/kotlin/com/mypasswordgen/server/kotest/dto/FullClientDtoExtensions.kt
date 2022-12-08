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

import com.mypasswordgen.common.dto.fullClient.FullEmailClientDto
import com.mypasswordgen.common.dto.fullClient.FullSessionClientDto
import com.mypasswordgen.common.dto.fullClient.FullSiteClientDto
import com.mypasswordgen.common.dto.fullClient.FullUserClientDto
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.uuid


fun FullSiteClientDto.Companion.arb() = Arb.uuid().map { siteId -> FullSiteClientDto(siteId.toString()) }

fun FullEmailClientDto.Companion.arb(range: IntRange) =
    Arb.bind(Arb.uuid(), Arb.set(FullSiteClientDto.arb(), range)) { emailId, siteList ->
        FullEmailClientDto(emailId.toString(), siteList)
    }

fun FullUserClientDto.Companion.arb(range: IntRange) = arb(range, range)
fun FullUserClientDto.Companion.arb(emailRange: IntRange, siteRange: IntRange) =
    Arb.bind(Arb.uuid(), Arb.set(FullEmailClientDto.arb(siteRange), emailRange)) { userId, emailList ->
        FullUserClientDto(userId.toString(), emailList)
    }

fun FullSessionClientDto.Companion.arb(range: IntRange) = arb(range, range)
fun FullSessionClientDto.Companion.arb(userRange: IntRange, ranges: IntRange) =
    arb(userRange, ranges, ranges)

fun FullSessionClientDto.Companion.arb(userRange: IntRange, emailRange: IntRange, siteRange: IntRange) =
    Arb.set(FullUserClientDto.arb(emailRange, siteRange), userRange).map { userList ->
        FullSessionClientDto(userList)
    }
