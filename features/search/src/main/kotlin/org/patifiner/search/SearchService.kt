package org.patifiner.search

import org.patifiner.database.TopicLevel
import org.patifiner.search.api.PaginationRequest
import org.patifiner.topics.UserTopicDto
import org.patifiner.topics.TopicDao
import org.patifiner.user.UserInfoDto
import org.patifiner.user.UserDao
import org.patifiner.user.toDto

internal class SearchService(
    private val userDao: UserDao,
    private val topicDao: TopicDao
) {
    suspend fun findTopicIdea(myUserId: Long): TopicIdeaDto {
        val myTopics = topicDao.getUserTopics(myUserId).toSet()
        val myUserProfile = userDao.getById(myUserId)

        if (myTopics.isEmpty()) {
            return TopicIdeaDto(
                person = UserProfileDto(myUserProfile.toDto(), myTopics),
                topic = topicDao.getTopicsTree().first(),
                getIdeaForEmptyTopics()
            )
        }

        val candidateIds = topicDao
            .findUsersByAnyTopics(
                topicIds = myTopics.map { it.topic.id },
                limit = 50,
                offset = 0,
                excludeUserId = -1L
            )
            .filter { it != myUserId }

        if (candidateIds.isEmpty()) {
            return TopicIdeaDto(
                person = UserProfileDto(myUserProfile.toDto(), myTopics),
                topic = topicDao.getTopicsTree().first(),
                idea = getIdeaForNoCandidates()
            )
        }

        val candidateId = candidateIds.shuffled().first()

        val candidateEntity = userDao.getById(candidateId)
        val candidateTopics = topicDao.getUserTopics(candidateId)

        // ищем общий topicId
        val commonTopicId = myTopics.map { it.topic.id }.shuffled().first()
        val candidateByTopicId = candidateTopics.associateBy { it.topic.id }

        val myTopic = myTopics.first { it.topic.id == commonTopicId }
        val candidateTopic = candidateByTopicId.getValue(commonTopicId)

        val ideaText = getIdeaText(
            myInfo = myUserProfile.toDto(),
            myTopic = myTopic,
            personInfo = candidateEntity.toDto(),
            personTopic = candidateTopic
        )

        return TopicIdeaDto(
            person = UserProfileDto(
                userInfo = candidateEntity.toDto(),
                userTopics = candidateTopics
            ),
            topic = myTopic.topic,
            idea = ideaText
        )
    }

    suspend fun findUsers(
        myId: Long,
        paging: PaginationRequest
    ): Set<UserInfoDto> {
        // Собираем мои topicId
        val myTopics = topicDao.getUserTopics(myId)          // Set<UserTopicDto>
        if (myTopics.isEmpty()) return emptySet()

        check(paging.limit in 1..100) { "Invalid limit param" }
        check(paging.offset >= 0) { "Invalid offset param" }

        val topicIds = myTopics.map { it.topic.id }.toSet()
        return userDao.findUsersByAnyTopics(
            topicIds = topicIds,
            excludeUserId = myId,
            limit = paging.limit,
            offset = paging.offset
        )
    }

// -------------------- helpers --------------------

    fun getIdeaForEmptyTopics() = "Добавьте себе хотя бы один топик !!"
    fun getIdeaForNoCandidates() = "У вас уникальные интересы, ни у кого таких нет, вы прекрасны !!"

    fun getIdeaText(
        myInfo: UserInfoDto,
        myTopic: UserTopicDto,
        personInfo: UserInfoDto,
        personTopic: UserTopicDto
    ): String {
        val commonTopicName = myTopic.topic.name
        myInfo.name
        val personName = personInfo.name
        return when {
            myTopic.level == TopicLevel.NEWBIE && personTopic.level == TopicLevel.NEWBIE -> listOf(
                "расскажите почему вам интересно $commonTopicName !!",
                "спросите что $personName любит в $commonTopicName ??"
            )

            myTopic.level == TopicLevel.NEWBIE && personTopic.level == TopicLevel.INTERMEDIATE -> listOf(
                "расскажите почему вам интересно $commonTopicName !!",
                "спросите что $personName шарит в $commonTopicName ??"
            )

            myTopic.level == TopicLevel.NEWBIE && personTopic.level == TopicLevel.ADVANCED -> listOf(
                "склонитесь перед скромным маэстро $personInfo непревосходным в $commonTopicName !!",
                "попросите его направить вас на путь истинный в $commonTopicName !!"
            )

            myTopic.level == TopicLevel.INTERMEDIATE && personTopic.level == TopicLevel.NEWBIE -> listOf(
                "расскажите $personName о своих успехах в  $commonTopicName !!",
                "спросите насколько $personName далеко продвинулся в $commonTopicName ??"
            )

            myTopic.level == TopicLevel.INTERMEDIATE && personTopic.level == TopicLevel.INTERMEDIATE -> listOf(
                "расскажите $personName о своих успехах в  $commonTopicName и спросите насколько хороши успехи  $personName"
            )

            myTopic.level == TopicLevel.INTERMEDIATE && personTopic.level == TopicLevel.ADVANCED -> listOf(
                "расскажите $personName о своих успехах в  $commonTopicName и спросите как достичь успеха $personName"
            )

            myTopic.level == TopicLevel.ADVANCED && personTopic.level == TopicLevel.NEWBIE -> listOf(
                "скажите что $personName молодец и пожелайте ему успехов в $commonTopicName"
            )

            myTopic.level == TopicLevel.ADVANCED && personTopic.level == TopicLevel.INTERMEDIATE -> listOf(
                "скажите что $personName молодец и вы с ним настоящие братья по $commonTopicName"
            )

            myTopic.level == TopicLevel.ADVANCED && personTopic.level == TopicLevel.ADVANCED -> listOf(
                "ну это просто капец, тут столько идей сразу, даже не знаю с чего начать (сами придумайте раз такие умные)"
            )

            else -> listOf("поставьте куданибудь лайк !!")
        }.random()
    }
}
//            "Начните с обсуждения общих интересов и выберите формат по настроению."
//            "Предложите начать вместе с простого плана на неделю и обмениваться прогрессом."
//            "Попросите лайтовый менторинг: короткий созвон и мини-план на старт."
//            "Попросите «дорожную карту» от профи и разбейте её на первые 2–3 шага."
//            "Предложите мини-челлендж на 3–5 дней и дружеский фидбек."
//            "Сделайте спарринг: выберите тему и сравните подходы."
//            "Попросите ревью вашей практики и список «что прокачать дальше»."
//            "Дайте новичку быстрый онбординг: 3 ключевых шага и полезные ссылки."
//            "Обсудите продвинутые кейсы и обменяйтесь
//