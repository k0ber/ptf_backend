package org.patifiner.search.api


sealed class SearchException(message: String) : RuntimeException(message) {

//    class NoUserTopicsException(userId: Long) : SearchException("User $userId has no topics")
//
//    class NoCandidatesFoundException(userId: Long) : SearchException("No candidates found for user $userId")

    class InconsistentUserException(userId: Long) :
        SearchException("Candidate user $userId not found")

    class NoCommonTopicException(myId: Long, otherId: Long) :
        SearchException("No common topic between user $myId and user $otherId")

}

