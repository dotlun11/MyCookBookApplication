package com.example.kaildyhoang.mycookbookapplication;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import java.text.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.firebase.client.Firebase;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText _etxtName, _etxtUserName, _etxtPassword, _etxtBirthDay, _etxtEmail, _etxtAddress, _etxtDescription;
    private RadioGroup _rdoGrGender;
    private RadioButton _rdoBtnMale, _rdoBtnFemale, _rdoBtnOthers;
    private ImageView _imgVGetImage;
    private String _imgUrlReturn = null; //image url be returned after uploaded onto firebase
    private String _genderReturn = null;
    private RadioButton radioButton;
    private Calendar calendar;
    private String birthday, _dateNow;

    public static final int READ_EXTERNAL_STORAGE = 0;
    private static final int GALLERY_INTENT = 2;
    private ProgressDialog progressDialog;
    private Firebase mFirebase;
    private Uri mImgUri = null;
    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageRef;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "SignUpActivityWithEMAI";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //        Initialize the progressDialog
        progressDialog = new ProgressDialog(this);

        Firebase.setAndroidContext(this);

        //        Initialize Firebase Database paths for database and storage
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        //        Push will create new child every time we upload
        mFirebase = new Firebase("https://cookbookapplication-396d8.firebaseio.com/").push();
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://cookbookapplication-396d8.appspot.com");

        //Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        _etxtName = (EditText) findViewById(R.id.editTextInputNameSU);
        _etxtUserName = (EditText) findViewById(R.id.editTextInputUserNameSU);
        _etxtPassword = (EditText) findViewById(R.id.editTextInputPasswordSU);
        _etxtBirthDay = (EditText) findViewById(R.id.editTextInputBirthDaySU);
        _etxtEmail = (EditText) findViewById(R.id.editTextInputEmailSU);
        _etxtAddress = (EditText) findViewById(R.id.editTextInputAddressSU);
        _etxtDescription = (EditText) findViewById(R.id.editTextInputDescriptionSU);

        _imgVGetImage = (ImageView) findViewById(R.id.imageViewGetImageSU);

        findViewById(R.id.buttonSignUpSU).setOnClickListener(this);
        findViewById(R.id.editTextInputBirthDaySU).setOnClickListener(this);

        _imgVGetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //                Check for runtime permission
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG,"Call for Permission");

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
                    }
                }else {
                    callGallery();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.editTextInputBirthDaySU){
            showDatePickerDialog();
        }else if(i == R.id.radioGroupGenderSU){

        }else if(i == R.id.buttonSignUpSU){
            doCreateAccountAuth();
        }
    }

    private void doCreateAccountAuth() {
//        Toast.makeText(SignUpActivity.this, email+password, Toast.LENGTH_SHORT).show();
        final String email = _etxtEmail.getText().toString();
        final String password = _etxtPassword.getText().toString();
        Log.d(TAG, "doCreateAccount: " + email);

        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        //[Start create account with emai]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "CreateUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            doCreateUser(user.getUid());
                            Toast.makeText(SignUpActivity.this, "Welcome to CookBook, Sign In!" + user.getEmail() + user.getUid(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplication(),SignInActivity.class));
                        } else {
                            Log.w(TAG, "CreateUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "CreateUserWithEmail failed.", Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void doCreateUser(String uid){
        _dateNow = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())+"";
        User user = new User(
                _etxtName.getText().toString(),
                _etxtEmail.getText().toString(),
                _etxtPassword.getText().toString(),
                _etxtAddress.getText().toString(),
                _etxtDescription.getText().toString(),
                _imgUrlReturn,
                _genderReturn,
                "",
                false,
                birthday,
                _dateNow
        );
        mDatabaseRef.child("users").child(uid).setValue(user);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case READ_EXTERNAL_STORAGE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    callGallery();
                }
                return;
        }
        Toast.makeText(getApplicationContext(),"...",Toast.LENGTH_SHORT).show();
    }
    private void callGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_INTENT);
    }
    @SuppressWarnings("VisibleForTests")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_INTENT && resultCode == RESULT_OK){

            mImgUri = data.getData();
            _imgVGetImage.setImageURI(mImgUri);
            StorageReference filePath = mStorageRef.child("User_Images").child(mImgUri.getLastPathSegment());

            showProgressDialog();

            filePath.putFile(mImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl(); //Ignore this error

                    _imgUrlReturn = downloadUri.toString();
                    Glide.with(getApplicationContext())
                            .load(downloadUri)
                            .crossFade()
                            .placeholder(R.drawable.load_icon)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(_imgVGetImage);
                   hideProgressDialog();
                }
            });
        }
    }
    public void showDatePickerDialog(){
        calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar date = Calendar.getInstance();
                date.set(year,month,dayOfMonth);
                birthday = TimeUnit.MILLISECONDS.toSeconds(date.getTimeInMillis())+"";
                _etxtBirthDay.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }
    private boolean validateForm() {
        boolean valid = true;

        String email = _etxtEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            _etxtEmail.setError("Require.");
            valid = false;
        } else {
            _etxtEmail.setError(null);
        }

        String password = _etxtPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            _etxtPassword.setError("Require.");
            valid = false;
        } else {
            _etxtPassword.setError(null);
        }

        return valid;

    }

    private void showProgressDialog(){
        progressDialog.setMessage("Waiting...");
        progressDialog.show();
    }
    private void hideProgressDialog(){
        progressDialog.dismiss();
    }
}
