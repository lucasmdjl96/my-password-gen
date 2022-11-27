/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.common.dto.fullClient

import kotlinx.serialization.Serializable

@Serializable
data class FullEmailClientDto(val id: String, val sites: Set<FullSiteClientDto>) {

    constructor(id: String, builderBlock: Builder.() -> Unit = {}) : this(id, Builder().apply(builderBlock).siteSet)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FullEmailClientDto) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    class Builder {
        private val sites: MutableSet<FullSiteClientDto> = mutableSetOf()
        val siteSet: Set<FullSiteClientDto>
            get() = sites.toSet()
        operator fun FullSiteClientDto.unaryPlus() = sites.add(this)
    }

}
