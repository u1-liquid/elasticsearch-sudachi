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

package com.worksap.nlp.lucene.sudachi.ja

import com.worksap.nlp.lucene.sudachi.ja.attributes.MorphemeAttribute
import com.worksap.nlp.lucene.sudachi.ja.attributes.SudachiAttribute
import com.worksap.nlp.lucene.sudachi.ja.attributes.SudachiAttributeFactory
import com.worksap.nlp.sudachi.IOTools
import java.io.StringReader
import java.nio.CharBuffer
import org.apache.lucene.analysis.Tokenizer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute
import org.apache.lucene.util.AttributeFactory

private val MAX_CHUNK_SIZE = 1 * 1024 * 1024
private val INITIAL_CHUNK_SIZE = 32 * 1024
private val CHUNK_GROW_SCALE = 8

class SudachiTokenizer(
    private val tokenizer: CachingTokenizer,
    private val discardPunctuation: Boolean,
    factory: AttributeFactory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY
) : Tokenizer(SudachiAttributeFactory(factory)) {
  private val termAtt = addAttribute<CharTermAttribute>()
  private val morphemeAtt = addAttribute<MorphemeAttribute>()
  private val offsetAtt = addAttribute<OffsetAttribute>()
  private val posIncAtt = addAttribute<PositionIncrementAttribute>()
  private val posLenAtt = addAttribute<PositionLengthAttribute>()

  init {
    addAttribute<SudachiAttribute> { it.dictionary = tokenizer.dictionary }
  }

  // To cope with huge text, input data split into chunks for tokenize.
  // Initial chunk size is INITIAL_CHUNK_SIZE.
  // But it grows, if input data is large (up to MAX_CHUNK_SIZE).
  // TODO: Should split with meaningful delimitations instead of fixed size.
  private var chunk: CharBuffer = CharBuffer.allocate(INITIAL_CHUNK_SIZE)
  private var iterator: MorphemeIterator = MorphemeIterator.EMPTY
  private var offset = 0 // pos from the beginning to current chunk.
  private var endOffset = 0

  override fun reset() {
    super.reset()
    iterator = MorphemeIterator.EMPTY
    offset = 0
    endOffset = 0
  }

  private fun growChunk() {
    val newChunkSize = kotlin.math.min(chunk.capacity() * CHUNK_GROW_SCALE, MAX_CHUNK_SIZE)
    val newChunk = CharBuffer.allocate(newChunkSize)
    chunk.flip()
    newChunk.put(chunk)

    chunk = newChunk
  }

  private fun read(): Boolean {
    chunk.clear()

    while (true) {
      val nread = IOTools.readAsMuchAsCan(input, chunk)
      if (nread < 0) {
        return chunk.position() > 0
      }

      // check: chunk reads all data from Reader. No remaining data in Reader.
      if (chunk.hasRemaining()) {
        return true
      }

      // check: chunk is already max size
      if (chunk.capacity() == MAX_CHUNK_SIZE) {
        return true
      }

      growChunk()
    }
  }

  override fun incrementToken(): Boolean {
    clearAttributes()

    var m = iterator.next()
    if (m == null) {
      if (!read()) {
        return false
      }
      chunk.flip()

      var iter = tokenizer.tokenize(StringReader(chunk.toString()))
      if (discardPunctuation) {
        iter = NonPunctuationMorphemes(iter)
      }
      iterator = iter
      offset = endOffset

      m = iterator.next() ?: return false
    }

    morphemeAtt.setMorpheme(m)
    posLenAtt.positionLength = 1
    posIncAtt.positionIncrement = 1
    val baseOffset = iterator.baseOffset // offset in this chunk
    offsetAtt.setOffset(
        correctOffset(offset + baseOffset + m.begin()),
        correctOffset(offset + baseOffset + m.end()))
    endOffset = offset + baseOffset + m.end()

    termAtt.setEmpty().append(m.surface())
    return true
  }

  override fun end() {
    super.end()
    val lastOffset = correctOffset(offset + iterator.baseOffset)
    offsetAtt.setOffset(lastOffset, lastOffset)
    iterator = MorphemeIterator.EMPTY
  }
}
