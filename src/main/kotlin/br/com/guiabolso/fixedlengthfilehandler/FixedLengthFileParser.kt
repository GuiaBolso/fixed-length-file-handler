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

package br.com.guiabolso.fixedlengthfilehandler

import br.com.guiabolso.fixedlengthfilehandler.Padding.NoPadding
import br.com.guiabolso.fixedlengthfilehandler.internal.defaultTypeParser
import br.com.guiabolso.fixedlengthfilehandler.internal.parseToDecimal
import java.io.InputStream

/**
 * Parse a [fileStream] into records defined by [recordBuilder]
 *
 * ```
 * // File:
 * // aaaa1232019-02-09
 * // bbbb4562019-03-10
 * // cccc7892019-04-11
 *
 * // Record:
 * data class Foo(val string: String, val int: Int, val date: LocalDate)
 *
 * val sequence: Sequence<Foo> = fixedLengthFileParser<Foo>(stream) {
 *   Foo(
 *      field(0, 4),
 *      field(4, 7),
 *      field(7, 17)
 *   )
 * }
 * ```
 *
 * IMPORTANT: You must close [fileStream] after using it. It would be impossible to close it automatically as the
 * processing is lazy and the stream is kept open while the `Sequence` is being used.
 */
public fun <T> fixedLengthFileParser(
    fileStream: InputStream,
    recordBuilder: FixedLengthFileParser<T>.() -> T
): Sequence<T> {
    val parser = MultiFixedLengthFileParser<T>(fileStream, emptyList())
    parser.apply { withRecord({ true }, recordBuilder) }
    return parser.buildSequence()
}


/**
 * Parse a [fileStream] into records defined by multiple record builders
 *
 * ```
 * // File:
 * // 1aaaa1232019-02-09
 * // 1bbbb4562019-03-10
 * // 2aaaa123
 *
 * // Record:
 * data class Foo(val string: String, val int: Int, val date: LocalDate)
 * data class Bar(val string: String, val int: Int)
 *
 * val sequence: Sequence<Foo> = multiFixedLengthFileParser<Any>(stream) {
 *   withRecord({ line -> line[0] == '1'}) {
 *      Foo(field(1, 4), field(5, 8), field(8, 18))
 *   }
 *
 *   withRecord({ line -> line[0] == '2' }) {
 *      Bar(field(1,4), field(5, 8))
 *   }
 * }
 * ```
 *
 * IMPORTANT: You must close [fileStream] after using it. It would be impossible to close it automatically as the
 * processing is lazy and the stream is kept open while the `Sequence` is being used.
 */
public fun <T> multiFixedLengthFileParser(
    fileStream: InputStream,
    recordBuilderMappings: MultiFixedLengthFileParser<T>.() -> Unit
): Sequence<T> {
    val parser = MultiFixedLengthFileParser<T>(fileStream, emptyList())
    parser.apply(recordBuilderMappings)
    return parser.buildSequence()
}

public open class FixedLengthFileParser<T>(
    private val fileStream: InputStream,
    recordMappings: List<FixedLengthFileParser<T>.RecordMapping>
) {

    @PublishedApi
    internal lateinit var currentLine: String

    @PublishedApi
    internal var recordMappings: MutableList<RecordMapping> = recordMappings.toMutableList()

    @Suppress("TooGenericExceptionCaught")
    public fun buildSequence(): Sequence<T> {
        return fileStream.bufferedReader().lineSequence().map {
            currentLine = it

            try {
                recordMapperFor(it).recordBuilder(this)
            } catch (exception: Exception) {
                throw LineParseException(currentLine, exception)
            }
        }
    }

    private fun recordMapperFor(line: String): RecordMapping =
        recordMappings.find { it.lineSelector(line) } ?: throw NoRecordMappingException(line)

    public inline fun <reified R> field(
        from: Int,
        toExclusive: Int,
        padding: Padding = NoPadding,
        unpaddedValueParser: String.() -> R = ::defaultTypeParser
    ): R {
        val stringBlock = currentLine.substring(from, toExclusive)
        val stringWithRemovedPadding = padding.removePadding(stringBlock)
        return stringWithRemovedPadding.unpaddedValueParser()
    }

    /**
     * Field will start at [from] and will stop at the end of the line
     */
    public inline fun <reified R> field(
        from: Int,
        padding: Padding = NoPadding,
        unpaddedValueParser: String.() -> R = ::defaultTypeParser
    ): R {
        val stringBlock = currentLine.substring(from)
        val stringWithRemovedPadding = padding.removePadding(stringBlock)
        return stringWithRemovedPadding.unpaddedValueParser()
    }
    
    public inline fun <reified R : Number> decimalField(
        from: Int,
        toExclusive: Int,
        scale: Int,
        padding: Padding = NoPadding
    ): R {
        val stringBlock = currentLine.substring(from, toExclusive)
        val stringWithRemovedPadding = padding.removePadding(stringBlock)
        
        return stringWithRemovedPadding.parseToDecimal(scale)
    }
    
    public inner class RecordMapping(
        public val lineSelector: (String) -> Boolean,
        public val recordBuilder: FixedLengthFileParser<T>.() -> T
    )
}

public class MultiFixedLengthFileParser<T>(
    private val fileStream: InputStream,
    recordMappings: List<FixedLengthFileParser<T>.RecordMapping>
) : FixedLengthFileParser<T>(fileStream, recordMappings) {

    public fun withRecord(
        lineSelector: (String) -> Boolean,
        recordBuilder: FixedLengthFileParser<T>.() -> T
    ) {
        val mapping = RecordMapping(lineSelector, recordBuilder)
        recordMappings.add(mapping)
    }
}

public class NoRecordMappingException(
    public val line: String
) : RuntimeException("There are no valid record mappers for line $line")

public class LineParseException(
    public val line: String,
    public override val cause: Exception
) : RuntimeException("Failed to parse line", cause)
