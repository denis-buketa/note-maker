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
package com.raywenderlich.android.notemaker.features.notesoverview

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.android.notemaker.R
import kotlinx.android.synthetic.main.view_note.view.*

/**
 * Adapter for displaying notes in "Notes" screen.
 */
class NotesOverviewAdapter(
    private val layoutInflater: LayoutInflater
) : RecyclerView.Adapter<NotesOverviewAdapter.NoteViewHolder>() {

  interface OnNoteClickListener {

    fun onNoteClicked(noteId: Long)
  }

  private val notes = mutableListOf<NoteOverviewItemData>()

  private var clickListener: OnNoteClickListener? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder =
      NoteViewHolder(layoutInflater.inflate(R.layout.view_note, parent, false))

  override fun getItemCount(): Int = notes.size

  override fun onBindViewHolder(holder: NoteViewHolder, position: Int) =
      holder.bindData(notes[position])

  fun setData(newNotes: List<NoteOverviewItemData>) {
    notes.clear()
    notes.addAll(newNotes)
    notifyDataSetChanged()
  }

  fun setOnNoteClickListener(listener: OnNoteClickListener) {
    clickListener = listener
  }

  inner class NoteViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    fun bindData(item: NoteOverviewItemData) {
      with(item) {

        // Setup note content
        view.title.text = note.title
        view.note.text = note.content

        // Setup note color
        val drawableBackground = view.root.background
        drawableBackground.colorFilter = PorterDuffColorFilter(
            Color.parseColor(color.hex),
            PorterDuff.Mode.MULTIPLY
        )
        view.root.background = drawableBackground

        // Setup note's onClick listener
        view.root.setOnClickListener { clickListener?.onNoteClicked(note.id) }
      }
    }
  }
}