package org.patifiner.topics

import org.koin.dsl.module
import org.patifiner.topics.data.ExposedTopicDao
import org.patifiner.topics.data.TopicDao

val topicsModule = module {

    val topicDao = ExposedTopicDao()
    single<TopicDao> { topicDao }
    single<TopicsService> { TopicsService(topicDao) }

}
