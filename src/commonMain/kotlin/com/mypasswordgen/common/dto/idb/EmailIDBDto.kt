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
data class EmailIDBDto(val id: String, val emailAddress: String, val sites: List<SiteIDBDto>) {
    constructor(id: String, emailAddress: String, builderBlock: Builder.() -> Unit = {}) : this(id, emailAddress, Builder().apply(builderBlock).siteList)
    class Builder {
        private val sites: MutableList<SiteIDBDto> = mutableListOf()
        val siteList: List<SiteIDBDto>
            get() = sites.toList()
        operator fun SiteIDBDto.unaryPlus() = sites.add(this)
    }
}
