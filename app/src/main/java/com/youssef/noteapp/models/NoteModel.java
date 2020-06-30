package com.youssef.noteapp.models;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Relation;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "notes_table")
public class NoteModel {
    @PrimaryKey(autoGenerate =true)
    int id;
    @ColumnInfo(name = "Title")
    String Title;
    @ColumnInfo(name = "Subject")
    String Subject;
    @Embedded
    public List<String> ImageUrl;
    @ColumnInfo(name = "VoiceUrl")
    String VoiceUrl;
    @ColumnInfo(name = "Date")
    String Date;
    @ColumnInfo(name = "text_color")
    String text_color;
    @ColumnInfo(name = "background_color")
    String background_color;

    public NoteModel(String Title, String Subject, List<String> ImageUrl, String VoiceUrl, String Date, String text_color, String background_color) {
        this.Title = Title;
        this.Subject = Subject;
        this.ImageUrl = ImageUrl;
        this.VoiceUrl = VoiceUrl;
        this.Date = Date;
        this.text_color = text_color;
        this.background_color = background_color;
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

    public List<String> getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(List<String> imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getVoiceUrl() {
        return VoiceUrl;
    }

    public void setVoiceUrl(String voiceUrl) {
        VoiceUrl = voiceUrl;
    }

}
