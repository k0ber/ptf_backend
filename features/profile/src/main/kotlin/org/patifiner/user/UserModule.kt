package org.patifiner.user

import org.koin.dsl.module
import org.patifiner.user.data.ExposedUserDao
import org.patifiner.user.data.UserDao

val userModule = module {

    val userDao = ExposedUserDao()
    single<UserDao> { userDao }
    single<UserService> { UserService(userDao, get()) }

}
