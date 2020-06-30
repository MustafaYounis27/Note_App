package com.youssef.noteapp.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.youssef.noteapp.R;
import com.youssef.noteapp.ui.WirteNewNote.WriteNewNoteActivity;
import com.youssef.noteapp.ui.fragments.HomeFragment;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CreateFolderInStorage();
        IniFloatingButton();
        ReplaceFraagment(new HomeFragment());
    }

    private void CreateFolderInStorage() {
        File folder = new File(Environment.getExternalStorageDirectory()+ "/Note App");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File pdfFolder = new File(folder+"/pdf");
        if (!pdfFolder.exists()) {
            pdfFolder.mkdirs();
        }
        File ImaagesFolder = new File(folder+"/Images");
        if (!ImaagesFolder.exists()) {
            ImaagesFolder.mkdirs();
        }
    }

    private void IniFloatingButton() {
        floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(), WriteNewNoteActivity.class);
                startActivity(intent);
            }
        });
    }

    void ReplaceFraagment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.Frame, fragment);
        fragmentTransaction.commit();
    }

}
