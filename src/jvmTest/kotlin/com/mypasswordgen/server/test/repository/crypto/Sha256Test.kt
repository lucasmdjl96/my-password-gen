/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.test.repository.crypto

import com.mypasswordgen.server.repository.crypto.encode
import com.mypasswordgen.server.test.TestParent
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class Sha256Test : TestParent() {
    override fun initMocks() {}

    override fun initDummies() {}

    @Test
    fun `same strings`() {
        val string = "string1234".encode()
        val stringSame = "string1234".encode()

        assertEquals(string, stringSame)
    }

    @Test
    fun `different strings`() {
        val string = "string1234".encode()
        val stringOther = "string1432".encode()

        assertNotEquals(string, stringOther)
    }

}
