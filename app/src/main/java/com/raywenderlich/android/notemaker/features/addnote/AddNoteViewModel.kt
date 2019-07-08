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
package com.raywenderlich.android.notemaker.features.addnote

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.room.EmptyResultSetException
import com.raywenderlich.android.notemaker.NoteMakerApplication
import com.raywenderlich.android.notemaker.data.model.Note
import com.raywenderlich.android.notemaker.data.model.Tag
import com.raywenderlich.android.notemaker.data.repository.Repository
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class AddNoteViewModel(application: Application) : AndroidViewModel(application) {

  companion object {

    val CLOSE_SCREEN_EVENT = Object()
  }

  val closeScreenEvent = MutableLiveData<Any>()

  private val compositeDisposable = CompositeDisposable()

  private val repository: Repository =
    (application as NoteMakerApplication).dependencyInjector.repository

  fun onSaveNoteClicked(title: String, note: String, tag: String) =
    compositeDisposable.add(
      getTagId(tag)
        .flatMapCompletable { tagId -> repository.insertNote(Note(title, note, tagId)) }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          {
            Log.d("debug_log", "Note saved")
            closeScreenEvent.value = CLOSE_SCREEN_EVENT
          },
          { Log.d("debug_log", "Throwable: $it") })
    )

  /**
   * Get Tag id of the existing tag or create a new tag and return new id
   */
  private fun getTagId(tagName: String): Single<Long> =
    repository
      .fetchTagId(tagName)
      .onErrorResumeNext {
        if (it is EmptyResultSetException) {
          repository.addTag(Tag(tagName))
        } else {
          throw it
        }
      }

  override fun onCleared() {
    compositeDisposable.clear()
    super.onCleared()
  }
}