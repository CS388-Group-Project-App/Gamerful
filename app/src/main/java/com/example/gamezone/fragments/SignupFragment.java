package com.example.gamezone.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.gamezone.MainActivity;
import com.example.gamezone.R;
import com.example.gamezone.models.User;
import com.example.gamezone.progressButtonSubmit.ProgressButtonLogin;
import com.example.gamezone.progressButtonSubmit.ProgressButtonSignup;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SignupFragment extends Fragment {

    private final String TAG = "SignupFragment";
    private static final int RESULT_LOAD_IMG = 402;
    private ImageView ivAddPicture;
    private EditText etAddUsername;
    private EditText etAddPassword;
    private EditText etConfirmPassword;
    private RadioButton radioButton;
    private Button backButton;

    private Bitmap selectedImage;

    private String username;
    private String role;
    private String password;
    private String confirmPassword;

    RadioGroup radioGroup;

    private File photoFile;
    private String photoFileName = "photo.jpg";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivAddPicture = view.findViewById(R.id.ivAddPicture);
        etAddUsername = view.findViewById(R.id.etAddUsername);
        etAddPassword = view.findViewById(R.id.etAddPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        radioGroup = view.findViewById(R.id.radioGroup);
        backButton = view.findViewById(R.id.backBtn);

        final FragmentManager fragmentManager = ((AppCompatActivity) getContext()).getSupportFragmentManager();

        ProgressButtonSignup progressButton = new ProgressButtonSignup(getContext(), view);

        Glide.with(getContext())
                .load(R.drawable.ic_default_figure)
                .into(ivAddPicture);

        ivAddPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked on image");
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager.popBackStack();
            }
        });

        view.findViewById(R.id.progressBtnSignup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = etAddUsername.getText().toString();
                password = etAddPassword.getText().toString();
                confirmPassword = etConfirmPassword.getText().toString();

                // get selected radio button from radioGroup
                int selectedId = radioGroup.getCheckedRadioButtonId();
                radioButton = view.findViewById(selectedId);

                switch (radioButton.getText().toString()) {
                    case "Yes":
                        role = "Parent";
                        break;

                    case "No":
                        role = "Child";
                        break;
                }

                if(username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill all the information", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!password.equals(confirmPassword)) {
                    Toast.makeText(getContext(), "Your password does not match", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressButton.buttonActivated();
                addUser(username, role, password, progressButton);
            }
        });

    }

    private void addUser(String username, String role, String password, ProgressButtonSignup progressButton) {

        final ParseFile file = new ParseFile(photoFile);

        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    User user = new User();
                    user.setUsername(username);
                    user.setRole(role);
                    user.setPassword(password);
                    user.setProfileImage(file);
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Toast.makeText(getContext(), "Error while signing up!", Toast.LENGTH_SHORT).show();
                                progressButton.buttonReset();
                            }
                            goMainActivity();
                            progressButton.buttonReset();
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK && reqCode == RESULT_LOAD_IMG) {
            Log.d(TAG, "Result Code Inside: " + reqCode);
            try {
                final Uri imageUri = data.getData();
                Log.d(TAG, imageUri.toString());

                final InputStream imageStream = ((AppCompatActivity) getContext()).getContentResolver().openInputStream(imageUri);
                selectedImage = BitmapFactory.decodeStream(imageStream);

                photoFile = getPhotoFileUri(photoFileName);
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

                Glide.with(getContext())
                        .asBitmap()
                        .load(takenImage)
                        .centerCrop()
                        .circleCrop()
                        .into(ivAddPicture);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(getContext(), "You haven't picked Image",Toast.LENGTH_LONG).show();
        }
    }

    public File getPhotoFileUri(String fileName) throws IOException {
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        File f = new File(mediaStorageDir.getPath(), fileName);
        f.createNewFile();

        //Convert bitmap to byte array
        Bitmap bitmap = selectedImage;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    private void goMainActivity() {
        Intent i = new Intent(getContext(), MainActivity.class);
        startActivity(i);
        ((AppCompatActivity) getContext()).finish();
    }
}