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
import com.youssef.noteapp.ui.main.MainActivity;

public class LoginActivity extends AppCompatActivity
{
    NoteModel noteModel;
    String noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_login );

        noteId = getIntent ().getStringExtra ( "noteId" );
        noteModel = (NoteModel) getIntent ().getSerializableExtra ( "noteModel" );
        if(noteModel != null)
            replaceFragment ( new LoginFragment (noteModel) );
        else
            replaceFragment ( new LoginFragment (noteId) );
    }

    void replaceFragment(Fragment fragment)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_login, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed ();

        if(noteModel != null)
        {
            Intent intent = new Intent ( getApplicationContext (), MainActivity.class );
            intent.putExtra ( "noteModel",noteModel );
            startActivity ( intent );
            finish ();
        }else
            {
                Intent intent = new Intent ( getApplicationContext (), MainActivity.class );
                startActivity ( intent );
                finish ();
            }
    }
}