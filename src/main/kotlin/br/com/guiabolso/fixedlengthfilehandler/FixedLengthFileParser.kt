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

@file:Suppress("CanBePrimaryConstructorProperty")

package br.com.guiabolso.fixedlengthfilehandler

import br.com.guiabolso.fixedlengthfilehandler.Padding.NoPadding
import br.com.guiabolso.fixedlengthfilehandler.internal.defaultFileParser
import java.io.InputStream

public fun <T> fixedLengthFileParser(
    fileStream: InputStream,
    recordBuilder: FieldProvider.() -> T
): Sequence<T> {
    return FixedLengthFileParser(
        fileStream,
        listOf(RecordMapping({ true }, { recordBuilder(this as FixedLengthFileParser<T>) } ))
    ).buildSequence()
}

@JvmName("fixedLengthFileParserTemplates")
public fun fixedLengthFileParser(
    fileStream: InputStream,
    recordBuilderMappings: RecordProvider<Any>.() -> Unit
): Sequence<Any> {
    val parser = FixedLengthFileParser<Any>(fileStream, emptyList())
    parser.apply(recordBuilderMappings)
    return parser.buildSequence()
}


interface FieldProvider {
    val currentLine: String
}

public inline fun <reified R : Any> FieldProvider.field(
    from: Int,
    toExclusive: Int,
    padding: Padding = NoPadding,
    unpaddedValueParser: String.() -> R = ::defaultFileParser
): R {
    val stringBlock = currentLine.substring(from, toExclusive)
    val stringWithRemovedPadding = padding.removePadding(stringBlock)
    return stringWithRemovedPadding.unpaddedValueParser()
}

interface RecordProvider<T> : FieldProvider {
    val recordMappings: MutableList<RecordMapping<out T>>
}

public inline fun <reified R : Any> RecordProvider<Any>.withRecord(
    noinline lineSelector: (String) -> Boolean,
    noinline recordBuilder: FixedLengthFileParser<*>.() -> R
) {
    val mapping = RecordMapping(lineSelector, recordBuilder)
    recordMappings.add(mapping)
}

public class FixedLengthFileParser<T>(
    private val fileStream: InputStream,
    recordMappings: List<RecordMapping<out T>>
) : RecordProvider<T> {
    
    override lateinit var currentLine: String
    
    override var recordMappings: MutableList<RecordMapping<out T>> = recordMappings.toMutableList()
    
    public fun buildSequence(): Sequence<T> {
        return fileStream.bufferedReader().lineSequence().map {
            currentLine = it
            recordMapperFor(it).recordBuilder(this)
        }
    }
    
    private fun recordMapperFor(line: String): RecordMapping<out T> =
        recordMappings.find { it.lineSelector(line) } ?: throw NoRecordMappingException(line)
    
}

public class RecordMapping<R>(
    public val lineSelector: (String) -> Boolean,
    public val recordBuilder: FixedLengthFileParser<*>.() -> R
)

public class NoRecordMappingException(
    public val line: String
) : RuntimeException("There are no valid record mappers for line $line")
