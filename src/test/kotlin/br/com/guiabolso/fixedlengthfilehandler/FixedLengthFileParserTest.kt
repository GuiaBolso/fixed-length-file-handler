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

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.ShouldSpec
import java.io.InputStream
import java.time.LocalDate

class FixedLengthFileParserTest : ShouldSpec() {
    
    init {
        should("Read stream with single Strings as lines with fixed-length") {
            val stream = """
                aaaa
                bbbb
                cccc
            """.trimmedInputStream()
        
            fixedLengthFileParser<String>(stream) {
                field(from = 0, toExclusive = 4)
            }.toList() shouldBe listOf("aaaa", "bbbb", "cccc")
        
        }
    
        should("Allow for custom transformation on values") {
            val stream = """
                aaaa
                aaaa
                cccc
            """.trimmedInputStream()
        
            fixedLengthFileParser<String>(stream) {
                field(from = 0, toExclusive = 4) { replace("a", "b") }
            }.toList() shouldBe listOf("bbbb", "bbbb", "cccc")
        }
    
        should("Read stream with more complex types as line with fixed-length") {
            data class Foo(val string: String, val int: Int, val date: LocalDate)
        
            val stream = """
                aaaa1232019-02-09
                bbbb4562019-03-10
                cccc7892019-04-11
            """.trimmedInputStream()
        
            fixedLengthFileParser<Foo>(stream) {
                Foo(
                    field(0, 4),
                    field(4, 7),
                    field(7, 17)
                )
            }.toList() shouldBe listOf(
                Foo("aaaa", 123, LocalDate.of(2019, 2, 9)),
                Foo("bbbb", 456, LocalDate.of(2019, 3, 10)),
                Foo("cccc", 789, LocalDate.of(2019, 4, 11))
            )
        }
    
        should("Allow the elimination of any left padding") {
            val stream = """
                aaaa
                  bb
                   c
            """.trimmedInputStream()
        
            fixedLengthFileParser<String>(stream) {
                field(0, 4, Padding.PaddingLeft(' '))
            }.toList() shouldBe listOf("aaaa", "bb", "c")
        }
    
        should("Allow the elimination of any left padding with a specified char") {
            val stream = """
                aaaa
                00bb
                000c
            """.trimmedInputStream()
        
            fixedLengthFileParser<String>(stream) {
                field(0, 4, Padding.PaddingLeft('0'))
            }.toList() shouldBe listOf("aaaa", "bb", "c")
        }
        
        should("Allow the elimination of any right padding with a specified char") {
            val stream = """
                aaaa
                bb00
                c000
            """.trimmedInputStream()
        
            fixedLengthFileParser<String>(stream) {
                field(0, 4, Padding.PaddingRight('0'))
            }.toList() shouldBe listOf("aaaa", "bb", "c")
        }
        
        should("Read stream with more than one possible record types") {
            abstract class Bar
    
            data class Baz(val type: Int, val fullString: String) : Bar()
            data class Foo(val type: Int, val int: Int, val firstString: String, val secondString: String) : Bar()
            
            val stream = """
                1aaaa
                1bbbb
                21234aabb
                21234ccdd
                1jjjj
            """.trimmedInputStream()
            
            multiFixedLengthFileParser<Bar>(stream) {
                withRecord({it[0] == '1'}) {
                    Baz(
                        field(0, 1),
                        field(1, 5)
                    )
                }
                
                withRecord({ it[0] == '2' }) {
                    Foo(
                        field(0, 1),
                        field(1, 5),
                        field(5, 7),
                        field(7, 9)
                    )
                }
            }.toList() shouldBe listOf(
                Baz(1, "aaaa"),
                Baz(1, "bbbb"),
                Foo(2, 1234, "aa", "bb"),
                Foo(2, 1234, "cc", "dd"),
                Baz(1, "jjjj")
            )
        }
        
        should("Allow auto-casting to a common type when more than one record is present") {
            abstract class Foo(open val type: Int)
            
            data class Bar(override val type: Int, val string: String) : Foo(type)
            data class Baz(override val type: Int, val int: Int) : Foo(type)
            
            val stream = """
                1aaaa
                1bbbb
                24444
                25555
            """.trimmedInputStream()
    
            multiFixedLengthFileParser<Foo>(stream) {
                withRecord({ it[0] == '1' }) {
                    Bar(field(0, 1), field(1, 5))
                }
        
                withRecord({ it[0] == '2' }) {
                    Baz(field(0, 1), field(1, 5))
                }
            } as Sequence<Foo>
        }
        
        should("Throw an exception when trying to parse a line with no mapper for it") {
            val stream = """
                aaaa
                aaaa
                cccc
            """.trimmedInputStream()
    
            shouldThrow<NoRecordMappingException> {
                multiFixedLengthFileParser<String>(stream) {
                    withRecord({ it.contains("b") }) {
                        field<String>(0, 4)
                    }
                }.toList()
            }
        }
        
        should("Parse correctly the documentation example") {
            data class MyUserRecord(val username: String, val userDoc: Int, val registryDate: LocalDate)
            
            val stream = """
                FirstUsername                 1234567892019-02-09
                SecondAndLongerUsername       9876543212018-03-10
                ThirdUsernameWithShorterDoc   0000001232017-04-11
            """.trimmedInputStream()
            
            fixedLengthFileParser<MyUserRecord>(stream) {
                MyUserRecord(
                    field(0, 30, Padding.PaddingRight(' ')),
                    field(30, 39, Padding.PaddingLeft('0')),
                    field(39, 49)
                )
            }.toList() shouldBe listOf(
                MyUserRecord("FirstUsername", 123456789, LocalDate.of(2019, 2, 9)),
                MyUserRecord("SecondAndLongerUsername", 987654321, LocalDate.of(2018, 3, 10)),
                MyUserRecord("ThirdUsernameWithShorterDoc", 123, LocalDate.of(2017, 4, 11))
            )
        }
    }
    
    private fun String.trimmedInputStream(): InputStream = trimIndent().toByteArray().inputStream()
}
