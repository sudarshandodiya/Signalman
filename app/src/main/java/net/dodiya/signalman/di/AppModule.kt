package net.dodiya.signalman.di

import net.dodiya.signalman.data.AppDatabase
import net.dodiya.signalman.data.PreferenceManager
import net.dodiya.signalman.data.RuleRepository
import net.dodiya.signalman.data.RuleRepositoryImpl
import net.dodiya.signalman.domain.CreateAutoRuleUseCase
import net.dodiya.signalman.domain.MatchRuleUseCase
import net.dodiya.signalman.domain.TransformUrlUseCase
import net.dodiya.signalman.ui.EditRuleViewModel
import net.dodiya.signalman.ui.RoutingViewModel
import net.dodiya.signalman.ui.RuleViewModel
import net.dodiya.signalman.ui.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dataModule =
    module {
        single { AppDatabase.getDatabase(androidContext()) }
        single { get<AppDatabase>().ruleDao() }
        singleOf(::PreferenceManager)
        singleOf(::RuleRepositoryImpl) { bind<RuleRepository>() }
    }

val useCasesModule =
    module {
        singleOf(::MatchRuleUseCase)
        singleOf(::TransformUrlUseCase)
        singleOf(::CreateAutoRuleUseCase)
    }

val viewModelModule =
    module {
        viewModelOf(::RuleViewModel)
        viewModelOf(::EditRuleViewModel)
        viewModelOf(::RoutingViewModel)
        viewModelOf(::SettingsViewModel)
    }

val appModule =
    module {
        includes(dataModule, useCasesModule, viewModelModule)
    }
