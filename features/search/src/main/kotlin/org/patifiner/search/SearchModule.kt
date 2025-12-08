package org.patifiner.search

import org.koin.dsl.module


val searchModule = module {

    single { SearchService(get(), get()) }

}
