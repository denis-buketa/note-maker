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
package com.raywenderlich.android.notemaker.features.savenote.colorpicker

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

private const val FIRST_POSITION_INDEX = 0

/**
 * Decoration for Color items allows easy manipulation with margins of specific items.
 *
 * Put [outerMarginPx] on the left side of first item and right side of the last item in list.
 * Put [innerMarginPx] between items.
 */
class ColorItemDecoration(
    private val outerMarginPx: Int,
    private val innerMarginPx: Int
) : RecyclerView.ItemDecoration() {

  override fun getItemOffsets(
      outRect: Rect,
      view: View,
      parent: RecyclerView,
      state: RecyclerView.State) {
    val position = parent.getChildAdapterPosition(view)
    val itemCount = parent.adapter?.itemCount

    with(outRect) {
      left = if (position == FIRST_POSITION_INDEX) outerMarginPx else innerMarginPx
      right = if (itemCount != null && position == itemCount - 1) outerMarginPx else innerMarginPx
    }
  }
}