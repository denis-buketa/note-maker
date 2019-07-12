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

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.raywenderlich.android.notemaker.NoteMakerApplication
import com.raywenderlich.android.notemaker.data.model.Color
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * ViewModel for "Notes" screen.
 */
class NotesOverviewViewModel(application: Application) : AndroidViewModel(application) {

  val notes = MutableLiveData<List<NoteOverviewItemData>>()

  private val compositeDisposable = CompositeDisposable()
  private val repository = (application as NoteMakerApplication).dependencyInjector.repository

  fun fetchNotes() {
    compositeDisposable.add(
        // Get all notes from database
        repository
            .getAllNotes()
            .flatMapObservable { Observable.fromIterable(it) }
            .flatMapSingle { note ->

              // For each note fetch its color
              repository
                  .findColorById(note.colorId)
                  .onErrorReturn { Color.DEFAULT_COLOR }
                  .map {

                    // Map note and color into NoteOverviewItemData
                    NoteOverviewItemData(note, it)
                  }
            }
            .toList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { notes.value = it },
                {
                  Log.e(NotesOverviewViewModel::class.java.name,
                      "Error occurred while fetching notes: $it"
                  )
                }
            )
    )
  }

  override fun onCleared() {
    compositeDisposable.clear()
    super.onCleared()
  }
}