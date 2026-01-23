package org.patifiner.relations

import org.koin.dsl.module

val relationsModule = module {
    single { RelationsService(relationsDao = ExposedRelationsDao()) }
}
