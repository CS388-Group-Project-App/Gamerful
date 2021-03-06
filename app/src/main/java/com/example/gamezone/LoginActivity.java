package com.example.gamezone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import com.example.gamezone.fragments.LoginFragment;


public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";

    final FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Fragment fragment = new LoginFragment();
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();

    }
}