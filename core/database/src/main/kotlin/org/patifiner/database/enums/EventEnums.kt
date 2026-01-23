package org.patifiner.database.enums

enum class EventType {
    IRL_PUBLIC_PLACE, // ивент где не нужно платить
    IRL_PAID, // например концерт, кино или ещё что-то, участник должен будет сам оплатить свои расходы
    ONLINE, // онлайн игры, конфиренции, игры, онлайн сервисы коммуникации, совместный просмотр кино онлайн и т.д.
}

enum class ParticipantStatus {
    INVITED,  // Приглашен организатором
    JOINED,   // Подтвердил участие
    REJECTED  // Отклонил приглашение
}
