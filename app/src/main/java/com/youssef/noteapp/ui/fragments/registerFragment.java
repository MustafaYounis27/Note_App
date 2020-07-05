package com.youssef.noteapp.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.youssef.noteapp.R;

public class registerFragment extends Fragment
{
    View registerFragment;
    EditText usernameField, emailField, passwordField, confirmPasswordField;
    Button signUp;
    TextView login;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        registerFragment=inflater.inflate ( R.layout.fragment_register, null );
        return registerFragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated ( savedInstanceState );

        initViews();
        onClick();
    }

    private void onClick()
    {
        signUp.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                String username = usernameField.getText ().toString ();
                String email = emailField.getText ().toString ();
                String password = passwordField.getText ().toString ();
                String confirmPassword = confirmPasswordField.getText ().toString ();

                checkFields(username,email,password,confirmPassword);
            }
        } );
    }

    private void checkFields(String username, String email, String password, String confirmPassword)
    {
        if (username.isEmpty ())
        {
            Toast.makeText ( getContext (), "enter your name", Toast.LENGTH_SHORT ).show ();
            usernameField.requestFocus ();
            return;
        }

        if (email.isEmpty ())
        {
            Toast.makeText ( getContext (), "enter your email", Toast.LENGTH_SHORT ).show ();
            emailField.requestFocus ();
            return;
        }

        if (password.isEmpty ())
        {
            Toast.makeText ( getContext (), "enter your password", Toast.LENGTH_SHORT ).show ();
            passwordField.requestFocus ();
            return;
        }

        if (!confirmPassword.equals ( password ))
        {
            Toast.makeText ( getContext (), "password doesn't match", Toast.LENGTH_SHORT ).show ();
            passwordField.requestFocus ();
            return;
        }

        completeRegister(username,email,password);
    }

    private void completeRegister(String username, String email, String password)
    {

    }

    private void initViews()
    {
        usernameField=registerFragment.findViewById ( R.id.username_field );
        emailField=registerFragment.findViewById ( R.id.email_field );
        passwordField=registerFragment.findViewById ( R.id.password_field );
        confirmPasswordField=registerFragment.findViewById ( R.id.confirm_password_field );
        signUp=registerFragment.findViewById ( R.id.sign_up );
        login=registerFragment.findViewById ( R.id.login );
    }
}
