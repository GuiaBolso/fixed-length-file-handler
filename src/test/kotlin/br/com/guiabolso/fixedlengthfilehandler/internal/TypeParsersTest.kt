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

import io.kotlintest.matchers.doubles.plusOrMinus
import io.kotlintest.matchers.withClue
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrowAny
import io.kotlintest.specs.ShouldSpec
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TypeParsersTest : ShouldSpec() {
    
    init {
        should("Parse String") {
            "String".parseToType<String>(String::class) shouldBe "String"
        }
        
        should("Parse Int") {
            "123".parseToType<Int>(Int::class) shouldBe 123
        }

        should("Parse UInt") {
            "123".parseToType<UInt>(UInt::class) shouldBe 123u
        }
        
        should("Parse Double") {
            "123.4567".parseToType<Double>(Double::class) shouldBe (123.4567 plusOrMinus 0.0001)
        }
        
        should("Parse Long") {
            Long.MAX_VALUE.toString().parseToType<Long>(Long::class) shouldBe Long.MAX_VALUE
        }

        should("Parse ULong") {
            ULong.MAX_VALUE.toString().parseToType<ULong>(ULong::class) shouldBe ULong.MAX_VALUE
        }
        
        should("Parse to Char") {
            "a".parseToType<Char>(Char::class) shouldBe 'a'
            shouldThrowAny { "ab".parseToType(Char::class) }
        }
        
        should("Parse to Boolean") {
            "true".parseToType<Boolean>(Boolean::class) shouldBe true
            "false".parseToType<Boolean>(Boolean::class) shouldBe false
            "TRUE".parseToType<Boolean>(Boolean::class) shouldBe true
        }
        
        should("Parse to LocalDate") {
            "2019-02-09".parseToType<LocalDate>(LocalDate::class) shouldBe LocalDate.of(2019, 2, 9)
            shouldThrowAny { "2019-2-9".parseToType(LocalDate::class) }
        }
        
        should("Parse to LocalDateTime") {
            "2019-02-09T03:55:55".parseToType<LocalDateTime>(LocalDateTime::class) shouldBe 
                    LocalDateTime.of(2019, 2, 9, 3, 55, 55)
        }

        should("Parse to LocalTime") {
            "03:55:55".parseToType<LocalTime>(LocalTime::class) shouldBe LocalTime.of(3, 55, 55)
        }
        
        should("Parse to BigDecimal") {
            "123456789.123456789".parseToType<BigDecimal>(BigDecimal::class) shouldBe BigDecimal("123456789.123456789")
        }

        should("Parse to Enum class") {
            "FOO".parseToType<MyEnum>(MyEnum::class) shouldBe MyEnum.FOO
            withClue("Can't allow ignore case. FOO != FoO in enums") {
                shouldThrowAny { "bar".parseToType<MyEnum>(MyEnum::class) }
            }
            shouldThrowAny { "Fab".parseToType<MyEnum>(MyEnum::class) }
        }
    }

    private enum class MyEnum { FOO, BAR }
}
