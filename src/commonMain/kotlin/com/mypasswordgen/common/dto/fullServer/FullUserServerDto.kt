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
data class FullUserServerDto(val username: String, val emails: MutableList<FullEmailServerDto>) {
    constructor(username: String, builderBlock: Builder.() -> Unit = {}) : this(username, Builder().apply(builderBlock).emails)
    fun addEmail(email: FullEmailServerDto) = emails.add(email)
    class Builder {
        val emails: MutableList<FullEmailServerDto> = mutableListOf()
        val emailList: List<FullEmailServerDto>
            get() = emails.toList()
        operator fun FullEmailServerDto.unaryPlus() = emails.add(this)
    }
}
