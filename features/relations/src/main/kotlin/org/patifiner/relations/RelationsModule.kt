package org.patifiner.relations

import org.koin.dsl.module

val relationsModule = module {
    single<RelationsDao> { ExposedRelationsDao() }
    single { RelationsService(relationsDao = get()) }
}
