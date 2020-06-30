package com.youssef.noteapp.ui.WirteNewNote;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.youssef.noteapp.R;
import com.youssef.noteapp.data.local.AppDataBase;
import com.youssef.noteapp.models.NoteModel;
import com.youssef.noteapp.ui.fragments.AttachmentFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WriteNewNoteActivity extends AppCompatActivity {
    private EditText TitleField, SubjectField;
    private AppDataBase db;
    private ProgressDialog dialog;
    private Toolbar toolbar;
    private NoteModel noteModel;
    private View gray, read, blue, green, green2, trqwaz, black;
    private View backgray, backread, backblue, backgreen, backgreen2, backtrqwaz, backblack;
    private LinearLayout linearLayout, BackGrounLinear, Attachment;
    private String text_color = null, backgroun_color = null;
    private int Storage_Code = 100;
    private Uri image_uri;
    private List<Uri> ImagesUri = new ArrayList<>();
    public static List<String> SaveImagesString = new ArrayList<>();
    public static final String FONT = "res/font/notonaskharabic_regular.ttf";
    public static final String ARABIC = "\u0627\u0644\u0633\u0639\u0631 \u0627\u0644\u0627\u062c\u0645\u0627\u0644\u064a";
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_new_note);
        IntialSharedPreferences();
        InitViews();
        InitClors();
        InitBackGroundClors();
        InitData();
        InitDialog();
        OnItemCLick();
        OnBack();
    }

    private void InitDialog() {
        dialog = new ProgressDialog(this);
        dialog.setTitle("load");
        dialog.setMessage("please waite...");
        dialog.setCancelable(false);
    }

    private void InitData() {
        db = Room.databaseBuilder(getApplicationContext(), AppDataBase.class, "db").build();
    }

    private void InitViews() {
        TitleField = findViewById(R.id.TitleField);
        SubjectField = findViewById(R.id.SubjectField);
        toolbar = findViewById(R.id.toolbarId);
        linearLayout = findViewById(R.id.LinearColor);
        BackGrounLinear = findViewById(R.id.LinearBackgroundColor);
        Attachment = findViewById(R.id.Attachments);
        Attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Fragment fragment=new AttachmentFragment();
                fragmentTransaction.replace(R.id.ree, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    class GetData extends AsyncTask<Void, Void, NoteModel> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected NoteModel doInBackground(Void... voids) {
            noteModel = db.Dao().GetOneNotes(1);
            return noteModel;
        }

        @Override
        protected void onPostExecute(NoteModel noteModel) {
            super.onPostExecute(noteModel);
            TitleField.setText(noteModel.getTitle());
            SubjectField.setText(noteModel.getSubject());
            dialog.dismiss();
        }
    }

    class Insert extends AsyncTask<NoteModel, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(NoteModel... noteModels) {
            db.Dao().Insert(noteModels);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            onBackPressed();
        }
    }

    private void OnItemCLick() {
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.NewNoteChangeTextColor:
                        linearLayout.setVisibility(View.VISIBLE);
                        break;
                    case R.id.NewNoteChangeBackGroundColor:
                        BackGrounLinear.setVisibility(View.VISIBLE);
                        break;
                    case R.id.NewNoteSaveAsPdf:
                        String name = TitleField.getText().toString();
                        String subject = SubjectField.getText().toString();
                        CheackPermisstion(name, subject);
                        break;
                    case R.id.NewNoteAttachment:
                        Attatchment();
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
                if (!title.isEmpty() && !subject.isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM hh:mm aa", Locale.getDefault());
                    String currentDateandTime = sdf.format(new Date());
                    if (text_color == null) {
                        text_color = "#000";
                    }
                    if (backgroun_color == null) {
                        backgroun_color = "#fff";
                    }
                    if (SaveImagesString.size() != 0) {
                        NoteModel noteModel = new NoteModel(title, subject, SaveImagesString, "", currentDateandTime, text_color, backgroun_color);
                    } else {
                        NoteModel noteModel = new NoteModel(title, subject, SaveImagesString, "", currentDateandTime, text_color, backgroun_color);
                    }
                    new Insert().execute(noteModel);
                    for (int i = 0; i < SaveImagesString.size(); i++) {
                        editor.putString(title + i, SaveImagesString.get(i));
                        editor.commit();
                    }
                    Toast.makeText(getApplicationContext(), "saved", Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), ImagesUri.size()+"", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    @Override
    public void onBackPressed() {
        String title = TitleField.getText().toString();
        String subject = SubjectField.getText().toString();
        if (!title.isEmpty() && !subject.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM hh:mm aa", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());
            if (text_color == null) {
                text_color = "#000";
            }
            if (backgroun_color == null) {
                backgroun_color = "#fff";
            }
            if (SaveImagesString.size() != 0) {
                NoteModel noteModel = new NoteModel(title, subject, SaveImagesString, "", currentDateandTime, text_color, backgroun_color);
            } else {
                NoteModel noteModel = new NoteModel(title, subject, SaveImagesString, "", currentDateandTime, text_color, backgroun_color);
            }
            new Insert().execute(noteModel);
            for (int i = 0; i < SaveImagesString.size(); i++) {
                editor.putString(title + i, SaveImagesString.get(i));
                editor.commit();
            }
            Toast.makeText(getApplicationContext(), "saved2", Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), ImagesUri.size()+"", Toast.LENGTH_SHORT).show();
        }
        super.onBackPressed();
    }

    private void InitClors() {
        black = findViewById(R.id.black);
        gray = findViewById(R.id.gray);
        read = findViewById(R.id.read);
        blue = findViewById(R.id.blue);
        green = findViewById(R.id.green);
        green2 = findViewById(R.id.green2);
        trqwaz = findViewById(R.id.trqwaz);

        black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setTextColor(getApplicationContext().getResources().getColor(R.color.black));
                linearLayout.setVisibility(View.GONE);
                text_color = "#000";
            }
        });

        gray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TitleField.setTextColor(getApplicationContext().getResources().getColor(R.color.gray));
                linearLayout.setVisibility(View.GONE);
                text_color = "#696969";
            }
        });

        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setTextColor(getApplicationContext().getResources().getColor(R.color.read));
                linearLayout.setVisibility(View.GONE);
                text_color = "#FA0505";
            }
        });
        blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setTextColor(getApplicationContext().getResources().getColor(R.color.blue));
                linearLayout.setVisibility(View.GONE);
                text_color = "#3F51B5";
            }
        });

        green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setTextColor(getApplicationContext().getResources().getColor(R.color.green));
                linearLayout.setVisibility(View.GONE);
                text_color = "#4CAF50";
            }
        });

        green2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setTextColor(getApplicationContext().getResources().getColor(R.color.green2));
                linearLayout.setVisibility(View.GONE);
                text_color = "#CDDC39";
            }
        });

        trqwaz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setTextColor(getApplicationContext().getResources().getColor(R.color.trqwaz));
                linearLayout.setVisibility(View.GONE);
                text_color = "#009688";
            }
        });
    }

    private void InitBackGroundClors() {
        backblack = findViewById(R.id.backblack);
        backgray = findViewById(R.id.backgray);
        backread = findViewById(R.id.backread);
        backblue = findViewById(R.id.backblue);
        backgreen = findViewById(R.id.backgreen);
        backgreen2 = findViewById(R.id.backgreen2);
        backtrqwaz = findViewById(R.id.backtrqwaz);

        backblack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.black));
                BackGrounLinear.setVisibility(View.GONE);
                backgroun_color = "#000";
            }
        });
        backgray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TitleField.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.gray));
                BackGrounLinear.setVisibility(View.GONE);
                backgroun_color = "#696969";
            }
        });

        backread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.read));
                BackGrounLinear.setVisibility(View.GONE);
                backgroun_color = "#FA0505";
            }
        });
        backblue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.blue));
                BackGrounLinear.setVisibility(View.GONE);
                backgroun_color = "#3F51B5";
            }
        });

        backgreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setTextColor(getApplicationContext().getResources().getColor(R.color.green));
                BackGrounLinear.setVisibility(View.GONE);
                backgroun_color = "#4CAF50";
            }
        });

        backgreen2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.green2));
                linearLayout.setVisibility(View.GONE);
                text_color = "#CDDC39";
            }
        });

        backtrqwaz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SubjectField.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.trqwaz));
                BackGrounLinear.setVisibility(View.GONE);
                backgroun_color = "#009688";
            }
        });
    }

    private void CheackPermisstion(String PdfName, String Subject) {
        //cheack if version above than marshmello version
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //system os > marshmello check if permation is enable or not
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
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
            PdfWriter.getInstance(document, new FileOutputStream(MFilePath));
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
            Toast.makeText(this, PdfName + ".pdf is saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "error permissions...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void Attatchment() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); //allows any image file type. Change * to specific extension to limit it
//**The following line is the important one!
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        Toast.makeText(this, ImagesUri.size()+"", Toast.LENGTH_SHORT).show();
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 505); //SELECT_PICTURES is simply a global int used to check the calling intent in onActivityResult
    }

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
            Toast.makeText(this, imagesUri.get(i) + "cccc", Toast.LENGTH_SHORT).show();
            getRealPathFromURI(imagesUri.get(i), i);
        }
    }

    private String getRealPathFromURI(Uri contentURI, int i) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
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

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File expFile = new File(dst.getPath() + File.separator + "IMG_" + i + timeStamp + ".jpg");
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(src).getChannel();
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
        SaveImagesString.add(Image.toString());
        return expFile;
    }

 /*   @Override
    protected void onPause() {
        String title = TitleField.getText().toString();
        String subject = SubjectField.getText().toString();
        if (!title.isEmpty() && !subject.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM hh:mm aa", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());
            if (text_color == null) {
                text_color = "#000";
            }
            if (backgroun_color == null) {
                backgroun_color = "#fff";
            }
            if (SaveImagesString.size() != 0) {
                NoteModel noteModel = new NoteModel(title, subject, "1", "", currentDateandTime, text_color, backgroun_color);
            } else {
                NoteModel noteModel = new NoteModel(title, subject, "", "", currentDateandTime, text_color, backgroun_color);
            }
            new Insert().execute(noteModel);
            for (int i = 0; i < SaveImagesString.size(); i++) {
                editor.putString(title + i, SaveImagesString.get(i));
                editor.commit();
            }
            Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();
        }
        super.onPause();
    }*/

    private void IntialSharedPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();
    }
    /* public void createPdf(String dest) throws IOException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();
        Font f = FontFactory.getFont(FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        PdfPTable table = new PdfPTable(1);
        Phrase phrase = new Phrase();
        Chunk chunk = new Chunk("يوسف هشام رررررررررر-test value");
        phrase.add(chunk);
        phrase.add(new Chunk(ARABIC, f));
        PdfPCell cell = new PdfPCell(phrase);
        cell.setUseDescender(true);
        cell.setRunDirection(PdfWriter.RUN_DIRECTION_RTL);
        table.addCell(cell);
        document.add(table);
        document.close();
    }*/
}
