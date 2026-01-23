package org.patifiner.search

import org.koin.dsl.module


val searchModule = module {

    single { SearchService(userDao = get(), topicDao = get(), relationsDao = get()) }

}
