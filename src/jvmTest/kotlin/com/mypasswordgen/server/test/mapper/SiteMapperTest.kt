/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.test.mapper

import com.mypasswordgen.common.dto.client.SiteClientDto
import com.mypasswordgen.common.dto.fullClient.FullSiteClientDto
import com.mypasswordgen.server.mapper.impl.SiteMapperImpl
import com.mypasswordgen.server.model.Site
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verifySequence
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SiteMapperTest : MapperTestParent() {

    private lateinit var siteMock: Site
    private lateinit var dummySiteId: String
    private lateinit var dummySiteClientDto: SiteClientDto
    private lateinit var dummyFullSiteClientDto: FullSiteClientDto

    @BeforeAll
    override fun initMocks() {
        siteMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummySiteId = "site123"
        dummySiteClientDto = SiteClientDto(dummySiteId)
        dummyFullSiteClientDto = FullSiteClientDto(dummySiteId)
    }

    @Nested
    inner class SiteToSiteClientDto {

        @Test
        fun `with argument`() {
            every { siteMock.id.value.toString() } returns dummySiteId
            val siteMapper = SiteMapperImpl()
            val siteDto = siteMapper.siteToSiteClientDto(siteMock)
            assertEquals(dummySiteId, siteDto.id)
        }

        @Test
        fun `with receiver`() {
            val siteMapper = SiteMapperImpl()
            val siteMapperSpy = spyk(siteMapper)
            every { siteMapperSpy.siteToSiteClientDto(siteMock) } returns dummySiteClientDto
            val result = with(siteMapperSpy) {
                siteMock.toSiteClientDto()
            }
            assertEquals(dummySiteClientDto, result)
            verifySequence {
                with(siteMapperSpy) {
                    siteMock.toSiteClientDto()
                }
                siteMapperSpy.siteToSiteClientDto(siteMock)
            }
        }

    }

    @Nested
    inner class SiteToFullSiteClientDto {

        @Test
        fun `with argument`() {
            mockTransaction()
            every { siteMock.id.value.toString() } returns dummySiteId
            val siteMapper = SiteMapperImpl()
            val fullSiteClientDto = siteMapper.siteToFullSiteClientDto(siteMock)
            assertEquals(dummySiteId, fullSiteClientDto.id)
        }

        @Test
        fun `with receiver`() {
            val siteMapper = SiteMapperImpl()
            val siteMapperSpy = spyk(siteMapper)
            every { siteMapperSpy.siteToFullSiteClientDto(siteMock) } returns dummyFullSiteClientDto
            val result = with(siteMapperSpy) {
                siteMock.toFullSiteClientDto()
            }
            assertEquals(dummyFullSiteClientDto, result)
            verifySequence {
                with(siteMapperSpy) {
                    siteMock.toFullSiteClientDto()
                }
                siteMapperSpy.siteToFullSiteClientDto(siteMock)
            }
        }

    }

}
