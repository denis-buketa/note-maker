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

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.android.notemaker.R
import com.raywenderlich.android.notemaker.features.savenote.SaveNoteActivity
import kotlinx.android.synthetic.main.activity_notes_overview.*

class NotesOverviewActivity : AppCompatActivity(), NotesOverviewAdapter.OnNoteClickListener {

  private lateinit var viewModel: NotesOverviewViewModel
  private lateinit var notesOverviewAdapter: NotesOverviewAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_notes_overview)

    requestToBeLayoutFullscreen()
    handleInsets()
    initToolbar()
    initViewModel()
    initNotesRecyclerView()
    initAddNoteClickListener()
  }

  private fun requestToBeLayoutFullscreen() {
    root.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
  }

  private fun handleInsets() {

    val toolbarOriginalTopPadding = toolbar.paddingTop

    val addNoteButtonMarginLayoutParam = addNoteButton.layoutParams as ViewGroup.MarginLayoutParams
    val addNoteButtonOriginalBottomMargin = addNoteButtonMarginLayoutParam.bottomMargin

    root.setOnApplyWindowInsetsListener { _, windowInsets ->

      val newToolbarTopPadding = windowInsets.systemWindowInsetTop + toolbarOriginalTopPadding
      toolbar.setPadding(0, newToolbarTopPadding, 0, 0)

      addNoteButtonMarginLayoutParam.bottomMargin =
          addNoteButtonOriginalBottomMargin + windowInsets.systemWindowInsetBottom
      addNoteButton.layoutParams = addNoteButtonMarginLayoutParam

      windowInsets
    }

    notes.setOnApplyWindowInsetsListener { view, windowInsets ->
      view.updatePadding(bottom = windowInsets.systemWindowInsetBottom)
      windowInsets
    }
  }

  private fun initToolbar() {
    screenTitle.setText(R.string.notes_overview_screen_title)
  }

  private fun initViewModel() {
    viewModel = ViewModelProviders.of(this).get(NotesOverviewViewModel::class.java)
    viewModel.notes.observe(this,
        Observer<List<NoteOverviewItemData>> { notesOverviewAdapter.setData(it) })
  }

  private fun initNotesRecyclerView() {
    notesOverviewAdapter = NotesOverviewAdapter(layoutInflater)
    notesOverviewAdapter.setOnNoteClickListener(this)
    val layoutManager = LinearLayoutManager(this)
    layoutManager.orientation = RecyclerView.VERTICAL
    notes.layoutManager = layoutManager
    notes.adapter = notesOverviewAdapter
  }

  private fun initAddNoteClickListener() {
    addNoteButton.setOnClickListener { this.startActivity(SaveNoteActivity.newIntent(this)) }
  }

  override fun onResume() {
    super.onResume()
    viewModel.fetchNotes()
  }

  override fun onNoteClicked(noteId: Long) =
      this.startActivity(SaveNoteActivity.newIntent(this, noteId))
}
