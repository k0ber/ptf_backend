package org.patifiner.upload

import org.koin.dsl.module

val uploadModule = module {

    single { UploadService(get()) }

}
