package org.patifiner.topics

import org.koin.dsl.module

val topicsModule = module {

    val topicDao = ExposedTopicDao()
    single<TopicDao> { topicDao }
    single<TopicsService> { TopicsService(topicDao) }

}
