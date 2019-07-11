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
import androidx.room.EmptyResultSetException
import com.raywenderlich.android.notemaker.NoteMakerApplication
import com.raywenderlich.android.notemaker.data.model.Note
import com.raywenderlich.android.notemaker.data.model.Tag
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class SaveNoteViewModel(application: Application) : AndroidViewModel(application) {

  companion object {

    const val INVALID_NOTE_ID = -1L

    val CLOSE_SCREEN_EVENT = Object()

    private const val EMPTY = ""
  }

  val viewData = MutableLiveData<SaveNoteViewData>()
  val closeScreenEvent = MutableLiveData<Any>()

  private val compositeDisposable = CompositeDisposable()
  private val repository = (application as NoteMakerApplication).dependencyInjector.repository

  private var noteId = INVALID_NOTE_ID

  fun fetchNote(noteId: Long) {
    this.noteId = noteId
    compositeDisposable.add(
        repository
            .fetchNote(noteId)
            .flatMap { note -> fetchTagTitle(note.tag).map { mapToViewData(note, it) } }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { viewData.value = it },
                { Log.e("debug_log", "Error while fetching note: $it") }
            )
    )
  }

  private fun fetchTagTitle(tagId: Long) =
      repository
          .fetchTag(tagId)
          .map { it.title }
          .onErrorReturn { EMPTY }

  private fun mapToViewData(note: Note, tagTitle: String) =
      SaveNoteViewData(note.title, tagTitle, note.content)

  fun saveNote(title: String, noteContent: String, tag: String) =
      compositeDisposable.add(
          getTagId(tag)
              .flatMapCompletable { tagId ->

                val note = if (noteId == INVALID_NOTE_ID)
                  Note(title, noteContent, tagId)
                else
                  Note(title, noteContent, tagId, noteId)

                repository.insertNote(note)
              }
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(
                  { closeScreen() },
                  { Log.e("debug_log", "Error while saving note: $it") }
              )
      )

  /**
   * Get Tag id of the existing tag or create a new tag and return new id
   */
  private fun getTagId(tagName: String): Single<Long> {

    if (tagName.isEmpty()) {
      return Single.just(0)
    }

    return repository
        .fetchTagId(tagName)
        .onErrorResumeNext {
          if (it is EmptyResultSetException) {
            repository.addTag(Tag(tagName))
          } else {
            throw it
          }
        }
  }

  fun deleteNote(noteId: Long) {
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

  private fun closeScreen() {
    closeScreenEvent.value = CLOSE_SCREEN_EVENT
  }

  override fun onCleared() {
    compositeDisposable.clear()
    super.onCleared()
  }
}