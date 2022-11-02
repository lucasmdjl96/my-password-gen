package com.lucasmdjl.passwordgenerator.server.mapper.impl

import com.lucasmdjl.passwordgenerator.common.dto.client.SiteClientDto
import com.lucasmdjl.passwordgenerator.server.mapper.SiteMapper
import com.lucasmdjl.passwordgenerator.server.model.Site
import mu.KotlinLogging

private val logger = KotlinLogging.logger("SiteMapperImpl")

class SiteMapperImpl : SiteMapper {

    override fun siteToSiteClientDto(site: Site): SiteClientDto {
        logger.debug { "siteToSiteClientDto" }
        return SiteClientDto(site.id.value.toString())
    }

}
