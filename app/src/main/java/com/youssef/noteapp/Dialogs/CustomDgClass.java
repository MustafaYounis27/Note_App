package com.youssef.noteapp.Dialogs;

import android.app.Activity;
        import android.app.Dialog;
        import android.os.Bundle;
        import android.view.View;
        import android.view.Window;
        import android.widget.Button;

        import com.youssef.noteapp.R;

public class CustomDgClass extends Dialog implements android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button ok;

    public CustomDgClass(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature( Window.FEATURE_NO_TITLE);
        setContentView( R.layout.custom_dlg);
        ok =  findViewById(R.id.btn_ok);
        ok.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId () == R.id.btn_ok) {
            c.finish ();
        }
        dismiss();
    }
}
