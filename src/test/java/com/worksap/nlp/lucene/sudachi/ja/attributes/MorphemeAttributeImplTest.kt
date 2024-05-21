/*
 * Copyright (c) 2024 Works Applications Co., Ltd.
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

package com.worksap.nlp.lucene.sudachi.ja.attributes

import com.worksap.nlp.lucene.aliases.ToXContent
import com.worksap.nlp.sudachi.Config
import com.worksap.nlp.sudachi.DictionaryFactory
import com.worksap.nlp.sudachi.Morpheme
import com.worksap.nlp.test.TestDictionary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Rule

class MorphemeAttributeImplTest {
  @JvmField @Rule var testDic = TestDictionary("system")

  private lateinit var config: Config

  fun getFirstMorpheme(text: String): Morpheme? {
    val dict = DictionaryFactory().create(config)
    val tok = dict.create()
    val morphemes = tok.tokenize(text)

    return if (morphemes.size == 0) null else morphemes.get(0)
  }

  @Before
  fun setup() {
    val configDir = testDic.root.toPath().resolve("config/sudachi")
    config = Config.fromFile(configDir.resolve("sudachi.json"))
  }

  @Test
  fun setMorpheme() {
    var morphemeAtt = MorphemeAttributeImpl()
    assertNull(morphemeAtt.getMorpheme())

    val morph = getFirstMorpheme("東京都")!!
    morphemeAtt.setMorpheme(morph)
    assertEquals(morph, morphemeAtt.getMorpheme())
  }

  @Test
  fun reflectMorpheme() {
    var morphemeAtt = MorphemeAttributeImpl()
    val morph = getFirstMorpheme("東京都")!!
    morphemeAtt.setMorpheme(morph)

    morphemeAtt.reflectWith(
        fun(attClass, key, value) {
          assertEquals(MorphemeAttribute::class.java, attClass)
          assertEquals("morpheme", key)
          assertTrue(value is ToXContent)
        })
  }
}
