package org.patifiner.postgres

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.DecimalColumnType
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.append
import org.jetbrains.exposed.sql.stringLiteral
import java.math.BigDecimal

// 1. Определите пользовательский бинарный оператор для Exposed (оператор %)
class PgTrgmSearchOp(
    private val expr1: Expression<*>,
    private val expr2: Expression<*>
) : Op<Boolean>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder): Unit = queryBuilder {
        // Формат: expr1 % expr2
        append(expr1, " % ", expr2)
    }
}

// 2. Определите функцию расширения для Column<String> (для оператора %)
infix fun Column<String>.trgmSearch(value: String): Op<Boolean> = PgTrgmSearchOp(this, stringLiteral(value))

class SimilarityFunction(
    private val column: Column<String>,
    private val query: String
) : Function<BigDecimal>(DecimalColumnType(10, 5)) { // Используем Decimal/Double для возвращаемого значения (0.0 до 1.0)
    override fun toQueryBuilder(queryBuilder: QueryBuilder): Unit = queryBuilder {
        append("similarity(", column, ", ", stringLiteral(query), ")")
    }
}

fun Column<String>.similarity(query: String): Expression<BigDecimal> = SimilarityFunction(this, query)
