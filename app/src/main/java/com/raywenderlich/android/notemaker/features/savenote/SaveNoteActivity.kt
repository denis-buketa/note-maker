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
package com.raywenderlich.android.notemaker.features.savenote

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.raywenderlich.android.notemaker.R
import com.raywenderlich.android.notemaker.features.savenote.SaveNoteViewModel.Companion.INVALID_NOTE_ID
import kotlinx.android.synthetic.main.activity_add_note.*

class SaveNoteActivity : AppCompatActivity() {

  companion object {

    private const val EXTRA_NOTE_ID = "extra_note_id"

    fun newIntent(context: Context) =
        Intent(context, SaveNoteActivity::class.java).apply {
          putExtra(EXTRA_NOTE_ID, INVALID_NOTE_ID)
        }

    fun newIntent(context: Context, noteId: Long) =
        Intent(context, SaveNoteActivity::class.java).apply {
          putExtra(EXTRA_NOTE_ID, noteId)
        }
  }

  private lateinit var viewModel: SaveNoteViewModel
  private var noteId: Long = INVALID_NOTE_ID

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_note)

    extractArguments()
    initToolbar()
    initViewModel()
  }

  private fun extractArguments() {
    noteId = intent.getLongExtra(EXTRA_NOTE_ID, INVALID_NOTE_ID)
  }

  private fun initToolbar() {
    supportActionBar?.title = if (noteId == INVALID_NOTE_ID) "Add Note" else "Edit Note"
  }

  private fun initViewModel() {
    viewModel = ViewModelProviders.of(this).get(SaveNoteViewModel::class.java)
    viewModel.closeScreenEvent.observe(this, Observer<Any> { finish() })
    viewModel.viewData.observe(this, Observer { renderViewData(it) })

    if (noteId != INVALID_NOTE_ID) {
      viewModel.fetchNote(noteId)
    }
  }

  private fun renderViewData(viewData: SaveNoteViewData) = with(viewData) {
    titleEditText.setText(title)
    tagEditText.setText(tag)
    noteEditText.setText(note)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_add_note, menu)
    menu.findItem(R.id.action_delete_note).isVisible = noteId != INVALID_NOTE_ID
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {

    R.id.action_save_changes -> {
      val title = titleEditText.text.toString()
      val note = noteEditText.text.toString()
      val tag = tagEditText.text.toString()
      viewModel.saveNote(title, note, tag)
      true
    }

    R.id.action_delete_note -> {
      viewModel.deleteNote(noteId)
      true
    }

    else -> {
      // If we got here, the user's action was not recognized.
      // Invoke the superclass to handle it.
      super.onOptionsItemSelected(item)
    }
  }
}