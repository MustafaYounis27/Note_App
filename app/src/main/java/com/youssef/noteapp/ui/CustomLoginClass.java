package com.youssef.noteapp.ui;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.youssef.noteapp.R;

public class CustomLoginClass extends Dialog implements android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button yes, no;
    public EditText emailField;

    public CustomLoginClass(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature( Window.FEATURE_NO_TITLE);
        setContentView( R.layout.custom_login_view);
        emailField = findViewById ( R.id.email_field );
        yes =  findViewById(R.id.btn_yes);
        no =  findViewById(R.id.btn_no);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                c.finish();
                break;
            case R.id.btn_no:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}
