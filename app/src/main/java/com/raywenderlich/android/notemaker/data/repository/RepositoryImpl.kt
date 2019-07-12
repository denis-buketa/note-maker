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
package com.raywenderlich.android.notemaker.data.repository

import android.annotation.SuppressLint
import android.util.Log
import com.raywenderlich.android.notemaker.data.database.ColorDao
import com.raywenderlich.android.notemaker.data.database.NoteDao
import com.raywenderlich.android.notemaker.data.model.Color
import com.raywenderlich.android.notemaker.data.model.Note
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

/**
 * {@inheritDoc}
 */
class RepositoryImpl(private val noteDao: NoteDao, private val colorDao: ColorDao) : Repository {

  init {
    prepopulateDatabase()
  }

  /**
   * Populates database with colors if it is empty.
   */
  @SuppressLint("CheckResult")
  private fun prepopulateDatabase() {

    val colors = Color.DEFAULT_COLORS.toTypedArray()

    colorDao
        .getAll()
        .flatMapCompletable {

          if (it.isEmpty()) {
            // Populate database since it is empty.
            colorDao.insertAll(*colors)
          } else {
            // Do nothing. Database already contains colors.
            Completable.complete()
          }
        }
        .subscribeOn(Schedulers.io())
        .subscribe(
            { /* Colors inserted successfully */ },
            { Log.e(Repository::class.java.name, "Error occurred while inserting colors: $it") }
        )

  }

  override fun getAllNotes(): Single<List<Note>> = noteDao.getAll()

  override fun findNoteById(id: Long): Single<Note> = noteDao.findById(id)

  override fun insertNote(note: Note): Completable = noteDao.insert(note)

  override fun deleteNote(id: Long): Completable = noteDao.delete(id)

  override fun getAllColors(): Single<List<Color>> = colorDao.getAll()

  override fun findColorById(id: Long): Single<Color> = colorDao.findById(id)
}