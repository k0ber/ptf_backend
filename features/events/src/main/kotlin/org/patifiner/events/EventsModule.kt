package org.patifiner.events

import org.koin.dsl.module

val eventsModule = module {
    single { EventsService(logger = get(), eventsDao = ExposedEventsDao()) }
}
