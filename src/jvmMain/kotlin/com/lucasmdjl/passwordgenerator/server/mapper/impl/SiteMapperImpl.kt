package com.lucasmdjl.passwordgenerator.server.mapper.impl

import com.lucasmdjl.passwordgenerator.common.dto.client.SiteClientDto
import com.lucasmdjl.passwordgenerator.server.mapper.SiteMapper
import com.lucasmdjl.passwordgenerator.server.model.Site
import mu.KotlinLogging

private val logger = KotlinLogging.logger("SiteMapperImpl")

object SiteMapperImpl : SiteMapper {

    override fun siteToSiteClientDto(site: Site): SiteClientDto {
        logger.debug { "siteToSiteDto call with site: $site" }
        return SiteClientDto(site.name)
    }

    override fun siteIterableToSiteDtoClientIterable(siteIterable: Iterable<Site>): Iterable<SiteClientDto> {
        logger.debug { "siteIterableToSiteDtoIterable call with siteList: $siteIterable" }
        return siteIterable.map(SiteMapperImpl::siteToSiteClientDto)
    }

}
