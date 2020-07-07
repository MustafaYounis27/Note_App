package com.youssef.noteapp.ui.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.youssef.noteapp.R;
import com.youssef.noteapp.data.local.AppDataBase;
import com.youssef.noteapp.models.NoteModel;

import java.util.List;

public class HomeFragment extends Fragment {
    private Context context;
    private View view;
    private RecyclerView recyclerView;
    private Button deleteButton, pinButton, closeButton;
    private LinearLayout editLinear, searchLinear;
    private EditText searchField;
    private ImageView searchIcon, closeIcon;
    private AppDataBase db;
    private FirebaseAuth auth;
    private String uid;
    private DatabaseReference databaseReference;

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
                if(noteModel.getNote_id () == null)
                {
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
        auth=FirebaseAuth.getInstance ();
        uid = auth.getUid ();
        databaseReference= FirebaseDatabase.getInstance ().getReference ();
    }

    private void onSearchClick()
    {
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
                    closeIcon.setVisibility ( View.VISIBLE );
                }else
                    {
                        String word = searchField.getText ().toString ();

                        if(!word.isEmpty ())
                        {
                            completeSearch ( word );
                        }
                    }
            }
        } );

        closeIcon.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                closeIcon.setVisibility ( View.GONE );
                searchField.setVisibility ( View.GONE );
                searchField.setText ( "" );
                new GetData ().execute ();
            }
        } );
    }

    private void completeSearch(String word)
    {
        new getSearchData ().execute ( word );
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
        closeIcon=view.findViewById ( R.id.close_search );
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
        protected List<NoteModel> doInBackground(Void... voids) {
            return db.Dao ().GetAllNotes ();
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
                databaseReference.child ( "notes" ).child ( uid ).child ( noteModels[0].getNote_id () ).removeValue ();
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
            if(!noteModel.getBackground_color().equals("#fff")){
                holder.background.setBackgroundColor (Color.parseColor(noteModel.getBackground_color()));
            }
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
            holder.editNote.setOnClickListener ( new View.OnClickListener () {
                @Override
                public void onClick(View v) {
                    openEditNote(noteModel);
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
           public NotesViewHolder(@NonNull View itemView) {
               super(itemView);
               Title=itemView.findViewById(R.id.note_title);
               Subjec=itemView.findViewById(R.id.note_subject);
               Date=itemView.findViewById(R.id.Date);
               background=itemView.findViewById(R.id.HomeBackGroundColor);
               editNote=itemView.findViewById ( R.id.edit_note );
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
