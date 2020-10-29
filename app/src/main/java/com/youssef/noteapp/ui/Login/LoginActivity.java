package com.youssef.noteapp.ui.Login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.youssef.noteapp.R;
import com.youssef.noteapp.models.NoteModel;
import com.youssef.noteapp.ui.EditNote.EditNoteActivity;
import com.youssef.noteapp.ui.main.MainActivity;

import java.util.List;

public class LoginActivity extends AppCompatActivity
{
    NoteModel noteModel;
    String noteId,update;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_login );

        noteId = getIntent ().getStringExtra ( "noteId" );
        noteModel = (NoteModel) getIntent ().getSerializableExtra ( "noteModel" );
        String backup = getIntent ().getStringExtra ( "backup" );
        update = getIntent ().getStringExtra ( "update" );

        if(update != null)
            replaceFragment ( new LoginFragment (  ) );
        if(backup != null)
        {
            replaceFragment ( new LoginFragment ( 1 ) );
        }else {
            if (noteModel != null)
                replaceFragment ( new LoginFragment ( noteModel ) );
            else
                replaceFragment ( new LoginFragment ( noteId ) );
        }
    }

    void replaceFragment(Fragment fragment)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_login, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed ();
        NoteModel noteModel = (NoteModel) getIntent ().getSerializableExtra ( "noteModel" );

        if(noteModel != null)
            startActivity ( new Intent ( getApplicationContext (), EditNoteActivity.class ) );
        if(update != null){}
        else
            startActivity ( new Intent ( getApplicationContext (), MainActivity.class ) );
        finish ();
    }
}