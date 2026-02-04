package org.patifiner.events

import org.koin.dsl.module

val eventsModule = module {
    single<EventsDao> { ExposedEventsDao() }
    single { EventsService(logger = get(), eventsDao = get()) }
}
