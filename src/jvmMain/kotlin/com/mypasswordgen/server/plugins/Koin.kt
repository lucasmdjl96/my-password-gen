package com.mypasswordgen.server.plugins

import com.mypasswordgen.server.controller.*
import com.mypasswordgen.server.controller.impl.*
import com.mypasswordgen.server.mapper.EmailMapper
import com.mypasswordgen.server.mapper.SessionMapper
import com.mypasswordgen.server.mapper.SiteMapper
import com.mypasswordgen.server.mapper.UserMapper
import com.mypasswordgen.server.mapper.impl.EmailMapperImpl
import com.mypasswordgen.server.mapper.impl.SessionMapperImpl
import com.mypasswordgen.server.mapper.impl.SiteMapperImpl
import com.mypasswordgen.server.mapper.impl.UserMapperImpl
import com.mypasswordgen.server.repository.EmailRepository
import com.mypasswordgen.server.repository.SessionRepository
import com.mypasswordgen.server.repository.SiteRepository
import com.mypasswordgen.server.repository.UserRepository
import com.mypasswordgen.server.repository.impl.EmailRepositoryImpl
import com.mypasswordgen.server.repository.impl.SessionRepositoryImpl
import com.mypasswordgen.server.repository.impl.SiteRepositoryImpl
import com.mypasswordgen.server.repository.impl.UserRepositoryImpl
import com.mypasswordgen.server.service.EmailService
import com.mypasswordgen.server.service.SessionService
import com.mypasswordgen.server.service.SiteService
import com.mypasswordgen.server.service.UserService
import com.mypasswordgen.server.service.impl.EmailServiceImpl
import com.mypasswordgen.server.service.impl.SessionServiceImpl
import com.mypasswordgen.server.service.impl.SiteServiceImpl
import com.mypasswordgen.server.service.impl.UserServiceImpl
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
