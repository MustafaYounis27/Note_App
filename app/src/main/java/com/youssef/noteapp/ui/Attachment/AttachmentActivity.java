package com.youssef.noteapp.ui.Attachment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.youssef.noteapp.R;
import com.youssef.noteapp.data.local.AppDataBase;
import com.youssef.noteapp.models.NoteModel;
import com.youssef.noteapp.ui.CustomDialogClass;
import com.youssef.noteapp.ui.fragments.HomeFragment;

import java.util.ArrayList;
import java.util.List;

public class AttachmentActivity extends AppCompatActivity
{
    private List<String> imageList = new ArrayList<> ();

    @Override
    protected void onStart()
    {
        super.onStart ();

        String images = getIntent ().getStringExtra ( "imageUri" );
        if(images != null)
        {
            String[] imageArray = images.split ( "#" );
            for (int i = 1; i < imageArray.length; i++) {
                imageList.add ( imageArray[ i ] );
            }
        }else
            {
                Toast.makeText ( this, "Attachment is empty", Toast.LENGTH_SHORT ).show ();
            }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_attachment );

        initRecycler ();

    }

    private void initRecycler()
    {
        RecyclerView recyclerView=findViewById(R.id.AttachmentRecycler);
        recyclerView.addItemDecoration(new DividerItemDecoration (this,DividerItemDecoration.VERTICAL));
        ImagesAdapter imagesAdapter=new ImagesAdapter (imageList);
        recyclerView.setAdapter(imagesAdapter);
    }
    class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImagesHolder>
    {
        List<String> Images;

        public ImagesAdapter(List<String> Images) {
            this.Images = Images;
        }

        @NonNull
        @Override
        public ImagesAdapter.ImagesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(getApplicationContext ()).inflate(R.layout.attachment_item, parent, false);
            return new ImagesAdapter.ImagesHolder (view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImagesAdapter.ImagesHolder holder, final int position)
        {
            final String ImageUri=Images.get(position);

            Picasso.get().load(ImageUri).into(holder.imageView);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return Images.size();
        }


        class ImagesHolder extends RecyclerView.ViewHolder
        {
            ImageView imageView;
            public ImagesHolder(@NonNull View itemView)
            {
                super(itemView);
                imageView=itemView.findViewById(R.id.ImagesAttacment);
            }
        }
    }
}