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

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.android.notemaker.R
import com.raywenderlich.android.notemaker.data.model.Color
import kotlinx.android.synthetic.main.view_color.view.*

/**
 * Adapter for displaying colors in "Save Note" screen.
 */
class ColorsAdapter(
    private val layoutInflater: LayoutInflater
) : RecyclerView.Adapter<ColorsAdapter.ColorViewHolder>() {

  interface OnColorClickListener {

    fun onColorClicked(color: Color)
  }

  private val colors = mutableListOf<Color>()

  private var colorClickListener: OnColorClickListener? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder =
      ColorViewHolder(layoutInflater.inflate(R.layout.view_color, parent, false))

  override fun getItemCount(): Int = colors.size

  override fun onBindViewHolder(holder: ColorViewHolder, position: Int) =
      holder.bindData(colors[position])

  fun setData(newColors: List<Color>) {
    colors.clear()
    colors.addAll(newColors)
    notifyDataSetChanged()
  }

  fun setOnColorClickListener(listener: OnColorClickListener) {
    colorClickListener = listener
  }

  inner class ColorViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    fun bindData(color: Color) = with(view) {

      // Setup view's color
      val drawableBackground = colorView.background
      drawableBackground.colorFilter = PorterDuffColorFilter(
          android.graphics.Color.parseColor(color.hex),
          PorterDuff.Mode.MULTIPLY
      )
      colorView.background = drawableBackground

      // Setup color's onClick listener
      setOnClickListener { colorClickListener?.onColorClicked(color) }
    }
  }
}