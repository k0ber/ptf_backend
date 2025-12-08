package org.patifiner.upload

import org.koin.dsl.module
import org.patifiner.upload.api.UploadConfig


fun uploadModule(uploadConfig: UploadConfig) = module {

    single {
        UploadService(uploadConfig)
    }

}
