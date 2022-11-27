/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.jsclient.dto

import kotlinx.serialization.Serializable

@Serializable
class EmailClient(val emailAddress: String, val siteSet: MutableSet<String>) {

    constructor(emailAddress: String) : this(emailAddress, mutableSetOf())

    fun hasSite(siteName: String): Boolean = siteSet.find { it == siteName } != null

    fun addSite(siteName: String) = siteSet.add(siteName)

    fun removeSite(siteName: String) = siteSet.remove(siteName)

    override fun toString(): String {
        return "[EmailDto: $emailAddress]"
    }

}
