package com.youssef.noteapp.models;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.Relation;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "notes_table")
public class NoteModel implements Serializable {
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo(name = "pointer")
    String pointer;
    @ColumnInfo(name = "Title")
    String Title;
    @ColumnInfo(name = "Subject")
    String Subject;
    @ColumnInfo
    String ImageUrl;
    @ColumnInfo(name = "VoiceUrl")
    String VoiceUrl;
    @ColumnInfo(name = "Date")
    String Date;
    @ColumnInfo(name = "text_color")
    String text_color;
    @ColumnInfo(name = "background_color")
    String background_color;
    @ColumnInfo(name = "note_id")
    String note_id;
    @ColumnInfo(name = "online_state")
    int online_state;
    @ColumnInfo(name = "backup_state")
    int backup_state;
    @ColumnInfo(name = "pin_state")
    int pin_state;

    public NoteModel(String Title, String Subject, String ImageUrl, String VoiceUrl, String Date, String text_color, String background_color) {
        this.Title = Title;
        this.Subject = Subject;
        this.ImageUrl = ImageUrl;
        this.VoiceUrl = VoiceUrl;
        this.Date = Date;
        this.text_color = text_color;
        this.background_color = background_color;
    }

    public NoteModel()
    {

    }

    public String getText_color() {
        return text_color;
    }

    public void setText_color(String text_color) {
        this.text_color = text_color;
    }

    public String getBackground_color() {
        return background_color;
    }

    public void setBackground_color(String background_color) {
        this.background_color = background_color;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getVoiceUrl() {
        return VoiceUrl;
    }

    public void setVoiceUrl(String voiceUrl) {
        VoiceUrl = voiceUrl;
    }

    public String getNote_id(){
        return note_id;
    }

    public void setNote_id(String note_id){
        this.note_id = note_id;
    }

    public int getOnline_state() {
        return online_state;
    }

    public void setOnline_state(int online_state) {
        this.online_state = online_state;
    }

    public int getBackup_state() {
        return backup_state;
    }

    public void setBackup_state(int backup_state) {
        this.backup_state = backup_state;
    }

    public int getPin_state() {
        return pin_state;
    }

    public void setPin_state(int pin_state) {
        this.pin_state = pin_state;
    }

    public void setPointer(String pointer){
        this.pointer=pointer;
    }

    public String getPointer(){
        return pointer;
    }
}
