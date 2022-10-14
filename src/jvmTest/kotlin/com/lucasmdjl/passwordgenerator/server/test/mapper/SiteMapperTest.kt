package com.lucasmdjl.passwordgenerator.server.test.mapper

import com.lucasmdjl.passwordgenerator.common.dto.client.SiteClientDto
import com.lucasmdjl.passwordgenerator.server.mapper.impl.SiteMapperImpl
import com.lucasmdjl.passwordgenerator.server.model.Site
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
    private lateinit var dummySiteName: String
    private lateinit var dummySiteClientDto: SiteClientDto

    @BeforeAll
    override fun initMocks() {
        siteMock = mockk()
    }

    @BeforeEach
    override fun initDummies() {
        dummySiteName = "site123"
        dummySiteClientDto = SiteClientDto(dummySiteName)
    }

    @Nested
    inner class SiteToSiteClientDto {

        @Test
        fun `with no emails`() {
            every { siteMock.name } returns dummySiteName
            val siteMapper = SiteMapperImpl()
            val siteDto = siteMapper.siteToSiteClientDto(siteMock)
            assertEquals(dummySiteName, siteDto.siteName)
        }

        @Test
        fun `with receiver`() {
            val siteMapper = SiteMapperImpl()
            val siteMapperSpy = spyk(siteMapper)
            every { siteMapperSpy.siteToSiteClientDto(siteMock) } returns dummySiteClientDto
            with (siteMapperSpy) {
                siteMock.toSiteClientDto()
            }
            verifySequence {
                with (siteMapperSpy) {
                    siteMock.toSiteClientDto()            }
                siteMapperSpy.siteToSiteClientDto(siteMock)
            }
        }

    }

}
