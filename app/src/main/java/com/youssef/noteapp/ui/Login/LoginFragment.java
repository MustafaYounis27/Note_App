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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.youssef.noteapp.R;
import com.youssef.noteapp.data.local.AppDataBase;
import com.youssef.noteapp.models.NoteModel;
import com.youssef.noteapp.ui.main.MainActivity;

public class LoginFragment extends Fragment
{
    private View loginFragment;
    private Toolbar toolbar;
    private EditText emailField, passwordField;
    private Button login;
    private TextView signUp;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private NoteModel noteModel;
    private ProgressDialog dialog;

    public LoginFragment(NoteModel noteModel)
    {
        this.noteModel=noteModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        loginFragment = inflater.inflate ( R.layout.fragment_login,null );
        return loginFragment;
    }

    @Override
    public void onStart()
    {
        super.onStart ();

        initDialog ();

        FirebaseUser user = auth.getCurrentUser ();
        if(user != null)
        {
            uploadNote ( user.getUid () );
        }else
            {
                Toast.makeText ( getContext (), "please login or sign up", Toast.LENGTH_SHORT ).show ();
            }

    }

    private void initDialog()
    {
        dialog = new ProgressDialog (getContext ());
        dialog.setTitle("upload note");
        dialog.setMessage("please waite...");
        dialog.setCancelable(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated ( savedInstanceState );

        initFirebase();
        initViews();
        onClick();
    }

    private void initFirebase()
    {
        auth=FirebaseAuth.getInstance ();
        databaseReference= FirebaseDatabase.getInstance ().getReference ();
    }

    private void onClick()
    {
        login.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                String email = emailField.getText ().toString ();
                String password = passwordField.getText ().toString ();

                checkFields(email,password);
            }
        } );

        signUp.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                FragmentManager fragmentManager = requireActivity ().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Fragment fragment=new registerFragment (noteModel);
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

    private void onBack()
    {

        requireActivity ().onBackPressed ();
        requireActivity ().finish ();
    }

    private void checkFields(String email, String password)
    {
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

        completeLogin(email,password);
    }

    private void completeLogin(String email, String password)
    {
        auth.signInWithEmailAndPassword ( email,password ).addOnCompleteListener ( new OnCompleteListener<AuthResult> ()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if(task.isSuccessful () && task.getResult ()!=null)
                {
                    String uid = task.getResult ().getUser ().getUid ();
                    uploadNote ( uid );
                }
                else
                    {
                        Toast.makeText ( getContext (), task.getException ().getMessage (), Toast.LENGTH_SHORT ).show ();
                    }
            }
        } );
    }

    private void uploadNote(String uid)
    {
        final String noteId;

        if(noteModel.getNote_id () != null)
            noteId = noteModel.getNote_id ();
        else
            noteId = databaseReference.child ( "notes" ).child ( uid ).push ().getKey ();

        if(noteId != null)
        {
            dialog.show ();
            databaseReference.child ( "notes" ).child ( uid ).child ( noteId ).setValue ( noteModel ).addOnCompleteListener ( new OnCompleteListener<Void> ()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful ())
                    {
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

    private void initViews()
    {
        emailField=loginFragment.findViewById ( R.id.email_field );
        passwordField=loginFragment.findViewById ( R.id.password_field );
        login=loginFragment.findViewById ( R.id.login );
        signUp=loginFragment.findViewById ( R.id.sign_up );
        toolbar=loginFragment.findViewById ( R.id.toolbarId );
    }
}
