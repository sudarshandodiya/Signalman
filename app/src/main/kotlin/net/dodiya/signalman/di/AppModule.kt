package net.dodiya.signalman.di

import net.dodiya.signalman.data.AppDatabase
import net.dodiya.signalman.data.AppInfoRepository
import net.dodiya.signalman.data.PreferenceManager
import net.dodiya.signalman.data.RuleRepository
import net.dodiya.signalman.data.RuleRepositoryImpl
import net.dodiya.signalman.domain.CreateAutoRuleUseCase
import net.dodiya.signalman.domain.MatchRuleUseCase
import net.dodiya.signalman.domain.TransformUrlUseCase
import net.dodiya.signalman.ui.RoutingViewModel
import net.dodiya.signalman.ui.RuleViewModel
import net.dodiya.signalman.ui.SettingsViewModel
import net.dodiya.signalman.ui.editrule.EditRuleViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Data layer module - provides repositories and data sources
 */
val dataModule =
    module {
        // Database
        single { AppDatabase.getDatabase(androidContext()) }
        single { get<AppDatabase>().ruleDao() }

        // DataStore preferences
        singleOf(::PreferenceManager)

        // App info repository with dependencies
        single {
            AppInfoRepository(
                packageManager = androidContext().packageManager,
                myPackageName = androidContext().packageName,
            )
        }

        // Repository with interface binding
        singleOf(::RuleRepositoryImpl) { bind<RuleRepository>() }
    }

/**
 * Domain layer module - provides use cases
 * Use cases are stateless, so we use factory (new instance each time)
 */
val domainModule =
    module {
        factoryOf(::MatchRuleUseCase)
        factoryOf(::TransformUrlUseCase)
        factoryOf(::CreateAutoRuleUseCase)
    }

/**
 * ViewModel module - provides ViewModels
 * ViewModels are scoped to the lifecycle of the screen
 */
val viewModelModule =
    module {
        viewModelOf(::RuleViewModel)
        viewModelOf(::EditRuleViewModel)
        viewModelOf(::RoutingViewModel)
        viewModelOf(::SettingsViewModel)
    }

/**
 * Main application module that includes all modules
 */
val appModule =
    module {
        includes(dataModule, domainModule, viewModelModule)
    }
