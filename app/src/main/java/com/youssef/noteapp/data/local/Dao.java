package com.youssef.noteapp.data.local;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

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
    @Query("DELETE FROM notes_table")
    void Delete();
    @Query("UPDATE notes_table SET Title=:title WHERE id=:id")
    void UpdateTitle(String title,int id);
    @Query("UPDATE notes_table SET Subject=:subject WHERE id=:id")
    void UpdateSubject(String subject,int id);
}
