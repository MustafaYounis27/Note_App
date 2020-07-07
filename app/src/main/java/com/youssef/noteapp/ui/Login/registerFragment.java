package com.youssef.noteapp.ui.Login;

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
    View registerFragment;
    Toolbar toolbar;
    EditText usernameField, emailField, passwordField, confirmPasswordField;
    Button signUp;
    TextView login;
    NoteModel noteModel;
    FirebaseAuth auth;
    DatabaseReference databaseReference;
    AppDataBase db;

    public registerFragment(NoteModel noteModel)
    {
        this.noteModel=noteModel;
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

        initFirebase ();
        initViews();
        initData ();
        onClick();
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
                    checkNoteFound ( uid );
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

    private void checkNoteFound(final String uid)
    {
        databaseReference.child ( "notes" ).child ( uid ).addValueEventListener ( new ValueEventListener ()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for(DataSnapshot dataSnapshot : snapshot.getChildren ())
                {
                    NoteModel noteModels = dataSnapshot.getValue (NoteModel.class);
                    if(noteModels != null)
                    {
                        if(noteModels.getNote_id ().equals ( noteModel.getNote_id () ))
                        {
                            onBack ();
                        }
                    }
                }
                uploadNote ( uid );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText ( getContext (), error.getMessage (), Toast.LENGTH_SHORT ).show ();
            }
        } );
    }

    private void uploadNote(String uid)
    {
        final String noteId = databaseReference.child ( "notes" ).child ( uid ).push ().getKey ();

        if(noteId != null)
        {
            databaseReference.child ( "notes" ).child ( uid ).child ( noteId ).setValue ( noteModel ).addOnCompleteListener ( new OnCompleteListener<Void> ()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful ())
                    {
                        Intent intent = new Intent ( requireActivity (), MainActivity.class );
                        intent.putExtra ( "noteId",noteId );
                        intent.putExtra ( "noteModel",noteModel );
                        requireActivity ().startActivity ( intent );
                        requireActivity ().finish ();
                    }
                }
            } );
        }
    }

    class updateNote extends AsyncTask<NoteModel,Void,Void>
    {
        @Override
        protected Void doInBackground(NoteModel... noteModels)
        {
            db.Dao ().UpdateNote ( noteModels[0] );
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
