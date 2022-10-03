package com.lucasmdjl.passwordgenerator.server.mapper.impl

import com.lucasmdjl.passwordgenerator.common.dto.SiteDto
import com.lucasmdjl.passwordgenerator.server.mapper.SiteMapper
import com.lucasmdjl.passwordgenerator.server.model.Site
import mu.KotlinLogging

private val logger = KotlinLogging.logger("SiteMapperImpl")

object SiteMapperImpl : SiteMapper {

    override fun siteToSiteDto(site: Site): SiteDto {
        logger.debug { "siteToSiteDto call with site: $site" }
        return SiteDto(site.name)
    }

    override fun siteIterableToSiteDtoIterable(siteList: Iterable<Site>?): Iterable<SiteDto>? {
        logger.debug { "siteIterableToSiteDtoIterable call with siteList: $siteList" }
        return siteList?.map(SiteMapperImpl::siteToSiteDto)
    }

}
