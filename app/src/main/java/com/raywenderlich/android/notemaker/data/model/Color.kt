/*
 * Copyright (c) 2019 Razeware LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish, 
 * distribute, sublicense, create a derivative work, and/or sell copies of the 
 * Software in any work that is designed, intended, or marketed for pedagogical or 
 * instructional purposes related to programming, coding, application development, 
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works, 
 * or sale is expressly withheld.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.raywenderlich.android.notemaker.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Color(
    @ColumnInfo(name = "hex") val hex: String?,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
) {

  companion object {

    val DEFAULT_COLOR = Color("#FFFFFF", 1)

    val DEFAULT_COLORS = listOf(
        Color("#FFFFFF", 1), // White
        Color("#E57373", 2), // Red
        Color("#F06292", 3), // Pink
        Color("#CE93D8", 4), // Purple
        Color("#2196F3", 5), // Blue
        Color("#00ACC1", 6), // Cyan
        Color("#26A69A", 7), // Teal
        Color("#4CAF50", 8), // Green
        Color("#8BC34A", 9), // Light Green
        Color("#CDDC39", 10), // Lime
        Color("#FFEB3B", 11), // Yellow
        Color("#FF9800", 12), // Orange
        Color("#BCAAA4", 13), // Brown
        Color("#9E9E9E", 14)  // Gray
    )
  }
}