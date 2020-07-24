package com.youssef.noteapp.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.youssef.noteapp.R;
import com.youssef.noteapp.data.local.AppDataBase;
import com.youssef.noteapp.models.NoteModel;
import com.youssef.noteapp.ui.Login.LoginActivity;
import com.youssef.noteapp.ui.main.MainActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private Context context;
    private View view, viewInSearch;
    private RecyclerView recyclerView;
    private Button deleteButton, pinButton, closeButton, joinButton;
    private LinearLayout editLinear, searchLinear, optionsLinear;
    private EditText searchField;
    private ImageView searchIcon, closeSearch, backupIcon, restoreIcon, menuIcon, closeOptions;
    private AppDataBase db;
    private List<NoteModel> noteModels;
    private List<String> imageList = new ArrayList<>();
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String uid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return view=inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onStart()
    {
        super.onStart ();

        InitData();
        initFirebase();
        Intent i = requireActivity ().getIntent ();
        if( i != null)
        {
            NoteModel noteModel = (NoteModel) i.getSerializableExtra ( "noteModel" );
            String noteId = i.getStringExtra ( "noteId" );
            if( noteModel != null && noteId != null)
            {
                noteModel.setOnline_state ( 1 );
                new updateNote ().execute ( noteModel );
                if(noteModel.getNote_id () == null)
                {
                    Toast.makeText ( context, "noteId", Toast.LENGTH_SHORT ).show ();
                    noteModel.setNote_id ( noteId );
                    new updateNote ().execute ( noteModel );
                    openEditNote ( noteModel );
                }
            }
        }

        FloatingActionButton floatingActionButton = requireActivity ().findViewById ( R.id.floatingActionButton );
        floatingActionButton.setVisibility ( View.VISIBLE );
        InitViews();
        InitRecycler();
        new GetData().execute();
        onSearchClick();
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

    private void initFirebase()
    {
        auth = FirebaseAuth.getInstance ();
        uid = auth.getUid ();
        databaseReference = FirebaseDatabase.getInstance ().getReference ();
    }

    private void onSearchClick()
    {
        searchField.addTextChangedListener ( new TextWatcher () {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String word = s.toString ();
                if(joinButton.getVisibility () == View.GONE)
                    new getSearchData ().execute ( word );
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        } );

        searchIcon.setOnClickListener ( new View.OnClickListener ()
        {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v)
            {
                if(searchField.getVisibility () == View.GONE)
                {
                    searchField.setVisibility ( View.VISIBLE );
                    searchField.requestFocus ();
                    closeSearch.setVisibility ( View.VISIBLE );
                    joinButton.setVisibility ( View.GONE );
                    viewInSearch.setVisibility ( View.GONE );
                    searchIcon.setVisibility ( View.GONE );
                }
            }
        } );

        closeSearch.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                closeSearch.setVisibility ( View.GONE );
                searchField.setVisibility ( View.GONE );
                joinButton.setVisibility ( View.VISIBLE );
                viewInSearch.setVisibility ( View.VISIBLE );
                searchIcon.setVisibility ( View.VISIBLE );
                searchField.setText ( "" );
                new GetData ().execute ();
            }
        } );

        joinButton.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                if(searchField.getVisibility () == View.GONE)
                {
                    searchField.setVisibility ( View.VISIBLE );
                    closeSearch.setVisibility ( View.VISIBLE );
                    searchField.setHint ( "enter note id" );
                    searchField.requestFocus ();
                    viewInSearch.setVisibility ( View.GONE );
                    searchIcon.setVisibility ( View.GONE );
                }else
                    {
                    String noteId = searchField.getText ().toString ();

                    exportNoteFromFirebase ( noteId );
                }
            }
        } );

        menuIcon.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                searchLinear.setVisibility ( View.GONE );
                optionsLinear.setVisibility ( View.VISIBLE );
            }
        } );

        closeOptions.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                optionsLinear.setVisibility ( View.GONE );
                searchLinear.setVisibility ( View.VISIBLE );
            }
        } );

        backupIcon.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                if(uid != null)
                    preparingImagesTOBackup ();
                else
                    {
                        Intent intent = new Intent ( getContext (), LoginActivity.class );
                        intent.putExtra ( "backup", "backup" );
                        startActivity ( intent );
                    }
            }
        } );

        restoreIcon.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText ( context, "restore", Toast.LENGTH_SHORT ).show ();
            }
        } );
    }

    private void preparingImagesTOBackup()
    {
        for (int item = 0 ; item < noteModels.size () ; item++)
        {
            final NoteModel model = noteModels.get ( item );
            String modelImage = model.getImageUrl ();

            if(modelImage != null) {
                String[] Images = modelImage.split ( "#" );
                for (int image = 1; image < Images.length; image++) {
                    Uri imageUri = Uri.parse ( Images[ image ] );
                    storageReference = FirebaseStorage.getInstance ().getReference ().child ( "note Image/"+imageUri.getLastPathSegment () );
                    UploadTask uploadTask = storageReference.putFile ( imageUri );
                    Task<Uri> task = uploadTask.continueWithTask ( new Continuation<UploadTask.TaskSnapshot, Task<Uri>> () {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            return storageReference.getDownloadUrl ();
                        }
                    } ).addOnCompleteListener ( new OnCompleteListener<Uri> () {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful ()) {
                                Uri uri = task.getResult ();
                                String imageUrl = uri.toString ();
                                imageList.add ( imageUrl );
                                Toast.makeText ( getContext (), String.valueOf ( imageList.size () ), Toast.LENGTH_SHORT ).show ();
                            } else {
                            }
                        }
                    } );
                }
            }

            uploadNote ( model );
        }
    }

    private void uploadNote(final NoteModel noteModel)
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
                        String image = null;
                        for(int i = 0 ; i < imageList.size () ; i++)
                        {
                            image += "#" + imageList.get ( i );
                        }
                        if(image != null)
                        {
                            databaseReference.child ( "notes" ).child ( noteId ).child ( "imageUrl" ).setValue ( image );
                            imageList.clear ();
                        }
                        noteModel.setNote_id ( noteId );
                        databaseReference.child ( "notes" ).child ( noteId ).child ( "online_state" ).setValue ( 1 );
                        noteModel.setOnline_state ( 1 );
                        new updateNote ().execute ( noteModel );
                        new GetData ().execute ();
                    }else
                    {
                        Toast.makeText ( getContext (), "12", Toast.LENGTH_SHORT ).show ();
                    }
                }
            } );
        }
    }

    private void exportNoteFromFirebase(String noteId)
    {
        new checkNoteFound ().execute ( noteId );
    }

    class checkNoteFound extends AsyncTask<String,Void,NoteModel>
    {
        String noteId;
        @Override
        protected NoteModel doInBackground(String... strings)
        {
            noteId=strings[0];
            return db.Dao ().checkNoteFound ( strings[ 0 ] );
        }

        @Override
        protected void onPostExecute(NoteModel noteModels)
        {
            super.onPostExecute ( noteModels );

            if(noteModels != null)
            {
                Toast.makeText ( context, "note is already found", Toast.LENGTH_SHORT ).show ();
                new getSearchData ().execute ( noteModels.getTitle () );
            }else
                {
                    Intent intent = new Intent ( getContext (), LoginActivity.class );
                    intent.putExtra ( "noteId", noteId );
                    startActivity ( intent );
                    requireActivity ().finish ();
                }
        }
    }

    class getSearchData extends AsyncTask<String,Void,List<NoteModel>>
    {

        @Override
        protected List<NoteModel> doInBackground(String... strings)
        {
            return db.Dao ().search ( strings[ 0 ] );
        }

        @Override
        protected void onPostExecute(List<NoteModel> noteModels) {
            super.onPostExecute ( noteModels );
            RecyclerNotes recyclerNotes=new RecyclerNotes(noteModels);
            recyclerView.setAdapter(recyclerNotes);
        }
    }

    private void onEditClick(final NoteModel noteModel)
    {
        closeButton.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                editLinear.setVisibility ( View.GONE );
                searchLinear.setVisibility ( View.VISIBLE );
            }
        } );

        deleteButton.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                new Delete ().execute ( noteModel );
                new GetData ().execute ();
                editLinear.setVisibility ( View.GONE );
                searchLinear.setVisibility ( View.VISIBLE );
            }
        } );

        pinButton.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {

            }
        } );
    }

    private void InitViews()
    {
        deleteButton=view.findViewById ( R.id.delete );
        pinButton=view.findViewById ( R.id.pin );
        closeButton=view.findViewById ( R.id.close );
        editLinear=view.findViewById ( R.id.edit_lin );
        searchLinear=view.findViewById ( R.id.search_bar );
        searchField=view.findViewById ( R.id.search_field );
        searchIcon=view.findViewById ( R.id.search_icon );
        closeSearch=view.findViewById ( R.id.close_search );
        joinButton=view.findViewById ( R.id.join );
        viewInSearch=view.findViewById ( R.id.view_in_search );
        backupIcon=view.findViewById ( R.id.backup_icon );
        restoreIcon=view.findViewById ( R.id.restore_icon );
        menuIcon=view.findViewById ( R.id.menu_icon );
        closeOptions=view.findViewById ( R.id.close_options );
        optionsLinear=view.findViewById ( R.id.options_bar );
    }

    private void InitData() {
        db= Room.databaseBuilder(context, AppDataBase.class,"db").build();
    }

    private void InitRecycler() {
        recyclerView=view.findViewById(R.id.RecyclerView);
    }

    class GetData extends AsyncTask<Void,Void, List<NoteModel>>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<NoteModel> doInBackground(Void... voids)
        {
            noteModels = db.Dao ().GetAllNotes ();
            return noteModels;
        }

        @Override
        protected void onPostExecute(List<NoteModel> noteModels) {
            super.onPostExecute(noteModels);
            RecyclerNotes recyclerNotes=new RecyclerNotes(noteModels);
            recyclerView.setAdapter(recyclerNotes);
        }
    }

    class Delete extends AsyncTask<NoteModel,Void,Void>
    {
        @Override
        protected Void doInBackground(NoteModel... noteModels) {
            db.Dao ().Delete (noteModels[0]);
            if(noteModels[0].getNote_id () != null)
                databaseReference.child ( "notes" ).child ( noteModels[0].getNote_id () ).removeValue ();
            return null;
        }
    }

    class RecyclerNotes extends RecyclerView.Adapter<RecyclerNotes.NotesViewHolder> {
        List<NoteModel> noteModels;

        public RecyclerNotes(List<NoteModel> noteModels) {
            this.noteModels = noteModels;
        }

        @NonNull
        @Override
        public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View vie = LayoutInflater.from(context).inflate(R.layout.notes_items, parent, false);
            return new NotesViewHolder(vie);
        }

        @Override
        public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
            final NoteModel noteModel=noteModels.get(position);
            holder.Title.setText(noteModel.getTitle());
            holder.Subjec.setText(noteModel.getSubject());
            holder.Date.setText(noteModel.getDate());
            if(noteModel.getOnline_state () == 1)
                holder.onlineState.setVisibility ( View.VISIBLE );
            if(!noteModel.getImageUrl ().isEmpty ())
                holder.attachmentIcon.setVisibility ( View.VISIBLE );
            if(!noteModel.getBackground_color().equals("#fff")){
                holder.background.setBackgroundColor (Color.parseColor(noteModel.getBackground_color()));
            }
            holder.editNote.setOnClickListener ( new View.OnClickListener () {
                @Override
                public void onClick(View v) {
                    openEditNote(noteModel);
                }
            } );

            holder.editNote.setOnLongClickListener ( new View.OnLongClickListener ()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    editLinear.setVisibility ( View.VISIBLE );
                    searchLinear.setVisibility ( View.GONE );
                    onEditClick (noteModel);
                    return false;
                }
            } );
        }

        @Override
        public int getItemCount() {
            return noteModels.size();
        }


        class NotesViewHolder extends RecyclerView.ViewHolder{
            TextView Title,Subjec,Date;
            View background;
            LinearLayout editNote;
            ImageView onlineState, attachmentIcon;
           public NotesViewHolder(@NonNull View itemView) {
               super(itemView);
               Title=itemView.findViewById(R.id.note_title);
               Subjec=itemView.findViewById(R.id.note_subject);
               Date=itemView.findViewById(R.id.Date);
               background=itemView.findViewById(R.id.HomeBackGroundColor);
               editNote=itemView.findViewById ( R.id.edit_note );
               onlineState=itemView.findViewById ( R.id.online_state );
               attachmentIcon=itemView.findViewById ( R.id.attachment_icon );
           }
       }
    }

    private void openEditNote(NoteModel noteModel)
    {
        EditNoteFragment editNoteFragment = new EditNoteFragment (noteModel);
        FragmentManager fragmentManager = requireActivity ().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.Frame, editNoteFragment );
        fragmentTransaction.addToBackStack ( null );
        fragmentTransaction.commit();
    }

}
