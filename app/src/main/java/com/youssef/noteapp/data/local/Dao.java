package com.youssef.noteapp.data.local;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.youssef.noteapp.models.NoteModel;

import java.util.List;

@androidx.room.Dao
public interface Dao {
    @Query("Select * from notes_table")
    List<NoteModel> GetAllNotes();
    @Query("SELECT * FROM notes_table WHERE id = :id")
    NoteModel GetOneNotes(int id);
    @Insert
    void Insert(NoteModel...noteModels);
    @Delete
    void Delete(NoteModel noteModel);
    @Update
    void UpdateNote(NoteModel noteModels);
    @Query ( "SELECT * FROM notes_table WHERE Title LIKE '%' || :searchWord || '%'" )
    List<NoteModel> search(String searchWord);
}
