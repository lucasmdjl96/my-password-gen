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

import com.mypasswordgen.common.dto.server.EmailServerDto
import com.mypasswordgen.common.dto.server.SiteServerDto
import com.mypasswordgen.common.dto.server.UserServerDto
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string


fun SiteServerDto.Companion.arb() = Arb.string().map { siteName -> SiteServerDto(siteName) }
fun EmailServerDto.Companion.arb() = Arb.string().map { emailAddress -> EmailServerDto(emailAddress) }
fun UserServerDto.Companion.arb()  = Arb.string().map { username -> UserServerDto(username) }
