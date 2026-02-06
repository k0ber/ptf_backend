package org.patifiner.relations.api

import io.ktor.http.HttpStatusCode
import org.patifiner.base.PtfException

sealed class RelationsException(message: String, code: String, statusCode: HttpStatusCode) : PtfException(message, code, statusCode) {
    class RelationNotFoundException : RelationsException("Relation not found", "RELATION_NOT_FOUND", HttpStatusCode.NotFound)
    class AlreadyExistsException : RelationsException("Relation already exists", "RELATION_ALREADY_EXISTS", HttpStatusCode.Conflict)
    class ActionNotAllowedException : RelationsException("Action not allowed for current status", "RELATION_ACTION_NOT_ALLOWED", HttpStatusCode.Forbidden)
}
