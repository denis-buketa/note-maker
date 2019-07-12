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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.android.notemaker.R
import com.raywenderlich.android.notemaker.features.savenote.SaveNoteActivity
import kotlinx.android.synthetic.main.activity_notes_overview.*

/**
 * Represents "Notes" screen. This is the first screen after "Splash" screen.
 */
class NotesOverviewActivity : AppCompatActivity(), NotesOverviewAdapter.OnNoteClickListener {

  private lateinit var viewModel: NotesOverviewViewModel
  private lateinit var notesOverviewAdapter: NotesOverviewAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_notes_overview)

    // Initialize toolbar with the title
    initToolbar()

    // Initialize view model and start observing LiveData
    initViewModel()

    // Initialize adapter and recycler view for displaying notes
    initNotesRecyclerView()

    // Initialize click listener for "Add Note" button
    initAddNoteClickListener()
  }

  override fun onResume() {
    super.onResume()
    viewModel.fetchNotes()
  }

  private fun initToolbar() {
    // Set title in toolbar
    screenTitle.setText(R.string.notes_overview_screen_title)
  }

  private fun initViewModel() {

    // Create ViewModel
    viewModel = ViewModelProviders.of(this).get(NotesOverviewViewModel::class.java)

    // Observe LiveData
    viewModel.notes.observe(this,
        Observer<List<NoteOverviewItemData>> { notesOverviewAdapter.setData(it) }
    )
  }

  private fun initNotesRecyclerView() {

    // Initialize adapter
    notesOverviewAdapter = NotesOverviewAdapter(layoutInflater)
    notesOverviewAdapter.setOnNoteClickListener(this)

    // Initialize layout manager
    val layoutManager = LinearLayoutManager(this)
    layoutManager.orientation = RecyclerView.VERTICAL

    // Initialize notes recycler view
    notes.layoutManager = layoutManager
    notes.adapter = notesOverviewAdapter
  }

  private fun initAddNoteClickListener() {
    // On "Add Note" button click, open SaveNoteActivity
    addNoteButton.setOnClickListener { this.startActivity(SaveNoteActivity.newIntent(this)) }
  }

  // On Note click, open SaveNoteActivity but pass note's Id
  override fun onNoteClicked(noteId: Long) =
      this.startActivity(SaveNoteActivity.newIntent(this, noteId))
}
