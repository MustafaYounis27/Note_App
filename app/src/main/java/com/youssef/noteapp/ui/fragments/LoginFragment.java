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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.youssef.noteapp.R;

public class LoginFragment extends Fragment
{
    View loginFragment;
    EditText emailField, passwordField;
    Button login;
    TextView signUp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        loginFragment = inflater.inflate ( R.layout.fragment_login,null );
        return loginFragment;
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
        login.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                String email = emailField.getText ().toString ();
                String password = passwordField.getText ().toString ();

                checkFields(email,password);

            }
        } );

        signUp.setOnClickListener ( new View.OnClickListener ()
        {
            @Override
            public void onClick(View v)
            {
                FragmentManager fragmentManager = requireActivity ().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Fragment fragment=new registerFragment ();
                fragmentTransaction.replace(R.id.Frame, fragment);
                fragmentTransaction.commit();
            }
        } );
    }

    private void checkFields(String email, String password)
    {
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

        completeLogin(email,password);
    }

    private void completeLogin(String email, String password)
    {

    }

    private void initViews()
    {
        emailField=loginFragment.findViewById ( R.id.email_field );
        passwordField=loginFragment.findViewById ( R.id.password_field );
        login=loginFragment.findViewById ( R.id.login );
        signUp=loginFragment.findViewById ( R.id.sign_up );
    }
}
