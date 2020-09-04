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
    @Query ( "SELECT * FROM notes_table WHERE note_id = :searchWord")
    NoteModel checkNoteFound(String searchWord);
    @Query ( "UPDATE notes_table SET ImageUrl = :imageUrl WHERE note_id = :noteId" )
    void updateImage(String imageUrl, String noteId);
    @Query ( "UPDATE notes_table SET ImageUrl = :imageUrl WHERE id = :id" )
    void updateImg(String imageUrl, int id);
    @Query ( "UPDATE notes_table SET id = :id WHERE pointer = :pointer" )
    void updateId(int id, String pointer);
    @Query ( "UPDATE notes_table SET pin_state = :pin WHERE id = :id" )
    void updatePin(int pin, int id);
    @Query ( "UPDATE notes_table SET pointer = :pointer WHERE id = :id" )
    void updatePointer(String pointer, int id);
}
