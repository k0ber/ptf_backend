package org.patifiner.geo

import org.koin.dsl.module

val geoModule = module {
    single<GeoDao> { ExposedGeoDao() }
    single { GeoService(get(), get()) }
}
