/*
 * Copyright 2019 Guiabolso
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.guiabolso.fixedlengthfilehandler.internal

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.KClass

@PublishedApi
internal inline fun <reified T : Any> defaultTypeParser(parse: String): T = parse.parseToType(T::class)

@Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
@PublishedApi
internal fun <T : Any> String.parseToType(type: KClass<T>): T {
    return when (type) {
        String::class        -> parseToString()
        Int::class           -> parseToInt()
        Double::class        -> parseToDouble()
        Long::class          -> parseToLong()
        Char::class          -> parseToChar()
        Boolean::class       -> parseToBoolean()
        LocalDate::class     -> parseToLocalDate()
        LocalDateTime::class -> parseToLocalDateTime()
        BigDecimal::class    -> parseToBigDecimal()
        else                 -> throw NoParserForClass(type)
    } as T
}

public class NoParserForClass(
    klass: KClass<*>
) : RuntimeException("There are no default parsers for class $klass. Please provide a custom parser")

private fun String.parseToString() = this

private fun String.parseToInt() = this.toInt()

private fun String.parseToDouble() = this.toDouble()

private fun String.parseToLong() = this.toLong()

private fun String.parseToChar() = this.single()

private fun String.parseToBoolean() = this.toBoolean()

private fun String.parseToLocalDate() = LocalDate.parse(this)

private fun String.parseToLocalDateTime() = LocalDateTime.parse(this)

private fun String.parseToBigDecimal() = this.toBigDecimal()
