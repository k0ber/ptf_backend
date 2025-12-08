package org.patifiner.check.di

import org.patifiner.check.data.CheckData
import org.koin.dsl.bind
import org.koin.dsl.module
import org.patifiner.check.data.CheckDataImpl

val checkModule = module {
    single { CheckDataImpl() } bind CheckData::class
}
