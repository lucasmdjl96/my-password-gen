package com.lucasmdjl.application.mapper

import com.lucasmdjl.application.model.Site
import dto.SiteDto

interface SiteMapper {

    fun siteToSiteDto(site: Site): SiteDto

    fun siteIterableToSiteDtoIterable(siteList: Iterable<Site>?): Iterable<SiteDto>?

}