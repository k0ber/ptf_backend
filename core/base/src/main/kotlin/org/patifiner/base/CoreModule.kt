package org.patifiner.base

import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val coreModule = module { single<Logger> { LoggerFactory.getLogger("Ptf") } }
