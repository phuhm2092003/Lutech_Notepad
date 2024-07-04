package lutech.intern.noteapp.data.repository

import androidx.lifecycle.LiveData
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.NoteWithCategories
import lutech.intern.noteapp.database.dao.NoteDao

class NoteRepository(private val noteDao: NoteDao) {
    fun fetchNotes(): LiveData<List<Note>> {
        return noteDao.fetchAllNotes()
    }

    fun fetchNoteWithCategories(): LiveData<List<NoteWithCategories>> {
        return noteDao.getNoteWithCategories()
    }

    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    suspend fun update(note: Note) {
        noteDao.update(note)
    }

    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }
}