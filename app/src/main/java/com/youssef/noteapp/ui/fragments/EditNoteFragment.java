package com.youssef.noteapp.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.squareup.picasso.Picasso;
import com.youssef.noteapp.R;
import com.youssef.noteapp.data.local.AppDataBase;
import com.youssef.noteapp.models.NoteModel;
import com.youssef.noteapp.ui.WirteNewNote.WriteNewNoteActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditNoteFragment extends Fragment
{
    private View editNoteFragment;
    private EditText TitleField, SubjectField;
    private Toolbar toolbar;
    private LinearLayout linearLayout, BackGrounLinear, Attachment;
    private View gray, read, blue, green, green2, trqwaz, black;
    private View backgray, backread, backblue, backgreen, backgreen2, backtrqwaz, backblack;
    private String text_color = null, backgroun_color = null;
    private int Storage_Code = 100;
    private Uri image_uri;
    private List<Uri> ImagesUri = new ArrayList<> ();
    public static String SaveImagesString;
    private ProgressDialog dialog;
    private NoteModel noteModel;
    private AppDataBase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        editNoteFragment = inflater.inflate ( R.layout.fragment_edit_note , null );
        noteModel = (NoteModel) getArguments ().getSerializable ( "noteModel" );
        return editNoteFragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated ( savedInstanceState );

        InitViews();
        InitClors();
        InitBackGroundClors();
        InitDialog();
        InitData ();
        OnItemCLick();
        OnBack ();
    }

    private void InitDialog() {
        dialog = new ProgressDialog (getContext ());
        dialog.setTitle("load");
        dialog.setMessage("please waite...");
        dialog.setCancelable(false);
    }

    private void InitData() {
        db = Room.databaseBuilder(getContext (), AppDataBase.class, "db").build();
    }

    private void OnItemCLick() {
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.text_color:
                        linearLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.background_color:
                        BackGrounLinear.setVisibility(View.VISIBLE);
                        break;
                    case R.id.save_as_pdf:
                        String name = TitleField.getText().toString();
                        String subject = SubjectField.getText().toString();
                        CheackPermisstion(name, subject);
                        break;
                    case R.id.upload_picture:
                        Attatchment();
                        break;
                    case R.id.share_note:
                        shareNote();
                        break;
                }
                return false;
            }
        });
    }

    private void OnBack() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = TitleField.getText().toString();
                String subject = SubjectField.getText().toString();
                if (!title.isEmpty() && !subject.isEmpty() && !title.equals ( noteModel.getTitle ()) || !subject.equals ( noteModel.getSubject ()) ) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM hh:mm aa", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());
                    if (text_color == null) {
                        text_color = "#000";
                    }
                    if (backgroun_color == null) {
                        backgroun_color = "#fff";
                    }
                    noteModel.setSubject ( subject );
                    noteModel.setTitle ( title );
                    noteModel.setDate ( currentDateandTime );
                    noteModel.setBackground_color ( backgroun_color );
                    noteModel.setText_color ( text_color );
                    new updateNote ().execute( noteModel );
                    /*for (int i = 0; i < SaveImagesString ; i++) {
                        editor.putString(title + i, SaveImagesString.get(i));
                        editor.commit();
                    }*/
                    // Toast.makeText(getApplicationContext(), "saved", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getApplicationContext(), ImagesUri.size()+"", Toast.LENGTH_SHORT).show();
                }
               requireActivity ().onBackPressed ();
            }
        });
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

    private void shareNote()
    {
        FragmentManager fragmentManager = requireActivity ().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment=new LoginFragment ();
        fragmentTransaction.replace(R.id.Frame, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void InitViews() {
        FloatingActionButton floatingActionButton = getActivity ().findViewById ( R.id.floatingActionButton );
        floatingActionButton.setVisibility ( View.GONE );

        TitleField = editNoteFragment.findViewById(R.id.TitleField);
        TitleField.setText ( noteModel.getTitle () );
        SubjectField = editNoteFragment.findViewById(R.id.SubjectField);
        SubjectField.setText ( noteModel.getSubject () );
        toolbar = editNoteFragment.findViewById(R.id.toolbarId);
        linearLayout = editNoteFragment.findViewById(R.id.LinearColor);
        BackGrounLinear = editNoteFragment.findViewById(R.id.LinearBackgroundColor);
        Attachment = editNoteFragment.findViewById(R.id.Attachments);
        Attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = requireActivity ().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Fragment fragment=new AttachmentFragment();
                fragmentTransaction.replace(R.id.Frame, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    private void InitClors() {
        black = editNoteFragment.findViewById(R.id.black);
        gray = editNoteFragment.findViewById(R.id.gray);
        read = editNoteFragment.findViewById(R.id.read);
        blue = editNoteFragment.findViewById(R.id.blue);
        green = editNoteFragment.findViewById(R.id.green);
        green2 = editNoteFragment.findViewById(R.id.green2);
        trqwaz = editNoteFragment.findViewById(R.id.trqwaz);

        black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setTextColor(requireActivity ().getResources().getColor(R.color.black));
                linearLayout.setVisibility(View.GONE);
                text_color = "#000";
            }
        });

        gray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TitleField.setTextColor(requireActivity ().getResources().getColor(R.color.gray));
                linearLayout.setVisibility(View.GONE);
                text_color = "#696969";
            }
        });

        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setTextColor(requireActivity ().getResources().getColor(R.color.read));
                linearLayout.setVisibility(View.GONE);
                text_color = "#FA0505";
            }
        });
        blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setTextColor(requireActivity ().getResources().getColor(R.color.blue));
                linearLayout.setVisibility(View.GONE);
                text_color = "#3F51B5";
            }
        });

        green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setTextColor(requireActivity ().getResources().getColor(R.color.green));
                linearLayout.setVisibility(View.GONE);
                text_color = "#4CAF50";
            }
        });

        green2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setTextColor(requireActivity ().getResources().getColor(R.color.green2));
                linearLayout.setVisibility(View.GONE);
                text_color = "#CDDC39";
            }
        });

        trqwaz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setTextColor(requireActivity ().getResources().getColor(R.color.trqwaz));
                linearLayout.setVisibility(View.GONE);
                text_color = "#009688";
            }
        });
    }

    private void InitBackGroundClors() {
        backblack = editNoteFragment.findViewById(R.id.backblack);
        backgray = editNoteFragment.findViewById(R.id.backgray);
        backread = editNoteFragment.findViewById(R.id.backread);
        backblue = editNoteFragment.findViewById(R.id.backblue);
        backgreen = editNoteFragment.findViewById(R.id.backgreen);
        backgreen2 = editNoteFragment.findViewById(R.id.backgreen2);
        backtrqwaz = editNoteFragment.findViewById(R.id.backtrqwaz);

        backblack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setBackgroundColor(requireActivity ().getResources().getColor(R.color.black));
                BackGrounLinear.setVisibility(View.GONE);
                backgroun_color = "#000";
            }
        });
        backgray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TitleField.setBackgroundColor(requireActivity ().getResources().getColor(R.color.gray));
                BackGrounLinear.setVisibility(View.GONE);
                backgroun_color = "#696969";
            }
        });

        backread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setBackgroundColor(requireActivity ().getResources().getColor(R.color.read));
                BackGrounLinear.setVisibility(View.GONE);
                backgroun_color = "#FA0505";
            }
        });
        backblue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setBackgroundColor(requireActivity ().getResources().getColor(R.color.blue));
                BackGrounLinear.setVisibility(View.GONE);
                backgroun_color = "#3F51B5";
            }
        });

        backgreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setTextColor(requireActivity ().getResources().getColor(R.color.green));
                BackGrounLinear.setVisibility(View.GONE);
                backgroun_color = "#4CAF50";
            }
        });

        backgreen2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setBackgroundColor(requireActivity ().getResources().getColor(R.color.green2));
                linearLayout.setVisibility(View.GONE);
                text_color = "#CDDC39";
            }
        });

        backtrqwaz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setBackgroundColor(requireActivity ().getResources().getColor(R.color.trqwaz));
                BackGrounLinear.setVisibility(View.GONE);
                backgroun_color = "#009688";
            }
        });
    }

    private void CheackPermisstion(String PdfName, String Subject) {
        //cheack if version above than marshmello version
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //system os > marshmello check if permation is enable or not
            if (ContextCompat.checkSelfPermission( requireContext (), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                //permission not enable
                String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, Storage_Code);
            } else {
                SaveAsPdf(PdfName, Subject);
            }
        } else {
            SaveAsPdf(PdfName, Subject);
        }
    }

    private void SaveAsPdf(String PdfName, String Subject) {
        /*//create object of Document class
        Document document=new Document();
        //pdf file name
        String MFileName=PdfName;
        //pdf File
        String MFilePath= Environment.getExternalStorageDirectory()+"/Note App/pdf/"+MFileName+".pdf";
        try {
            //Create Instance Of Pdf Writer Class
            PdfWriter.getInstance(document,new FileOutputStream(MFilePath));
            //open the Document for writing
            document.open();
            // add author
            document.addAuthor("youssef");
            //add title
            document.addTitle(PdfName);
            //add praghraph
            document.add(new Paragraph(Subject));
            //close document
            document.close();
            Toast.makeText(this, PdfName+".pdf is saved", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }*/
        Document document = new Document();
        //pdf file name
        String MFileName = PdfName;
        //pdf File
        String MFilePath = Environment.getExternalStorageDirectory() + "/Note App/pdf/" + MFileName + ".pdf";
        try {
            BaseFont bf = BaseFont.createFont("res/font/notonaskharabic_regular.ttf",
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font = new Font(bf, 12);
            //Create Instance Of Pdf Writer Class
            PdfWriter.getInstance(document, new FileOutputStream (MFilePath));
            //open the Document for writing
            document.open();
            PdfPTable table = new PdfPTable(1);
            table.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
            //  PdfPCell cell = new PdfPCell();
            // cell.setBorder(Rectangle.NO_BORDER);
            Paragraph p;
            // Chunk chunk = new Chunk(Subject);
            p = new Paragraph("yyyy" + Subject, font);
            //    p.add(chunk);
            //  p.setAlignment(Element.ALIGN_LEFT);
            // cell.addElement(p);
            // table.addCell(cell);
            document.add(p);
            //close document
            document.close();
            Toast.makeText(getContext (), PdfName + ".pdf is saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext (), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Storage_Code) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                String name = TitleField.getText().toString();
                String Subject = SubjectField.getText().toString();
                SaveAsPdf(name, Subject);
            } else {
                Toast.makeText(getContext (), "error permissions...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void Attatchment() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); //allows any image file type. Change * to specific extension to limit it
//**The following line is the important one!
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        Toast.makeText(getContext (), ImagesUri.size()+"", Toast.LENGTH_SHORT).show();
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 505); //SELECT_PICTURES is simply a global int used to check the calling intent in onActivityResult
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 505) {
            if (resultCode == Activity.RESULT_OK) {
                if (Attachment.getVisibility() == View.GONE) {
                    Attachment.setVisibility(View.VISIBLE);
                }
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    int count = clipData.getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
                    for (int i = 0; i < count; i++) {
                        image_uri = clipData.getItemAt(i).getUri();
                        ImagesUri.add(image_uri);
                    }

                } else if (data.getData() != null) {
                    if (Attachment.getVisibility() == View.GONE) {
                        Attachment.setVisibility(View.VISIBLE);
                    }
                    ImagesUri.add(data.getData());
                }
                SaveImageInMyFile(ImagesUri);
            }
        }
    }

    private void SaveImageInMyFile(List<Uri> imagesUri) {
        for (int i = 0; i < imagesUri.size(); i++) {
            Toast.makeText(getContext (), imagesUri.get(i) + "cccc", Toast.LENGTH_SHORT).show();
            getRealPathFromURI(imagesUri.get(i), i);
        }
    }

    private String getRealPathFromURI(Uri contentURI, int i) {
        String result;
        Cursor cursor = requireActivity ().getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();

        }
        String saveto = Environment.getExternalStorageDirectory() + "/Note App/Images/";
        File savetofile = new File(saveto);
        File copyimagefrom = new File(result);
        try {
            exportFile(copyimagefrom, savetofile, i);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private File exportFile(File src, File dst, int i) throws IOException {

        //if folder does not exist
        if (!dst.exists()) {
            if (!dst.mkdir()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat ("yyyyMMdd_HHmmss").format(new Date ());
        File expFile = new File(dst.getPath() + File.separator + "IMG_" + i + timeStamp + ".jpg");
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream (src).getChannel();
            outChannel = new FileOutputStream(expFile).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
        Uri Image = Uri.fromFile(expFile);
        SaveImagesString = Image.toString();
        return expFile;
    }
}
