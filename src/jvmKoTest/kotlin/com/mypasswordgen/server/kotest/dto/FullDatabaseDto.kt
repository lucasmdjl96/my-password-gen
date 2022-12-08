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

import com.mypasswordgen.server.model.Session
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import org.jetbrains.exposed.sql.Op

data class FullDatabaseDto(val sessionSet: Set<FullSessionDto>) {
    fun insertStatement() = sessionSet.joinToString("") { it.insertStatement() }

    fun encode() = FullDatabaseDto(sessionSet.map { it.encode() }.toSet())

    fun recoverPartialDatabase() = FullDatabaseDto(buildSet {
        sessionSet.forEach {
            val fullSessionDto = FullSessionDto.recoverFromDatabase(it.id)
            fullSessionDto.shouldNotBeNull()
            add(fullSessionDto)
        }
    })

    companion object {
        fun recoverFromDatabase(): FullDatabaseDto {
            val sessions = Session.find(Op.TRUE).map { FullSessionDto.recoverFromDatabase(it.id.value)!! }
            return FullDatabaseDto(sessions.toSet())
        }

        fun arb(range: IntRange) = arb(range, range)
        fun arb(sessionRange: IntRange, ranges: IntRange) = arb(sessionRange, ranges, ranges, ranges)
        fun arb(sessionRange: IntRange, userRange: IntRange, emailRange: IntRange, siteRange: IntRange) =
            Arb.set(FullSessionDto.arb(userRange, emailRange, siteRange), sessionRange)
                .map { sessionSet -> FullDatabaseDto(sessionSet) }
    }
}
