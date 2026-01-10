package org.patifiner.search.api

import io.ktor.http.HttpStatusCode
import org.patifiner.base.PatifinerException

sealed class SearchException(message: String, code: String) : PatifinerException(message, code, HttpStatusCode.NotFound) {

//    class NoUserTopicsException(userId: Long) : SearchException("User $userId has no topics")
//
//    class NoCandidatesFoundException(userId: Long) : SearchException("No candidates found for user $userId")

    class InconsistentUserException(userId: Long) :
        SearchException("Candidate user $userId not found", "SEARCH_USER_INCONSISTENT")

    class NoCommonTopicException(myId: Long, otherId: Long) :
        SearchException("No common topic between user $myId and user $otherId", "SEARCH_NO_COMMON_TOPICS")

}
