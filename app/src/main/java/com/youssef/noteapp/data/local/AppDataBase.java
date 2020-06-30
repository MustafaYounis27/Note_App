package com.youssef.noteapp.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.youssef.noteapp.models.NoteModel;

@Database(entities = {NoteModel.class},version = 6,exportSchema =false)
public abstract class AppDataBase extends RoomDatabase {
    public abstract Dao Dao();
}
