package com.mypasswordgen.server.mapper.impl

import com.mypasswordgen.common.dto.FullSiteClientDto
import com.mypasswordgen.common.dto.client.SiteClientDto
import com.mypasswordgen.server.mapper.SiteMapper
import com.mypasswordgen.server.model.Site
import mu.KotlinLogging

private val logger = KotlinLogging.logger("SiteMapperImpl")

class SiteMapperImpl : SiteMapper {

    override fun siteToSiteClientDto(site: Site): SiteClientDto {
        logger.debug { "siteToSiteClientDto" }
        return SiteClientDto(site.id.value.toString())
    }

    override fun siteToFullSiteClientDto(site: Site): FullSiteClientDto {
        return FullSiteClientDto(site.id.value.toString())
    }

}
