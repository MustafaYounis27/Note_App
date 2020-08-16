package com.youssef.noteapp.ui.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
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

import com.google.android.gms.tasks.Continuation;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.youssef.noteapp.R;
import com.youssef.noteapp.data.local.AppDataBase;
import com.youssef.noteapp.models.NoteModel;
import com.youssef.noteapp.ui.EditNote.EditNoteActivity;
import com.youssef.noteapp.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class registerFragment extends Fragment
{
    private View registerFragment;
    private Toolbar toolbar;
    private EditText usernameField, emailField, passwordField, confirmPasswordField;
    private Button signUp;
    private TextView login;
    private NoteModel noteModel;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private List<String> imageList = new ArrayList<> ();
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
                    String backup = requireActivity ().getIntent ().getStringExtra ( "backup" );
                    if(backup != null)
                        onBack ();
                    else
                    {
                        if(noteModel != null)
                            checkPhoto ( task.getResult ().getUser () );
                        else
                            exportNote(noteId);
                    }
                }
                else
                {
                    Toast.makeText ( getContext (), task.getException ().getMessage (), Toast.LENGTH_SHORT ).show ();
                }
            }
        } );
    }

    private void checkPhoto(FirebaseUser user)
    {
        String modelImage = noteModel.getImageUrl ();

        if(!modelImage.isEmpty ()) {
            String[] Images = modelImage.split ( "#" );
            uploadToStorage (Images,1,user);
        }else
            uploadNote ( noteModel,user.getUid () );
    }

    private void uploadToStorage(final String[] images, final int i, final FirebaseUser user) {
        Uri imageUri = Uri.parse ( images[ i ] );
        dialog.show ();
        storageReference = FirebaseStorage.getInstance ().getReference ().child ( "note Image/" ).child ( imageUri.getLastPathSegment () );
        UploadTask uploadTask = storageReference.putFile ( imageUri );
        Task<Uri> task = uploadTask.continueWithTask ( new Continuation<UploadTask.TaskSnapshot, Task<Uri>> () {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                return storageReference.getDownloadUrl ();
            }
        } ).addOnCompleteListener ( new OnCompleteListener<Uri> () {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                String imageUrl = task.getResult ().toString ();
                imageList.add ( imageUrl );
                if(i<images.length-1){
                    uploadToStorage (images,i+1,user);
                }else
                    uploadNote ( noteModel,user.getUid () );
            }
        } );
    }

    private void uploadNote(final NoteModel noteModel, final String uid)
    {
        final String noteId;

        if(noteModel.getNote_id () != null)
            noteId = noteModel.getNote_id ();
        else
            noteId = databaseReference.child ( "notes" ).child ( uid ).push ().getKey ();

        if(noteId != null)
        {
            databaseReference.child ( "notes" ).child ( noteId ).setValue ( noteModel ).addOnCompleteListener ( new OnCompleteListener<Void> ()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful ())
                    {
                        databaseReference.child ( "notes" ).child ( noteId ).child ( "id" ).removeValue ();
                        databaseReference.child ( "notes" ).child ( noteId ).child ( "note_id" ).setValue ( noteId );
                        if(imageList.size () != 0) {
                            String image = null;
                            for (int i = 0; i < imageList.size (); i++) {
                                image += "#" + imageList.get ( i );
                            }
                            databaseReference.child ( "notes" ).child ( noteId ).child ( "imageUrl" ).setValue ( image );
                            imageList.clear ();
                        }
                        databaseReference.child ( "notes" ).child ( noteId ).child ( "online_state" ).setValue ( 1 );
                        noteModel.setNote_id ( noteId );
                        noteModel.setOnline_state ( 1 );
                        new updateNote ().execute ( noteModel );
                        Intent intent = new Intent ( getContext (), EditNoteActivity.class );
                        intent.putExtra ( "noteModel", noteModel );
                        startActivity ( intent );
                        requireActivity ().finish ();
                    }else
                    {
                        Toast.makeText ( getContext (), task.getException ().getMessage (), Toast.LENGTH_SHORT ).show ();
                        dialog.dismiss ();
                    }
                }
            } );
        }
    }

    private void onBack()
    {
        requireActivity ().onBackPressed ();
        requireActivity ().finish ();
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
                        noteModel.setNote_id ( noteId );
                        noteModel.setOnline_state ( 1 );
                        new updateNote ().execute ( noteModel );
                        Intent intent = new Intent ( getContext (), EditNoteActivity.class );
                        intent.putExtra ( "noteModel", noteModel );
                        startActivity ( intent );
                        requireActivity ().finish ();
                    }else
                    {
                        Toast.makeText ( getContext (), task.getException ().getMessage (), Toast.LENGTH_SHORT ).show ();
                        dialog.dismiss ();
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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute ( aVoid );
        }
    }

    private void exportNote(final String noteId)
    {
        databaseReference.child ( "notes" ).child ( noteId ).addValueEventListener ( new ValueEventListener ()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                noteModel = snapshot.getValue (NoteModel.class);
                Intent intent = new Intent ( getContext (), MainActivity.class );
                if(noteModel != null)
                {
                    new Insert ().execute ( noteModel );
                    intent.putExtra ( "export", noteModel );
                }
                else
                    Toast.makeText ( getContext (), "id not found", Toast.LENGTH_SHORT ).show ();
                startActivity ( intent );
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
