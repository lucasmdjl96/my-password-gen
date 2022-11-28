/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.common.dto.idb

import kotlinx.serialization.Serializable

@Serializable
data class UserIDBDto(val emails: Set<EmailIDBDto>) {
    constructor(builderBlock: Builder.() -> Unit = {}) : this(Builder().apply(builderBlock).emailSet)
    class Builder {
        private val emails: MutableSet<EmailIDBDto> = mutableSetOf()
        val emailSet: Set<EmailIDBDto>
            get() = emails.toSet()
        operator fun EmailIDBDto.unaryPlus() = emails.add(this)
    }
}
