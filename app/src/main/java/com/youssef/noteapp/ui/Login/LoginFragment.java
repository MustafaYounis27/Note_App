package com.youssef.noteapp.ui.Login;

import android.app.ProgressDialog;
import android.content.Context;
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
import com.youssef.noteapp.Dialogs.CustomLoginClass;
import com.youssef.noteapp.ui.EditNote.EditNoteActivity;
import com.youssef.noteapp.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class LoginFragment extends Fragment
{
    private View loginFragment;
    private Toolbar toolbar;
    private EditText emailField, passwordField;
    private Button login;
    private TextView signUp, forgetPassword;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private NoteModel noteModel;
    private List<String> imageList = new ArrayList<> ();
    private String noteId;
    private ProgressDialog dialog;
    private AppDataBase db;
    Context context;
    int backup;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        context=getContext ();
    }

    public LoginFragment(NoteModel noteModel)
    {
        this.noteModel=noteModel;
    }

    public LoginFragment(int backup)
    {
        this.backup = backup;
    }

    public LoginFragment(String noteId)
    {
        this.noteId=noteId;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        loginFragment = inflater.inflate ( R.layout.fragment_login,null );
        return loginFragment;
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
                dialog.dismiss ();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText ( getContext (), error.getMessage (), Toast.LENGTH_SHORT ).show ();
                dialog.dismiss ();
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

    private void initData() {
        db = Room.databaseBuilder(getContext (), AppDataBase.class, "db").build();
    }

    private void initDialog()
    {
        dialog = new ProgressDialog (context);
        dialog.setMessage("please waite...");
        dialog.setCancelable(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated ( savedInstanceState );
        initDialog ();
        initFirebase ();
        initData ();
        initViews ();
        onClick ();
        FirebaseUser user = auth.getCurrentUser ();
        if(user!=null){
            uploading(user);
        }
        else{
            Toast.makeText ( context, "please complete login", Toast.LENGTH_SHORT ).show ();
        }
    }

    private void uploading(FirebaseUser user)
    {
        String backup = requireActivity ().getIntent ().getStringExtra ( "backup" );
        if (noteModel != null)
            checkPhoto(user);
        else if(backup != null)
        {}
        else
            {
                exportNote ( noteId );
                dialog.show ();
            }
    }

    private void initFirebase()
    {
        auth = FirebaseAuth.getInstance ();
        databaseReference = FirebaseDatabase.getInstance ().getReference ();
        storageReference = FirebaseStorage.getInstance ().getReference ();
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
                if(noteModel != null)
                {
                    FragmentManager fragmentManager = requireActivity ().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    Fragment fragment=new registerFragment (noteModel);
                    fragmentTransaction.replace(R.id.frame_login, fragment);
                    fragmentTransaction.commit();
                }else
                    {
                        FragmentManager fragmentManager = requireActivity ().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        Fragment fragment=new registerFragment (noteId);
                        fragmentTransaction.replace(R.id.frame_login, fragment);
                        fragmentTransaction.commit();
                    }
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
        dialog.show ();
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
                    String backup = requireActivity ().getIntent ().getStringExtra ( "backup" );
                    if(backup != null)
                    {
                        Intent intent = new Intent ( getContext (), MainActivity.class );
                        intent.putExtra ( "backup", backup );
                        startActivity ( intent );
                        requireActivity ().finish ();
                        dialog.dismiss ();
                    }
                    else
                        uploading ( task.getResult ().getUser () );
                }
                else
                    {
                        Toast.makeText ( getContext (), task.getException ().getMessage (), Toast.LENGTH_SHORT ).show ();
                        dialog.dismiss ();
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
            {
                uploadNote ( noteModel, user.getUid () );
            }
        dialog.show ();
    }

    private void uploadToStorage(final String[] images, final int i, final FirebaseUser user) {
        Uri imageUri = Uri.parse ( images[ i ] );
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
                        }
                    dialog.dismiss ();
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

    private void initViews()
    {
        emailField=loginFragment.findViewById ( R.id.email_field );
        passwordField=loginFragment.findViewById ( R.id.password_field );
        forgetPassword=loginFragment.findViewById ( R.id.forget_password );
        forgetPassword.setOnClickListener ( new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                final CustomLoginClass customLoginClass = new CustomLoginClass ( getActivity () );
                customLoginClass.show ();
                customLoginClass.yes.setOnClickListener ( new View.OnClickListener () {
                    @Override
                    public void onClick(View v) {
                        String email = customLoginClass.emailField.getText ().toString ();
                        if(email.isEmpty ()){
                            Toast.makeText ( getContext (), "please enter your email", Toast.LENGTH_SHORT ).show ();
                            customLoginClass.emailField.requestFocus ();
                            return;
                        }
                        resetPassword(email);
                        customLoginClass.dismiss ();
                    }
                } );

            }
        } );
        login=loginFragment.findViewById ( R.id.login );
        signUp=loginFragment.findViewById ( R.id.sign_up );
        toolbar=loginFragment.findViewById ( R.id.toolbarId );

    }

    private void resetPassword(String email)
    {
        auth.sendPasswordResetEmail ( email )
                .addOnCompleteListener ( new OnCompleteListener<Void> () {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful ()){
                            Toast.makeText ( context, "sent, please check your email", Toast.LENGTH_SHORT ).show ();
                        }
                    }
                } );
    }
}
