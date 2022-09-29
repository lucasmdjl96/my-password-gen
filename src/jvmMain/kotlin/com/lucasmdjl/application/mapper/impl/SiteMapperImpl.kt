package com.lucasmdjl.application.mapper.impl

import com.lucasmdjl.application.mapper.SiteMapper
import com.lucasmdjl.application.model.Site
import dto.SiteDto
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