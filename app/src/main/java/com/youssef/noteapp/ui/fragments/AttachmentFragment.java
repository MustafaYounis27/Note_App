package com.youssef.noteapp.ui.fragments;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.youssef.noteapp.R;
import com.youssef.noteapp.models.NoteModel;
import com.youssef.noteapp.ui.WirteNewNote.WriteNewNoteActivity;

import java.util.List;

public class AttachmentFragment extends Fragment {
    private View view;
    private Context context;
    private List<String> Images;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return view=inflater.inflate(R.layout.fragment_attachment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //InitRecycler();
    }

    private void InitRecycler(){
        RecyclerView recyclerView=view.findViewById(R.id.AttachmentRecycler);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));
        //Images= WriteNewNoteActivity.SaveImagesString;
        ImagesAdapter imagesAdapter=new ImagesAdapter(Images);
        recyclerView.setAdapter(imagesAdapter);
    }
    class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImagesHolder> {
        List<String> Images;

        public ImagesAdapter(List<String> Images) {
            this.Images = Images;
        }

        @NonNull
        @Override
        public ImagesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View vie = LayoutInflater.from(context).inflate(R.layout.attachment_item, parent, false);
            return new ImagesHolder(vie);
        }

        @Override
        public void onBindViewHolder(@NonNull ImagesHolder holder, final int position) {
            String ImageUri=Images.get(position);
            Picasso.get().load(ImageUri).into(holder.imageView);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                //    WriteNewNoteActivity.SaveImagesString.remove(position);
                    Toast.makeText(getContext(), "deleted", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return Images.size();
        }


        class ImagesHolder extends RecyclerView.ViewHolder{
            ImageView imageView;
            public ImagesHolder(@NonNull View itemView) {
                super(itemView);
               imageView=itemView.findViewById(R.id.ImagesAttacment);
            }
        }
    }

}
