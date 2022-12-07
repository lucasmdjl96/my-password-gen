/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.common.dto.fullServer

import kotlinx.serialization.Serializable

@Serializable
data class FullUserServerDto(val username: String, val emails: MutableSet<FullEmailServerDto>) {
    constructor(username: String, builderBlock: Builder.() -> Unit = {}) : this(username, Builder().apply(builderBlock).buildEmails())
    fun addEmail(email: FullEmailServerDto) = emails.add(email)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FullUserServerDto) return false

        if (username != other.username) return false

        return true
    }

    override fun hashCode(): Int {
        return username.hashCode()
    }

    class Builder {
        private val emails: MutableSet<FullEmailServerDto> = mutableSetOf()
        operator fun FullEmailServerDto.unaryPlus() = emails.add(this)
        fun buildEmails() = emails
    }
}
