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
data class FullEmailServerDto(val emailAddress: String, val sites: MutableSet<FullSiteServerDto>) {
    constructor(emailAddress: String, builderBlock: Builder.() -> Unit = {}) : this(emailAddress, Builder().apply(builderBlock).sites)
    fun addSite(site: FullSiteServerDto) = sites.add(site)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FullEmailServerDto) return false

        if (emailAddress != other.emailAddress) return false

        return true
    }

    override fun hashCode(): Int {
        return emailAddress.hashCode()
    }

    class Builder {
        val sites: MutableSet<FullSiteServerDto> = mutableSetOf()
        val siteSet: Set<FullSiteServerDto>
            get() = sites.toSet()
        operator fun FullSiteServerDto.unaryPlus() = sites.add(this)
    }
}
