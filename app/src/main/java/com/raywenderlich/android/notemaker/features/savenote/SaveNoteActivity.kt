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
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.raywenderlich.android.notemaker.R
import com.raywenderlich.android.notemaker.data.model.Color
import com.raywenderlich.android.notemaker.features.savenote.SaveNoteViewModel.Companion.INVALID_NOTE_ID
import com.raywenderlich.android.notemaker.features.savenote.colorpicker.ColorItemDecoration
import com.raywenderlich.android.notemaker.features.savenote.colorpicker.ColorsAdapter
import kotlinx.android.synthetic.main.activity_add_note.*
import kotlinx.android.synthetic.main.bottom_sheet_more.*

class SaveNoteActivity : AppCompatActivity(), ColorsAdapter.OnColorClickListener {

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

  private val colorItemInnerMargin by lazy {
    resources.getDimensionPixelSize(R.dimen.color_item_inner_margin)
  }
  private val colorItemOuterMargin by lazy {
    resources.getDimensionPixelSize(R.dimen.color_item_outer_margin)
  }

  private lateinit var viewModel: SaveNoteViewModel
  private lateinit var colorsAdapter: ColorsAdapter
  private var noteId: Long = INVALID_NOTE_ID

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_note)

    requestToBeLayoutFullscreen()
    handleInsets()
    extractArguments()
    initToolbar()
    initColors()
    initOnClickListener()
    initViewModel()
  }

  private fun requestToBeLayoutFullscreen() {
    root.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
  }

  private fun handleInsets() {

    val toolbarOriginalTopPadding = toolbar.paddingTop

    val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
    val bottomSheetOriginalPeekHeight = bottomSheetBehavior.peekHeight

    val colorsLayoutParams = colors.layoutParams as ViewGroup.MarginLayoutParams
    val colorsOriginalMarginBottom = colorsLayoutParams.bottomMargin

    root.setOnApplyWindowInsetsListener { _, windowInsets ->

      val newToolbarTopPadding = toolbarOriginalTopPadding + windowInsets.systemWindowInsetTop
      toolbar.setPadding(0, newToolbarTopPadding, 0, 0)

      val newColorsMarginBottom = colorsOriginalMarginBottom + windowInsets.systemWindowInsetBottom
      colorsLayoutParams.bottomMargin = newColorsMarginBottom
      colors.layoutParams = colorsLayoutParams

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

        val gestureInsets = windowInsets.systemGestureInsets
        bottomSheetBehavior.peekHeight = bottomSheetOriginalPeekHeight + gestureInsets.bottom
      }

      windowInsets
    }
  }

  private fun extractArguments() {
    noteId = intent.getLongExtra(EXTRA_NOTE_ID, INVALID_NOTE_ID)
  }

  private fun initToolbar() {
    screenTitle.setText(R.string.save_note_screen_title)
  }

  private fun initOnClickListener() {
    saveChangesButton.setOnClickListener { onSaveChangesClicked() }
    deleteNoteOption.setOnClickListener { viewModel.deleteNote() }
  }

  private fun initColors() {

    colorsAdapter = ColorsAdapter(layoutInflater)
    colorsAdapter.setOnColorClickListener(this)

    val linearLayoutManager = LinearLayoutManager(this)
    linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL

    colors.layoutManager = linearLayoutManager
    colors.addItemDecoration(ColorItemDecoration(colorItemOuterMargin, colorItemInnerMargin))
    colors.adapter = colorsAdapter
  }

  private fun initViewModel() {
    viewModel = ViewModelProviders.of(this).get(SaveNoteViewModel::class.java)
    viewModel.closeScreenEvent.observe(this, Observer<Any> { finish() })
    viewModel.viewData.observe(this, Observer { renderViewData(it) })
    viewModel.colors.observe(this, Observer { renderColors(it) })

    viewModel.fetchViewData(noteId)
    viewModel.fetchColors()
  }

  private fun renderViewData(viewData: SaveNoteViewData) = with(viewData) {
    titleEditText.setText(title)
    noteEditText.setText(note)
    noteContainer.setBackgroundColor(android.graphics.Color.parseColor(viewData.noteColor.hex))
  }

  private fun renderColors(colors: List<Color>) {
    colorsAdapter.setData(colors)
  }

  override fun onColorClicked(color: Color) {
    val title = titleEditText.text.toString()
    val note = noteEditText.text.toString()
    viewModel.colorNote(title, note, color)
  }

  private fun onSaveChangesClicked() {
    val title = titleEditText.text.toString()
    val note = noteEditText.text.toString()
    viewModel.saveNote(title, note)
  }
}