package com.lucasmdjl.application.mapper.impl

import com.lucasmdjl.application.mapper.SiteMapper
import com.lucasmdjl.application.model.Site
import dto.SiteDto

object SiteMapperImpl : SiteMapper {

    override fun siteToSiteDto(site: Site): SiteDto {
        return SiteDto(site.name)
    }

    override fun siteListToSiteDtoList(siteList: Iterable<Site>?): Iterable<SiteDto>? {
        return siteList?.map(SiteMapperImpl::siteToSiteDto)
    }

}