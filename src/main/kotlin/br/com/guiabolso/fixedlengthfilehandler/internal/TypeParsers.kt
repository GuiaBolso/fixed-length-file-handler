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

@file:Suppress("MatchingDeclarationName")

package br.com.guiabolso.fixedlengthfilehandler.internal

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@PublishedApi
internal inline fun <reified T> defaultTypeParser(parse: String): T = parse.parseToType(T::class)

@Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
@PublishedApi
internal fun <T> String.parseToType(type: KClass<*>): T {
    if(type.isSubclassOf(Enum::class)) return parseToEnum(type) as T

    return when (type) {
        String::class        -> parseToString()
        Int::class           -> parseToInt()
        UInt::class          -> parseToUInt()
        Double::class        -> parseToDouble()
        Long::class          -> parseToLong()
        ULong::class         -> parseToULong()
        Char::class          -> parseToChar()
        Boolean::class       -> parseToBoolean()
        LocalDate::class     -> parseToLocalDate()
        LocalDateTime::class -> parseToLocalDateTime()
        LocalTime::class     -> parseToLocalTime()
        BigDecimal::class    -> parseToBigDecimal()
        else                 -> throw NoParserForClass(type)
    } as T
}

public class NoParserForClass(
    klass: KClass<*>
) : RuntimeException("There are no default parsers for class $klass. Please provide a custom parser")

private fun String.parseToString() = this

private fun String.parseToInt() = this.toInt()

private fun String.parseToUInt() = this.toUInt()

private fun String.parseToDouble() = this.toDouble()

private fun String.parseToLong() = this.toLong()

private fun String.parseToULong() = this.toULong()

private fun String.parseToChar() = this.single()

private fun String.parseToBoolean() = this.toBoolean()

private fun String.parseToLocalDate() = LocalDate.parse(this)

private fun String.parseToLocalDateTime() = LocalDateTime.parse(this)

private fun String.parseToLocalTime() = LocalTime.parse(this)

private fun String.parseToBigDecimal() = this.toBigDecimal()

private fun String.parseToEnum(enumClass: KClass<*>): Enum<*> {
    val enumConstants = enumClass.java.enumConstants as Array<Enum<*>>
    return enumConstants.first { it.name == this }
}

@PublishedApi
internal inline fun <reified T : Number> String.parseToDecimal(scale: Int): T {
    val builder = StringBuilder(this)
    builder.insert(this.length - scale, '.')
    val string = builder.toString()
    
    return when (T::class) {
        Double::class     -> string.toDouble()
        BigDecimal::class -> string.toBigDecimal()
        else              -> throw NoDecimalParserForClass(T::class)
    } as T
}

@PublishedApi
internal class NoDecimalParserForClass(
    klass: KClass<*>
) : RuntimeException("There are no default decimal parsers for class $klass. Please use a custom parser instead.")
