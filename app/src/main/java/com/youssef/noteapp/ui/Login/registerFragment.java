package com.youssef.noteapp.ui.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.youssef.noteapp.R;
import com.youssef.noteapp.data.local.AppDataBase;
import com.youssef.noteapp.models.NoteModel;
import com.youssef.noteapp.ui.main.MainActivity;

public class registerFragment extends Fragment
{
    private  View registerFragment;
    private  Toolbar toolbar;
    private EditText usernameField, emailField, passwordField, confirmPasswordField;
    private  Button signUp;
    private  TextView login;
    private NoteModel noteModel;
    private FirebaseAuth auth;
    private  DatabaseReference databaseReference;
    private AppDataBase db;
    private ProgressDialog dialog;
    private String noteId;

    public registerFragment(NoteModel noteModel)
    {
        this.noteModel=noteModel;
    }

    public registerFragment(String noteId)
    {
        this.noteId=noteId;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        registerFragment=inflater.inflate ( R.layout.fragment_register, null );
        return registerFragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated ( savedInstanceState );

        initDialog ();
        initFirebase ();
        initViews();
        initData ();
        onClick();
    }

    private void initDialog()
    {
        dialog = new ProgressDialog (getContext ());
        dialog.setTitle("upload note");
        dialog.setMessage("please waite...");
        dialog.setCancelable(false);
    }

    private void initData() {
        db = Room.databaseBuilder(getContext (), AppDataBase.class, "db").build();
    }

    private void initFirebase()
    {
        auth=FirebaseAuth.getInstance ();
        databaseReference= FirebaseDatabase.getInstance ().getReference ();
    }

    private void onClick()
    {
        signUp.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                String username = usernameField.getText ().toString ();
                String email = emailField.getText ().toString ();
                String password = passwordField.getText ().toString ();
                String confirmPassword = confirmPasswordField.getText ().toString ();

                checkFields(username,email,password,confirmPassword);
            }
        } );

        login.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                FragmentManager fragmentManager = requireActivity ().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Fragment fragment=new LoginFragment (noteModel);
                fragmentTransaction.replace(R.id.frame_login, fragment);
                fragmentTransaction.commit();
            }
        } );

        toolbar.setNavigationOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                onBack ();
            }
        } );
    }

    private void checkFields(String username, String email, String password, String confirmPassword)
    {
        if (username.isEmpty ())
        {
            Toast.makeText ( getContext (), "enter your name", Toast.LENGTH_SHORT ).show ();
            usernameField.requestFocus ();
            return;
        }

        if (email.isEmpty ())
        {
            Toast.makeText ( getContext (), "enter your email", Toast.LENGTH_SHORT ).show ();
            emailField.requestFocus ();
            return;
        }

        if (password.isEmpty ())
        {
            Toast.makeText ( getContext (), "enter your password", Toast.LENGTH_SHORT ).show ();
            passwordField.requestFocus ();
            return;
        }

        if (!confirmPassword.equals ( password ))
        {
            Toast.makeText ( getContext (), "password doesn't match", Toast.LENGTH_SHORT ).show ();
            passwordField.requestFocus ();
            return;
        }

        completeRegister(username,email,password);
    }

    private void completeRegister(final String username, String email, String password)
    {
        auth.createUserWithEmailAndPassword ( email,password ).addOnCompleteListener ( new OnCompleteListener<AuthResult> ()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if(task.isSuccessful () && task.getResult () != null)
                {
                    String uid = task.getResult ().getUser ().getUid ();
                    uploadUserDate ( uid,username );

                    if(noteModel != null)
                        uploadNote ( uid );
                    else
                        exportNote(noteId,uid);
                }
                else
                {
                    Toast.makeText ( getContext (), task.getException ().getMessage (), Toast.LENGTH_SHORT ).show ();
                }
            }
        } );
    }

    private void onBack()
    {
        requireActivity ().onBackPressed ();
    }

    private void uploadUserDate(String uid, String username)
    {
        databaseReference.child ( "users" ).child ( uid ).child ( "username" ).setValue ( username );
    }

    private void uploadNote(final String uid)
    {
        final String noteId;

        if(noteModel.getNote_id () != null)
            noteId = noteModel.getNote_id ();
        else
            noteId = databaseReference.child ( "notes" ).child ( uid ).push ().getKey ();

        if(noteId != null)
        {
            dialog.show ();
            databaseReference.child ( "notes" ).child ( noteId ).setValue ( noteModel ).addOnCompleteListener ( new OnCompleteListener<Void> ()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful ())
                    {
                        databaseReference.child ( "notes" ).child ( noteId ).child ( "id" ).removeValue ();
                        databaseReference.child ( "notes" ).child ( noteId ).child ( "note_id" ).setValue ( noteId );
                        Intent intent = new Intent ( requireActivity (), MainActivity.class );
                        intent.putExtra ( "noteModel",noteModel );
                        intent.putExtra ( "noteId",noteId );
                        requireActivity ().startActivity ( intent );
                        requireActivity ().finish ();
                    }else
                    {
                        Toast.makeText ( getContext (), "12", Toast.LENGTH_SHORT ).show ();
                        dialog.dismiss ();
                    }
                }
            } );
        }
    }

    private void exportNote(final String noteId, String uid)
    {
        databaseReference.child ( "notes" ).child ( noteId ).addValueEventListener ( new ValueEventListener ()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                noteModel = snapshot.getValue (NoteModel.class);
                new Insert ().execute ( noteModel );
                Intent intent = new Intent ( getContext (), MainActivity.class );
                intent.putExtra ( "noteModel",noteModel );
                intent.putExtra ( "noteId",noteId );
                requireActivity ().startActivity ( intent );
                requireActivity ().finish ();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText ( getContext (), error.getMessage (), Toast.LENGTH_SHORT ).show ();
            }
        } );
    }

    class Insert extends AsyncTask<NoteModel, Void, Void>
    {
        @Override
        protected Void doInBackground(NoteModel... noteModels)
        {
            db.Dao().Insert(noteModels);
            return null;
        }
    }

    private void initViews()
    {
        usernameField=registerFragment.findViewById ( R.id.username_field );
        emailField=registerFragment.findViewById ( R.id.email_field );
        passwordField=registerFragment.findViewById ( R.id.password_field );
        confirmPasswordField=registerFragment.findViewById ( R.id.confirm_password_field );
        signUp=registerFragment.findViewById ( R.id.sign_up );
        login=registerFragment.findViewById ( R.id.login );
        toolbar=registerFragment.findViewById ( R.id.toolbarId );
    }
}
