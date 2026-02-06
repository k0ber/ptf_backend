package org.patifiner.search

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.patifiner.base.PagedRequest
import org.patifiner.database.enums.TopicLevel
import org.patifiner.relations.RelationsDao
import org.patifiner.topics.TopicDao
import org.patifiner.topics.UserTopicDto
import org.patifiner.user.UserDao
import org.patifiner.user.UserDto
import org.patifiner.user.toDto

internal class SearchService(
    private val userDao: UserDao,
    private val topicDao: TopicDao,
    private val relationsDao: RelationsDao
) {
    suspend fun findTopicIdea(myUserId: Long): TopicIdeaDto? = newSuspendedTransaction(Dispatchers.IO) {
        val myTopics = topicDao.getUserTopics(myUserId)
        val myUserProfile = userDao.getById(myUserId)

        if (myTopics.isEmpty()) {
            return@newSuspendedTransaction TopicIdeaDto(
                person = UserProfileDto(myUserProfile.toDto(), myTopics),
                topic = topicDao.getTopicsTree().first(),
                idea = getIdeaForEmptyTopics()
            )
        }

        // 1. Пытаемся найти ОДНОГО случайного человека с общим топиком
        val myTopicIds = myTopics.map { it.topic.id }
        val candidateId = topicDao.getRandomUserIdByAnyTopics(
            topicIds = myTopicIds,
            excludeUserIds = relationsDao.getMutedUserIds(myUserId) + myUserId
        )

        // 2. Если никого нет — возвращаем null.
        // Фронт покажет "Идей пока нет, мы маякнем тебе нотификацией"
        if (candidateId == null) return@newSuspendedTransaction null

        val candidateEntity = userDao.getById(candidateId)
        val candidateTopics = topicDao.getUserTopics(candidateId)

        // 3. Ищем общие топики
        val candidateTopicIds = candidateTopics.map { it.topic.id }.toSet()
        val commonTopicIds = myTopicIds.intersect(candidateTopicIds)

        // Выбираем случайный из общих
        val chosenTopicId = commonTopicIds.random()

        val myTopic = myTopics.first { it.topic.id == chosenTopicId }
        val candidateTopic = candidateTopics.first { it.topic.id == chosenTopicId }

        TopicIdeaDto(
            person = UserProfileDto(candidateEntity.toDto(), candidateTopics),
            topic = myTopic.topic,
            idea = getIdeaText(myUserProfile.toDto(), myTopic, candidateEntity.toDto(), candidateTopic)
        )
    }

    suspend fun findUsers(myId: Long, req: PagedRequest): List<UserDto> = newSuspendedTransaction(Dispatchers.IO) {
        val myTopics = topicDao.getUserTopics(myId)
        if (myTopics.isEmpty()) return@newSuspendedTransaction emptyList()

        val excludeUserIds = relationsDao.getMutedUserIds(myId) + myId
        val topicIds = myTopics.map { it.topic.id }

        val userIds = topicDao.findUserIdsByAnyTopics(
            topicIds = topicIds,
            excludeUserIds = excludeUserIds,
            pagedRequest = req
        )

        userDao.getUsersByIds(userIds)
    }

    //region helpers
    private fun getIdeaForEmptyTopics() = "Добавьте себе хотя бы один топик !!"

    private fun getIdeaText(
        myInfo: UserDto,
        myTopic: UserTopicDto,
        personInfo: UserDto,
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
    //endregion
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