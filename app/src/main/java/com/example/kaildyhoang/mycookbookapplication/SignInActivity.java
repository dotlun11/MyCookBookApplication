package com.example.kaildyhoang.mycookbookapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView  _txtVResult, _txtVDetail;
    private EditText _edtTEmail, _edtTPassword;
    private ProgressDialog progressDialog;
    private ImageView _imgVLogo;
    private RoundImage roundImage;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "SignInActivityWithEMAI";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //Find view ID
        //TextView
        findViewById(R.id.textViewForgetPasswordSI).setOnClickListener(this);
        _txtVResult = (TextView) findViewById(R.id.textViewResultSI);
        _txtVDetail = (TextView) findViewById(R.id.textViewDetailSI);

        //EditText
        _edtTEmail = (EditText) findViewById(R.id.editTextEmailSI);
        _edtTPassword = (EditText) findViewById(R.id.editTextPasswordSI);

        //Button
        findViewById(R.id.buttonSignUpSI).setOnClickListener(this);
        findViewById(R.id.buttonSignInSI).setOnClickListener(this);
        findViewById(R.id.buttonSignInFacebookSI).setOnClickListener(this);
        findViewById(R.id.buttonSignInGmailSI).setOnClickListener(this);

        //ImageView
        _imgVLogo = (ImageView) findViewById(R.id.imageViewLogo);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher);
        roundImage = new RoundImage(bitmap);
        _imgVLogo.setImageDrawable(roundImage);

        //Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        //Initialize the progressDialog
        progressDialog = new ProgressDialog(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
    }

    @Override
    public void onClick(View v){
        int i = v.getId();
        if(i == R.id.textViewForgetPasswordSI){

        }else if(i == R.id.buttonSignInSI){
            doSignIn(_edtTEmail.getText().toString(),_edtTPassword.getText().toString());
        }else if(i == R.id.buttonSignUpSI){
            startActivity(new Intent(this,SignUpActivity.class));
        }else if(i == R.id.buttonSignInFacebookSI){

        }else if(i == R.id.buttonSignInGmailSI){

        }
    }

    private void doSignIn(String email,String password){
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        //[Start Sign in]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG,"SignInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            _txtVResult.setText(getString(R.string.emailpassword_status_fmt,user.getEmail(),user.isEmailVerified()));
                            _txtVDetail.setText(getString(R.string.firebase_status_fmt,user.getUid()));
                            Toast.makeText(SignInActivity.this,"Welcome." + user.getEmail(),Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        }else {
                            Log.w(TAG,"SignInWithEmail:failure",task.getException());
                            Toast.makeText(SignInActivity.this,"SignInWithEmail failed.",Toast.LENGTH_SHORT).show();

                            _txtVResult.setText(null);
                            _txtVDetail.setText(null);
                        }
                        if(!task.isSuccessful()){
                            Toast.makeText(SignInActivity.this,"Account not be exist, Please create new account.",Toast.LENGTH_SHORT).show();
                        }

                        hideProgressDialog();
                    }
                });
    }



    private boolean validateForm (){
        boolean valid = true;

        String email = _edtTEmail.getText().toString();
        if(TextUtils.isEmpty(email)){
            _edtTEmail.setError("Require.");
            valid = false;
        }else{
            _edtTEmail.setError(null);
        }

        String password = _edtTPassword.getText().toString();
        if(TextUtils.isEmpty(password)){
            _edtTPassword.setError("Require.");
            valid = false;
        }else{
            _edtTPassword.setError(null);
        }

        return  valid;
    }


    private void showProgressDialog(){
        progressDialog.setMessage("Waiting...");
        progressDialog.show();
    }
    private void hideProgressDialog(){
        progressDialog.dismiss();
    }
}
