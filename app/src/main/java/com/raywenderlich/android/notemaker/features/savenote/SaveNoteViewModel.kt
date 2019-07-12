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

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.raywenderlich.android.notemaker.NoteMakerApplication
import com.raywenderlich.android.notemaker.data.model.Color
import com.raywenderlich.android.notemaker.data.model.Note
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class SaveNoteViewModel(application: Application) : AndroidViewModel(application) {

  companion object {

    const val INVALID_NOTE_ID = -1L

    val CLOSE_SCREEN_EVENT = Object()
  }

  val viewData = MutableLiveData<SaveNoteViewData>()
  val colors = MutableLiveData<List<Color>>()
  val closeScreenEvent = MutableLiveData<Any>()

  private val compositeDisposable = CompositeDisposable()
  private val repository = (application as NoteMakerApplication).dependencyInjector.repository

  private var noteId = INVALID_NOTE_ID

  fun fetchViewData(noteId: Long) {
    this.noteId = noteId

    if (noteId == INVALID_NOTE_ID) {
      viewData.value = SaveNoteViewData.DEFAULT
      return
    }

    compositeDisposable.add(
        fetchNoteRelatedData(noteId)
            .map { mapToViewData(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { viewData.value = it },
                { Log.e("debug_log", "Error while fetching view data: $it") }
            )
    )
  }

  private fun fetchNoteRelatedData(noteId: Long): Single<NoteRelatedData> =
      repository
          .findNoteById(noteId)
          .flatMap { note ->
            repository
                .findColorById(note.colorId)
                .map { color -> NoteRelatedData(note, color) }
          }

  private fun mapToViewData(noteRelatedData: NoteRelatedData): SaveNoteViewData =
      SaveNoteViewData(
          noteRelatedData.note.title,
          noteRelatedData.note.content,
          noteRelatedData.noteColor
      )

  fun fetchColors() =
      compositeDisposable.add(
          repository.getAllColors()
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(
                  { colors.value = it },
                  { Log.e("debug_log", "Error while fetching colors: $it") }
              )
      )

  fun saveNote(title: String, noteContent: String) {

    if (title.isEmpty()) {
      return
    }

    val noteColorId = viewData.value?.noteColor?.id ?: Color.DEFAULT_COLOR.id

    val note = if (noteId == INVALID_NOTE_ID)
      Note(title, noteContent, noteColorId)
    else
      Note(title, noteContent, noteColorId, noteId)

    compositeDisposable.add(
        repository.insertNote(note)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { closeScreen() },
                { Log.e("debug_log", "Error while saving note: $it") }
            )
    )
  }

  fun deleteNote() {

    if (noteId == INVALID_NOTE_ID) {
      return
    }

    compositeDisposable.add(
        repository
            .deleteNote(noteId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { closeScreen() },
                { Log.e("debug_log", "Error while deleting note: $it") }
            )
    )
  }

  fun colorNote(title: String, content: String, color: Color) {
    viewData.value = SaveNoteViewData(title, content, color)
  }

  private fun closeScreen() {
    closeScreenEvent.value = CLOSE_SCREEN_EVENT
  }

  override fun onCleared() {
    compositeDisposable.clear()
    super.onCleared()
  }

  private data class NoteRelatedData(
      val note: Note,
      val noteColor: Color
  )
}