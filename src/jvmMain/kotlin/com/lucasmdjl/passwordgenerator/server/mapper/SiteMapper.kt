package com.lucasmdjl.passwordgenerator.server.mapper

import com.lucasmdjl.passwordgenerator.common.dto.SiteDto
import com.lucasmdjl.passwordgenerator.server.model.Site

interface SiteMapper {

    fun siteToSiteDto(site: Site): SiteDto

    fun siteIterableToSiteDtoIterable(siteList: Iterable<Site>?): Iterable<SiteDto>?

}
