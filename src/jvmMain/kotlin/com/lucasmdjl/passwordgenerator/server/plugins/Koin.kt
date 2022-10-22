package com.lucasmdjl.passwordgenerator.server.plugins

import com.lucasmdjl.passwordgenerator.server.controller.*
import com.lucasmdjl.passwordgenerator.server.controller.impl.*
import com.lucasmdjl.passwordgenerator.server.mapper.EmailMapper
import com.lucasmdjl.passwordgenerator.server.mapper.SessionMapper
import com.lucasmdjl.passwordgenerator.server.mapper.SiteMapper
import com.lucasmdjl.passwordgenerator.server.mapper.UserMapper
import com.lucasmdjl.passwordgenerator.server.mapper.impl.EmailMapperImpl
import com.lucasmdjl.passwordgenerator.server.mapper.impl.SessionMapperImpl
import com.lucasmdjl.passwordgenerator.server.mapper.impl.SiteMapperImpl
import com.lucasmdjl.passwordgenerator.server.mapper.impl.UserMapperImpl
import com.lucasmdjl.passwordgenerator.server.repository.EmailRepository
import com.lucasmdjl.passwordgenerator.server.repository.SessionRepository
import com.lucasmdjl.passwordgenerator.server.repository.SiteRepository
import com.lucasmdjl.passwordgenerator.server.repository.UserRepository
import com.lucasmdjl.passwordgenerator.server.repository.impl.EmailRepositoryImpl
import com.lucasmdjl.passwordgenerator.server.repository.impl.SessionRepositoryImpl
import com.lucasmdjl.passwordgenerator.server.repository.impl.SiteRepositoryImpl
import com.lucasmdjl.passwordgenerator.server.repository.impl.UserRepositoryImpl
import com.lucasmdjl.passwordgenerator.server.service.EmailService
import com.lucasmdjl.passwordgenerator.server.service.SessionService
import com.lucasmdjl.passwordgenerator.server.service.SiteService
import com.lucasmdjl.passwordgenerator.server.service.UserService
import com.lucasmdjl.passwordgenerator.server.service.impl.EmailServiceImpl
import com.lucasmdjl.passwordgenerator.server.service.impl.SessionServiceImpl
import com.lucasmdjl.passwordgenerator.server.service.impl.SiteServiceImpl
import com.lucasmdjl.passwordgenerator.server.service.impl.UserServiceImpl
import io.ktor.server.application.*
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.installKoin() {
    install(Koin) {
        modules(module {
            singleOf(::SessionRepositoryImpl) { bind<SessionRepository>() }
            singleOf(::UserRepositoryImpl) { bind<UserRepository>() }
            singleOf(::EmailRepositoryImpl) { bind<EmailRepository>() }
            singleOf(::SiteRepositoryImpl) { bind<SiteRepository>() }

            singleOf(::SessionServiceImpl) { bind<SessionService>() }
            singleOf(::UserServiceImpl) { bind<UserService>() }
            singleOf(::EmailServiceImpl) { bind<EmailService>() }
            singleOf(::SiteServiceImpl) { bind<SiteService>() }

            singleOf(::SessionControllerImpl) { bind<SessionController>() }
            singleOf(::UserControllerImpl) { bind<UserController>() }
            singleOf(::EmailControllerImpl) { bind<EmailController>() }
            singleOf(::SiteControllerImpl) { bind<SiteController>() }
            singleOf(::MainControllerImpl) { bind<MainController>() }
            singleOf(::CookieControllerImpl) { bind<CookieController>() }
            singleOf(::AboutControllerImpl) { bind<AboutController>() }

            singleOf(::SessionMapperImpl) { bind<SessionMapper>() }
            singleOf(::UserMapperImpl) { bind<UserMapper>() }
            singleOf(::EmailMapperImpl) { bind<EmailMapper>() }
            singleOf(::SiteMapperImpl) { bind<SiteMapper>() }
        })
    }
}
