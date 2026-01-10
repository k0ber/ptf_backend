package org.patifiner.user

import org.koin.dsl.module

val userModule = module {

    val userDao = ExposedUserDao()
    single<UserDao> { userDao }
    single<UserService> { UserService(userDao, get()) }

}
