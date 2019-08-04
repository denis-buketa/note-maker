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
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.raywenderlich.android.notemaker.R
import com.raywenderlich.android.notemaker.data.model.Color
import com.raywenderlich.android.notemaker.features.savenote.SaveNoteViewModel.Companion.INVALID_NOTE_ID
import com.raywenderlich.android.notemaker.features.savenote.colorpicker.ColorItemDecoration
import com.raywenderlich.android.notemaker.features.savenote.colorpicker.ColorsAdapter
import kotlinx.android.synthetic.main.activity_save_note.*
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
    setContentView(R.layout.activity_save_note)

    // Request to be layout full screen
    requestToBeLayoutFullscreen()

    // Adapt view according to insets
    adaptViewForInsets()

    // Extract arguments (like note Id)
    extractArguments()

    // Initialize toolbar with the title
    initToolbar()

    // Initialize adapter and recycler view for displaying colors
    initColors()

    // Initialize OnClick listeners
    initOnClickListener()

    // Initialize view model, start observing LiveData, and request data
    initViewModel()
  }

  private fun requestToBeLayoutFullscreen() {
    root.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
  }

  private fun adaptViewForInsets() {

    // Prepare original top padding of the toolbar
    val toolbarOriginalTopPadding = toolbar.paddingTop

    // Prepare original peek height of the bottom sheet
    val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
    val bottomSheetOriginalPeekHeight = bottomSheetBehavior.peekHeight

    // Prepare original bottom margin of colors recycler view
    val colorsLayoutParams = colors.layoutParams as ViewGroup.MarginLayoutParams
    val colorsOriginalMarginBottom = colorsLayoutParams.bottomMargin

    // Register OnApplyWindowInsetsListener
    root.setOnApplyWindowInsetsListener { _, windowInsets ->

      // Update toolbar's top padding to accommodate system window top inset
      val newToolbarTopPadding = toolbarOriginalTopPadding + windowInsets.systemWindowInsetTop
      toolbar.updatePadding(top = newToolbarTopPadding)

      // Update colors recycler view's bottom margin to accommodate system window bottom inset
      val newColorsMarginBottom = colorsOriginalMarginBottom + windowInsets.systemWindowInsetBottom
      colorsLayoutParams.bottomMargin = newColorsMarginBottom
      colors.layoutParams = colorsLayoutParams

      // Update bottom sheet's peek height
      adaptBottomSheetPeekHeight(bottomSheetBehavior, bottomSheetOriginalPeekHeight, windowInsets)

      // Exclude gestures on colors recycler view when bottom sheet is expanded
      excludeGesturesForColors(bottomSheetBehavior, windowInsets)

      windowInsets
    }
  }

  private fun adaptBottomSheetPeekHeight(
      bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>,
      bottomSheetOriginalPeekHeight: Int,
      windowInsets: WindowInsets
  ) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

      // If Q, update peek height according to gesture inset bottom
      val gestureInsets = windowInsets.systemGestureInsets
      bottomSheetBehavior.peekHeight = bottomSheetOriginalPeekHeight + gestureInsets.bottom

    } else {

      // If not Q, update peek height according to system window inset bottom
      bottomSheetBehavior.peekHeight = bottomSheetOriginalPeekHeight + windowInsets.systemWindowInsetBottom
    }
  }

  private fun excludeGesturesForColors(
      bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>,
      windowInsets: WindowInsets
  ) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

      bottomSheetBehavior.setBottomSheetCallback(object :
          BottomSheetBehavior.BottomSheetCallback() {

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
          // NO OP
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {

          if (newState == STATE_EXPANDED) {

            // Exclude gestures when bottom sheet is expanded
            root.doOnLayout {

              val gestureInsets = windowInsets.systemGestureInsets

              // Common Rect values
              val rectHeight = colors.height
              val rectTop = root.bottom - rectHeight
              val rectBottom = root.bottom

              // Left Rect values
              val leftExclusionRectLeft = 0
              val leftExclusionRectRight = gestureInsets.left

              // Right Rect values
              val rightExclusionRectLeft = root.right - gestureInsets.right
              val rightExclusionRectRight = root.right

              // Rect for gestures on the left side of the screen
              val leftExclusionRect = Rect(
                  leftExclusionRectLeft,
                  rectTop,
                  leftExclusionRectRight,
                  rectBottom
              )

              // Rect for gestures on the right side of the screen
              val rightExclusionRect = Rect(
                  rightExclusionRectLeft,
                  rectTop,
                  rightExclusionRectRight,
                  rectBottom
              )

              // Add both rects and exclude gestures
              root.systemGestureExclusionRects = listOf(leftExclusionRect, rightExclusionRect)
            }
          } else if (newState == STATE_COLLAPSED) {

            // Remove exclusion rects when bottom sheet is collapsed
            root.doOnLayout { root.systemGestureExclusionRects = listOf() }
          }
        }
      })
    }
  }

  private fun extractArguments() {
    // Extract note Id
    noteId = intent.getLongExtra(EXTRA_NOTE_ID, INVALID_NOTE_ID)
  }

  private fun initToolbar() {
    // Set title in toolbar
    screenTitle.setText(R.string.save_note_screen_title)
  }

  private fun initOnClickListener() {

    // Save note when "Save Note" button clicked
    saveChangesButton.setOnClickListener { onSaveChangesClicked() }

    // Delete note when "Delete" option selected
    deleteNoteOption.setOnClickListener { viewModel.deleteNote() }
  }

  private fun initColors() {

    // Initialize adapter
    colorsAdapter = ColorsAdapter(layoutInflater)
    colorsAdapter.setOnColorClickListener(this)

    // Initialize layout manager
    val linearLayoutManager = LinearLayoutManager(this)
    linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL

    // Initialize colors recycler view
    colors.layoutManager = linearLayoutManager
    colors.addItemDecoration(ColorItemDecoration(colorItemOuterMargin, colorItemInnerMargin))
    colors.adapter = colorsAdapter
  }

  private fun initViewModel() {

    // Create ViewModel
    viewModel = ViewModelProviders.of(this).get(SaveNoteViewModel::class.java)

    // Observe LiveData
    viewModel.closeScreenEvent.observe(this, Observer<Any> { finish() })
    viewModel.viewData.observe(this, Observer { renderViewData(it) })
    viewModel.colors.observe(this, Observer { renderColors(it) })

    // Initialize fetching of the ViewData and colors
    viewModel.fetchViewData(noteId)
    viewModel.fetchColors()
  }

  private fun renderViewData(viewData: SaveNoteViewData) = with(viewData) {

    // Render note content
    titleEditText.setText(title)
    noteEditText.setText(note)

    // Render note color background
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