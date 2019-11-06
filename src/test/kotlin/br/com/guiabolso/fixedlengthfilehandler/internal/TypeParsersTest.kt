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
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrowAny
import io.kotlintest.specs.ShouldSpec
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class TypeParsersTest : ShouldSpec() {
    
    init {
        should("Parse String") {
            "String".parseToType(String::class) shouldBe "String"
        }
        
        should("Parse Int") {
            "123".parseToType(Int::class) shouldBe 123
        }
        
        should("Parse Double") {
            "123.4567".parseToType(Double::class) shouldBe (123.4567 plusOrMinus 0.0001)
        }
        
        should("Parse Long") {
            Long.MAX_VALUE.toString().parseToType(Long::class) shouldBe Long.MAX_VALUE
        }
        
        should("Parse to Char") {
            "a".parseToType(Char::class) shouldBe 'a'
            shouldThrowAny { "ab".parseToType(Char::class) }
        }
        
        should("Parse to Boolean") {
            "true".parseToType(Boolean::class) shouldBe true
            "false".parseToType(Boolean::class) shouldBe false
            "TRUE".parseToType(Boolean::class) shouldBe true
        }
        
        should("Parse to LocalDate") {
            "2019-02-09".parseToType(LocalDate::class) shouldBe LocalDate.of(2019, 2, 9)
            shouldThrowAny { "2019-2-9".parseToType(LocalDate::class) }
        }
        
        should("Parse to LocalDateTime") {
            "2019-02-09T03:55:55".parseToType(LocalDateTime::class) shouldBe LocalDateTime.of(2019, 2, 9, 3, 55, 55)
        }
        
        should("Parse to BigDecimal") {
            "123456789.123456789".parseToType(BigDecimal::class) shouldBe BigDecimal("123456789.123456789")
        }
    }
}