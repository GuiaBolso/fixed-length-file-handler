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

public sealed class Padding(private val removePadding: (String) -> String) {
    
    public fun removePadding(string: String): String = removePadding.invoke(string)
    
    public object NoPadding : Padding({ it })
    
    public class PaddingLeft(paddingChar: Char) : Padding({ line -> line.dropWhile { it == paddingChar } })
    
    public class PaddingRight(paddingChar: Char) : Padding({ line -> line.dropLastWhile { it == paddingChar } })
}
