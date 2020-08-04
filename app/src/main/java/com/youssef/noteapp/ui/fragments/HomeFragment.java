package com.youssef.noteapp.ui.fragments;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.youssef.noteapp.R;
import com.youssef.noteapp.data.local.AppDataBase;
import com.youssef.noteapp.models.NoteModel;
import com.youssef.noteapp.ui.Login.LoginActivity;
import com.youssef.noteapp.ui.main.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String uid;
    private String image;

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

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute ( aVoid );
            Toast.makeText ( context, "222", Toast.LENGTH_SHORT ).show ();
            Toast.makeText ( context, "11111111", Toast.LENGTH_SHORT ).show ();
        }
    }

    private void initFirebase()
    {
        FirebaseAuth auth = FirebaseAuth.getInstance ();
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
                {
                    int i = 0;
                    preparingImagesTOBackup (i);
                }
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
                if(uid != null)
                {
                    getMyNotes();
                }
                else
                {
                    Intent intent = new Intent ( getContext (), LoginActivity.class );
                    intent.putExtra ( "backup", "backup" );
                    startActivity ( intent );
                }
            }
        } );
    }

    private void getMyNotes()
    {
        final List<NoteModel> noteModels = new ArrayList<> (  );
        databaseReference.child ( uid ).addValueEventListener ( new ValueEventListener ()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                for(DataSnapshot dataSnapshot : snapshot.getChildren ())
                {
                    NoteModel model = dataSnapshot.getValue (NoteModel.class);
                    if(model != null)
                    {
                        new Insert ().execute ( model );
                        Toast.makeText ( context, model.getNote_id (), Toast.LENGTH_SHORT ).show ();
                        noteModels.add ( model );
                    }
                }
                preparingImagesToSave ( noteModels,0 );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText ( context, error.getMessage (), Toast.LENGTH_SHORT ).show ();
            }
        } );

    }

    private void preparingImagesToSave(List<NoteModel> noteModel, int item)
    {
        NoteModel model = noteModel.get ( item );
        if(!model.getImageUrl ().isEmpty ()) {

            String[] images = model.getImageUrl ().split ( "#" );
            model.setImageUrl ( "" );
            downloadFile ( 1, images, noteModel, item );
        }else
            {
                if(item < noteModel.size ()-1)
                    preparingImagesToSave ( noteModel,item+1 );
            }

    }

    public void downloadFile(final int item, final String[] images, final List<NoteModel> noteModels, final int noteItem) {
        Toast.makeText ( context, String.valueOf ( item ), Toast.LENGTH_SHORT ).show ();
        final NoteModel model = noteModels.get ( noteItem );
        Toast.makeText ( context, "mosta"+images[item], Toast.LENGTH_SHORT ).show ();
        Picasso.get ()
                .load(images[item])
                .into(new Target () {
                          @Override
                          public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                              try {
                                  String root = Environment.getExternalStorageDirectory().toString();
                                  File myDir = new File(root + "/Note App/Images/");

                                  if (!myDir.exists()) {
                                      myDir.mkdirs();
                                  }

                                  String name = new Date().toString() + ".jpg";
                                  myDir = new File(myDir, name);
                                  FileOutputStream out = new FileOutputStream(myDir);
                                  bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                  Uri imageUri = Uri.parse ( myDir.toString () );
                                  image=image+ "#"+imageUri;
                                  Toast.makeText ( context, image, Toast.LENGTH_SHORT ).show ();
                                  out.flush();
                                  out.close();
                                  if(item < images.length-1)
                                  {
                                      downloadFile ( item+1,images,noteModels,noteItem );
                                  }else
                                      {
                                          Toast.makeText ( context, "youss"+noteItem, Toast.LENGTH_SHORT ).show ();
                                          if(noteItem < noteModels.size ()-1)
                                          {
                                              model.setImageUrl ( image );
                                              new updateNote ().execute ( model );
                                              image = "";
                                              Toast.makeText ( context,"sss"+model.getImageUrl (), Toast.LENGTH_SHORT ).show ();
                                              preparingImagesToSave ( noteModels, noteItem + 1 );
                                          }
                                      }
                              } catch(Exception e){
                                  Toast.makeText ( context, "error", Toast.LENGTH_SHORT ).show ();
                              }
                          }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Toast.makeText ( context, "failed", Toast.LENGTH_SHORT ).show ();
                    }

                          @Override
                          public void onPrepareLoad(Drawable placeHolderDrawable) {
                          }
                      }
                );
    }

    class Insert extends AsyncTask<NoteModel, Void, Void>
    {
        @Override
        protected Void doInBackground(NoteModel... noteModels)
        {
            db.Dao().Insert(noteModels);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute ( aVoid );
            new GetData ().execute ();
        }
    }

    private void preparingImagesTOBackup(int item)
    {
        final NoteModel model = noteModels.get ( item );
        String modelImage = model.getImageUrl ();

        if(!modelImage.isEmpty ()) {
            String[] Images = modelImage.split ( "#" );
            uploadToStorage (Images,1,model,item);
        }else
            uploadNote ( model,item );
    }

    private void uploadToStorage(final String[] images, int i, final NoteModel model, final int item) {
        final int ii=i;
        Uri imageUri = Uri.parse ( images[ i ] );
            Toast.makeText ( context, imageUri + "", Toast.LENGTH_SHORT ).show ();
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
                    Toast.makeText ( getContext (), imageUrl + "yyy", Toast.LENGTH_SHORT ).show ();
                    Toast.makeText ( getContext (), "mmm", Toast.LENGTH_SHORT ).show ();
                    imageList.add ( imageUrl );
                    Toast.makeText ( getContext (), String.valueOf ( imageList.size () ), Toast.LENGTH_SHORT ).show ();
                    if(ii<images.length-1){
                        uploadToStorage (images,ii+1,model,item);
                    }else
                        uploadNote ( model,item );
                }
            } );
        }

    private void uploadNote(final NoteModel noteModel, final int item)
    {
        Toast.makeText ( context, "mostafa", Toast.LENGTH_SHORT ).show ();
        final String noteId;

        if(noteModel.getNote_id () != null)
            noteId = noteModel.getNote_id ();
        else
            noteId = databaseReference.child ( uid ).push ().getKey ();

        if(noteId != null)
        {
            databaseReference.child ( uid ).child ( noteId ).setValue ( noteModel ).addOnCompleteListener ( new OnCompleteListener<Void> ()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful ())
                    {
                        databaseReference.child ( uid ).child ( noteId ).child ( "id" ).removeValue ();
                        databaseReference.child ( uid ).child ( noteId ).child ( "note_id" ).setValue ( noteId );
                        String image = null;
                        for(int i = 0 ; i < imageList.size () ; i++)
                        {
                            image += "#" + imageList.get ( i );
                        }
                        if(image != null)
                        {
                            databaseReference.child ( uid ).child ( noteId ).child ( "imageUrl" ).setValue ( image );
                            imageList.clear ();
                        }
                        databaseReference.child ( uid ).child ( noteId ).child ( "backup_state" ).setValue ( 1 );
                        noteModel.setBackup_state ( 1 );
                        if (item < noteModels.size ()-1)
                            preparingImagesTOBackup ( item+1 );
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
                holder.onlineIcon.setVisibility ( View.VISIBLE );
            if(noteModel.getBackup_state () == 1)
                holder.backupIcon.setVisibility ( View.VISIBLE );
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
                    optionsLinear.setVisibility ( View.GONE );
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
            ImageView onlineIcon, backupIcon, attachmentIcon;
           public NotesViewHolder(@NonNull View itemView) {
               super(itemView);
               Title=itemView.findViewById(R.id.note_title);
               Subjec=itemView.findViewById(R.id.note_subject);
               Date=itemView.findViewById(R.id.Date);
               background=itemView.findViewById(R.id.HomeBackGroundColor);
               editNote=itemView.findViewById ( R.id.edit_note );
               onlineIcon=itemView.findViewById ( R.id.online_state );
               backupIcon=itemView.findViewById ( R.id.backup_state );
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
