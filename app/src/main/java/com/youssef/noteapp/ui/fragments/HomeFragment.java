package com.youssef.noteapp.ui.fragments;

import android.app.ProgressDialog;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Environment;
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
import com.youssef.noteapp.R;
import com.youssef.noteapp.data.local.AppDataBase;
import com.youssef.noteapp.models.NoteModel;
import com.youssef.noteapp.ui.CustomDialogClass;
import com.youssef.noteapp.ui.EditNote.EditNoteActivity;
import com.youssef.noteapp.ui.Login.LoginActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {
    private Context context;
    private View view, viewInSearch;
    private RecyclerView recyclerView;
    private RecyclerNotes recyclerNotes;
    private Button deleteButton, pinButton, closeButton, joinButton;
    private LinearLayout editLinear, searchLinear, optionsLinear;
    private EditText searchField;
    private ImageView searchIcon, closeSearch, backupIcon, restoreIcon, menuIcon, closeOptions;
    private AppDataBase db;
    private List<NoteModel> noteModels;
    private List<String> imageList = new ArrayList<>();
    private List<NoteModel> restoreNotes = new ArrayList<> ();
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String uid;
    private String[] images;
    String imageUri;
    int c;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return view=inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onStart()
    {
        super.onStart ();
        NoteModel noteModel = (NoteModel) requireActivity ().getIntent ().getSerializableExtra ( "export" );
        if(noteModel != null)
        {
            preparingImagesToSave ( noteModel );
            requireActivity ().getIntent ().removeExtra ( "export" );
        }
        InitData();
        initFirebase();

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
                    preparingImagesTOBackup (0);
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
                    intent.putExtra ( "backup", "restore" );
                    startActivity ( intent );
                }
            }
        } );
    }

    class checkRestoreNote extends AsyncTask<NoteModel,Void,NoteModel>
    {
        NoteModel noteModel;
        @Override
        protected NoteModel doInBackground(NoteModel... noteModels)
        {
            noteModel = noteModels[0];
            return db.Dao ().checkNoteFound ( noteModel.getNote_id () );
        }

        @Override
        protected void onPostExecute(NoteModel noteModels)
        {
            super.onPostExecute ( noteModels );

            if(noteModels == null)
            {
                new Insert ().execute ( noteModel );
                restoreNotes.add ( noteModel );
                if(c == 1)
                    preparingImagesToSave ( restoreNotes.get ( 0 ) );
                c = 0;
            }
        }
    }

    private void preparingImagesToSave(NoteModel noteModel)
    {
        if(restoreNotes.size () != 0)
            restoreNotes.remove ( 0 );
        if(!noteModel.getImageUrl ().isEmpty ())
        {
            images = noteModel.getImageUrl ().split ( "#" );

            new DownloadFile (1).execute ( images[1],noteModel.getNote_id ());
        }else
            {
                if(restoreNotes.size () != 0)
                    preparingImagesToSave ( restoreNotes.get ( 0 ) );
            }
    }

    class DownloadFile extends AsyncTask<String,Integer,String> {
        int a;

        public DownloadFile(int a) {
            this.a = a;
        }

        ProgressDialog mProgressDialog = new ProgressDialog(getContext ());// Change Mainactivity.this with your activity name.

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage("Downloading");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.show();
        }
        @Override
        protected String doInBackground(String... aurl) {
            int count;
            String id = null;
            try {
                URL url = new URL(aurl[0]);
                id = aurl[1];
                URLConnection conexion = url.openConnection();
                conexion.connect();
                String name = new Date().toString() + ".jpg";

                int lenghtOfFile = conexion.getContentLength();
                String PATH = Environment.getExternalStorageDirectory()+"/Note App/Images/";
                File folder = new File(PATH);
                if(!folder.exists()){
                    folder.mkdir();//If there is no folder it will be created.
                }
                InputStream input = new BufferedInputStream (url.openStream());
                OutputStream output = new FileOutputStream(PATH+name);
                Uri Imageuri=Uri.fromFile (new File(PATH+name));
                imageUri += "#"+Imageuri;
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress ((int)(total*100/lenghtOfFile));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {}
            return id;
        }
        protected void onProgressUpdate(Integer... progress) {
            mProgressDialog.setProgress(progress[0]);
            if(mProgressDialog.getProgress()==mProgressDialog.getMax()){
                mProgressDialog.dismiss();
            }
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute ( string );
            if(a<images.length-1){
                new DownloadFile (a+1).execute ( images[a+1],string );
            }else
                {
                    new updateImage ().execute ( imageUri,string );
                    Toast.makeText(getContext (), "File Downloaded", Toast.LENGTH_SHORT).show();
                    if(restoreNotes.size () != 0)
                        preparingImagesToSave ( restoreNotes.get ( 0 ) );
                    imageUri="";
                }
        }
    }

    class updateImage extends AsyncTask<String,Void,Void>
    {
        @Override
        protected Void doInBackground(String... strings)
        {
            db.Dao ().updateImage ( strings[0],strings[1] );
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute ( aVoid );
        }
    }

    private void getMyNotes()
    {
        c = 1;
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
                        new checkRestoreNote ().execute ( model );
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Toast.makeText ( context, error.getMessage (), Toast.LENGTH_SHORT ).show ();
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
                    if(ii<images.length-1){
                        uploadToStorage (images,ii+1,model,item);
                    }else
                        uploadNote ( model,item );
                }
            } );
        }

    private void uploadNote(final NoteModel noteModel, final int item)
    {
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
                        Toast.makeText ( context, task.getException ().getMessage (), Toast.LENGTH_SHORT ).show ();
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

    private void onEditClick(final NoteModel noteModel, final View selectedItem)
    {
        closeButton.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                editLinear.setVisibility ( View.GONE );
                searchLinear.setVisibility ( View.VISIBLE );
                selectedItem.setVisibility ( View.GONE );
            }
        } );

        deleteButton.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                final CustomDialogClass cdd=new CustomDialogClass (getActivity ());
                cdd.show();
                cdd.yes.setOnClickListener ( new View.OnClickListener ()
                {
                    @Override
                    public void onClick(View v)
                    {
                        new Delete ().execute ( noteModel );
                        new GetData ().execute ();
                        editLinear.setVisibility ( View.GONE );
                        searchLinear.setVisibility ( View.VISIBLE );
                        cdd.dismiss ();
                    }
                } );

                cdd.no.setOnClickListener ( new View.OnClickListener ()
                {
                    @Override
                    public void onClick(View v)
                    {
                        editLinear.setVisibility ( View.GONE );
                        searchLinear.setVisibility ( View.VISIBLE );
                        cdd.dismiss ();
                    }
                } );
                selectedItem.setVisibility ( View.GONE );
            }
        } );

        pinButton.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                if(noteModel.getPin_state () == 0) {
                    noteModel.setPin_state ( 1 );
                    new updatePin ().execute ( 1, noteModel.getId () );
                    noteModel.setPointer ( noteModel.getTitle () + noteModel.getId () );
                    new updatePointer ( noteModel.getId () ).execute ( noteModel.getTitle () + noteModel.getId () );
                    for (int i = noteModels.size () - 1; i >= 0; i--) {
                        NoteModel model = noteModels.get ( i );
                        model.setPointer ( model.getTitle () + model.getId () );
                        new updatePointer ( model.getId () ).execute ( model.getTitle () + model.getId () );
                        model.setId ( model.getId () + 1 );
                        new updateId ( model.getPointer () ).execute ( model.getId () );

                        if (i == 0) {
                            noteModel.setId ( 1 );
                            new updateId ( noteModel.getPointer () ).execute ( 1 );
                        }
                    }
                }else{
                    noteModel.setPin_state ( 0 );
                    new updatePin ().execute ( 0,noteModel.getId () );
                }
                editLinear.setVisibility ( View.GONE );
                searchLinear.setVisibility ( View.VISIBLE );
                selectedItem.setVisibility ( View.GONE );
                new GetData ().execute (  );
            }
        } );
    }

    class updatePointer extends AsyncTask<String,Void,Void>
    {
        int id;

        public updatePointer(int id) {
            this.id = id;
        }

        @Override
        protected Void doInBackground(String... strings)
        {
            db.Dao ().updatePointer ( strings[0],id );
            return null;
        }
    }

    class updateId extends AsyncTask<Integer,Void,Void>
    {
        String pointer;
        public updateId(String pointer){
            this.pointer=pointer;
        }

        @Override
        protected Void doInBackground(Integer... integers)
        {
            db.Dao ().updateId ( integers[0],pointer );
            return null;
        }
    }

    class updatePin extends AsyncTask<Integer,Void,Void>
    {
        @Override
        protected Void doInBackground(Integer... integers)
        {
            db.Dao ().updatePin ( integers[0],integers[1] );
            return null;
        }
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
            recyclerNotes = new RecyclerNotes(noteModels);
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
        public void onBindViewHolder(@NonNull final NotesViewHolder holder, final int position)
        {
            final NoteModel noteModel=noteModels.get(position);
            holder.Title.setText(noteModel.getTitle());
            holder.Subjec.setText(noteModel.getSubject());
            holder.Date.setText(noteModel.getDate());
            if(noteModel.getOnline_state () == 1)
                holder.onlineIcon.setVisibility ( View.VISIBLE );
            if(noteModel.getBackup_state () == 1)
                holder.backupIcon.setVisibility ( View.VISIBLE );
            if(noteModel.getPin_state () == 1)
                holder.pinIcon.setVisibility ( View.VISIBLE );
            if(!noteModel.getImageUrl ().isEmpty ())
                holder.attachmentIcon.setVisibility ( View.VISIBLE );
            if(!noteModel.getBackground_color().equals("#fff")){
                holder.background.setBackgroundColor (Color.parseColor(noteModel.getBackground_color()));
            }
            holder.editNote.setOnClickListener ( new View.OnClickListener () {
                @Override
                public void onClick(View v) {
                    if(holder.selectedItem.getVisibility () == View.VISIBLE)
                    {
                        holder.selectedItem.setVisibility ( View.GONE );
                        editLinear.setVisibility ( View.GONE );
                        searchLinear.setVisibility ( View.VISIBLE );
                    }else
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
                    holder.selectedItem.setVisibility ( View.VISIBLE );
                    if(noteModel.getPin_state () == 1)
                        pinButton.setText ( "unpin" );
                    else
                        pinButton.setText ( "pin" );
                    onEditClick (noteModel,holder.selectedItem);
                    return true;
                }
            } );
        }

        @Override
        public int getItemCount() {
            return noteModels.size();
        }


        class NotesViewHolder extends RecyclerView.ViewHolder{
            TextView Title,Subjec,Date;
            View background, selectedItem;
            LinearLayout editNote;
            ImageView onlineIcon, backupIcon, attachmentIcon, pinIcon;
           public NotesViewHolder(@NonNull View itemView) {
               super(itemView);
               Title=itemView.findViewById(R.id.note_title);
               Subjec=itemView.findViewById(R.id.note_subject);
               Date=itemView.findViewById(R.id.Date);
               background=itemView.findViewById(R.id.HomeBackGroundColor);
               selectedItem=itemView.findViewById ( R.id.selected_item );
               editNote=itemView.findViewById ( R.id.edit_note );
               onlineIcon=itemView.findViewById ( R.id.online_state );
               backupIcon=itemView.findViewById ( R.id.backup_state );
               attachmentIcon=itemView.findViewById ( R.id.attachment_icon );
               pinIcon=itemView.findViewById ( R.id.pin_state );
           }
       }
    }

    private void openEditNote(NoteModel noteModel)
    {
        Intent intent = new Intent ( getContext (), EditNoteActivity.class );
        intent.putExtra ( "noteModel", noteModel );
        startActivity ( intent );
    }

}
