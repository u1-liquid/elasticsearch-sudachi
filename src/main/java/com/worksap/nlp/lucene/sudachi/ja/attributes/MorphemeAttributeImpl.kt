/*
 * Copyright (c) 2022-2024 Works Applications Co., Ltd.
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
import com.worksap.nlp.lucene.aliases.ToXContentParams
import com.worksap.nlp.lucene.aliases.XContentBuilder
import com.worksap.nlp.lucene.sudachi.ja.reflect
import com.worksap.nlp.sudachi.Morpheme
import org.apache.lucene.util.AttributeImpl
import org.apache.lucene.util.AttributeReflector

class MorphemeAttributeImpl : AttributeImpl(), MorphemeAttribute {
  private val inner: ToXContentWrapper = ToXContentWrapper(null, listOf())

  private class ToXContentWrapper(morpheme: Morpheme?, offsetMap: List<Int>) : ToXContent {
    private var morpheme = morpheme
    // mapping from the character to the original reader
    private var offsetMap = offsetMap

    override fun toXContent(builder: XContentBuilder, params: ToXContentParams): XContentBuilder {
      builder.value(
          mapOf(
              "surface" to morpheme?.surface(),
              "dictionaryForm" to morpheme?.dictionaryForm(),
              "normalizedForm" to morpheme?.normalizedForm(),
              "readingForm" to morpheme?.readingForm(),
              "partOfSpeech" to morpheme?.partOfSpeech(),
              "offsetMap" to offsetMap,
          ))
      return builder
    }

    fun getMorpheme(): Morpheme? {
      return morpheme
    }

    fun setMorpheme(morpheme: Morpheme?) {
      this.morpheme = morpheme
    }

    fun getOffsets(): List<Int> {
      return offsetMap
    }

    fun setOffsets(offsets: List<Int>) {
      this.offsetMap = offsets
    }
  }

  override fun clear() {
    inner.setMorpheme(null)
    inner.setOffsets(listOf())
  }

  override fun reflectWith(reflector: AttributeReflector) {
    reflector.reflect<MorphemeAttribute>(
        "morpheme", if (inner.getMorpheme() != null) inner else null)
  }

  override fun copyTo(target: AttributeImpl?) {
    (target as? MorphemeAttributeImpl)?.let {
      it.setMorpheme(getMorpheme())
      it.setOffsets(getOffsets())
    }
  }

  override fun getMorpheme(): Morpheme? {
    return inner.getMorpheme()
  }

  override fun setMorpheme(morpheme: Morpheme?) {
    inner.setMorpheme(morpheme)
  }

  override fun getOffsets(): List<Int> {
    return inner.getOffsets()
  }

  override fun setOffsets(offsets: List<Int>) {
    inner.setOffsets(offsets)
  }
}
