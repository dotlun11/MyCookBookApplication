package com.example.kaildyhoang.mycookbookapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AddNewPostActivity extends AppCompatActivity{
    private EditText _edtTitle,_edtDescription,_edtCountOfPeoples,_edtIngredient,_edtDirectionTime,_edtDirectionContent;
    private ImageView _imgVIllustrationPic,_imgVDirectionIllustrationPic;
    private FloatingActionButton _fabPost, _fabUpdate;

    private String _imgUrlReturn = null; //image url be returned after uploaded onto firebase
    private String _imgUrlReturn2 = null; //image url be returned after uploaded onto firebase
    private String _dateNow;
    private String myMenuOptions,postKey,directionKey;

    public static final int READ_EXTERNAL_STORAGE = 0;
    private static final int GALLERY_INTENT = 1;
    private static final int GALLERY_INTENT2 = 2;
    private ProgressDialog progressDialog;
    private Firebase mFirebase;
    private Uri mImgUri = null;
    private Uri mImgUri2 = null;
    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageRef;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "PostActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_post);

        initialiseScreen();
    }

    private void initialiseScreen(){
        //Get bundle data
        Intent intent = getIntent();
        postKey = intent.getStringExtra("postKey");
        myMenuOptions = intent.getStringExtra("options");
        directionKey = intent.getStringExtra("directionKey");

        // Initialize the progressDialog
        progressDialog = new ProgressDialog(this);

        //Set up Firebase
        Firebase.setAndroidContext(this);

        // Initialize Firebase Database paths for database and storage
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        // Push will create new child every time we upload
        mFirebase = new Firebase("https://cookbookapplication-396d8.firebaseio.com/").push();
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://cookbookapplication-396d8.appspot.com");

        //Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        //EditText
        _edtTitle = (EditText) findViewById(R.id.editTextTitle) ;
        _edtDescription = (EditText) findViewById(R.id.editTextDescription);
        _edtCountOfPeoples = (EditText) findViewById(R.id.editTextCountOfPeople);
        _edtIngredient = (EditText) findViewById(R.id.editTextIngredient);
        _edtDirectionTime = (EditText) findViewById(R.id.editTextDirectionTime);
        _edtDirectionContent = (EditText) findViewById(R.id.editTextDirectionContent);

        //ImageView
        _imgVIllustrationPic = (ImageView) findViewById(R.id.imageViewIllustrationPic);
        _imgVDirectionIllustrationPic = (ImageView) findViewById(R.id.imageViewDirectionIllustrationPic) ;

        _imgVIllustrationPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check for runtime permission
                if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG,"Call for Permission");

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
                    }
                }else {
                    callGallery(GALLERY_INTENT);
                }
            }
        });
        _imgVDirectionIllustrationPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //                Check for runtime permission
                if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG,"Call for Permission");

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
                    }
                }else {
                    callGallery(GALLERY_INTENT2);
                }
            }
        });

        //Button
        _fabPost = (FloatingActionButton) findViewById(R.id.fabPost);
        _fabUpdate = (FloatingActionButton) findViewById(R.id.fabUpdate);

        _fabPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Posting...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                FirebaseUser user = mAuth.getCurrentUser();
                String uid = user.getUid();
                doCreatePost(uid);
            }
        });
        _fabUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Updating...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                doUpdatePost(directionKey);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(myMenuOptions.equals("edit")){
            _fabUpdate.setVisibility(View.VISIBLE);
            _fabPost.setVisibility(View.GONE);
            setDataToUpdate();
        }else if(myMenuOptions.equals("add")){
            _fabUpdate.setVisibility(View.GONE);
            _fabPost.setVisibility(View.VISIBLE);
        }

    }

    private void setDataToUpdate(){
        mDatabaseRef.child("posts/"+postKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                Glide.with(getApplicationContext())
                        .load(post.getIllustrationPicture())
                        .crossFade()
                        .placeholder(R.drawable.load_icon)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(_imgVIllustrationPic);
                _edtTitle.setText(post.getTitle());
                _edtDescription.setText(post.getDescription());
                _edtCountOfPeoples.setText(String.valueOf(post.getEatPeopleCount()));
                _edtDirectionTime.setText(String.valueOf(post.getCookTimeLimit()));
                _edtIngredient.setText(post.getIngredient());
                mDatabaseRef.child("direction/"+post.getDirection()+"/01/").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Direction direction = dataSnapshot.getValue(Direction.class);
                        _edtDirectionContent.setText(direction.getDirectionCont());
                        Glide.with(getApplicationContext())
                                .load(direction.getDirectionIllustrationPicture())
                                .crossFade()
                                .placeholder(R.drawable.load_icon)
                                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                .into(_imgVDirectionIllustrationPic);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void doUpdatePost(String directionKey){

        String title = _edtTitle.getText().toString();
        String description = _edtDescription.getText().toString();
        String directionCont = _edtDirectionContent.getText().toString();
        String ingredient = _edtIngredient.getText().toString();
        int countPeoples = Integer.valueOf(_edtCountOfPeoples.getText().toString());
        int cookTime = Integer.valueOf(_edtDirectionTime.getText().toString());
        boolean isPublic = true;

        Map<String, Object> post = new HashMap<String, Object>();
        post.put("title", title);
        post.put("illustrationPicture", _imgUrlReturn);
        post.put("description", description);
        post.put("ingredient", ingredient);
        post.put("eatPeopleCount", countPeoples);
        post.put("cookTimeLimit", cookTime);

        mDatabaseRef.child("posts/"+postKey).updateChildren(post);

        Map<String, Object> direction = new HashMap<String, Object>();
        direction.put("directionCont", directionCont);
        direction.put("directionIllustrationPicture", _imgUrlReturn2);

        mDatabaseRef.child("direction/"+directionKey+"/01/").updateChildren(direction);

        startActivity(new Intent(getApplicationContext(),MainActivity.class));

    }
    private void doCreatePost(String uid){
        String title = _edtTitle.getText().toString();
        String directionCont = _edtDirectionContent.getText().toString();
        String description = _edtDescription.getText().toString();
        String ingredient = _edtIngredient.getText().toString();
        int countPeoples = Integer.valueOf(_edtCountOfPeoples.getText().toString());
        int cookTime = Integer.valueOf(_edtDirectionTime.getText().toString());
        boolean isPublic = true;

        HashMap<String, String> likeBy = new HashMap<String, String>();
        likeBy.put("likeByName","thisislikeByName");
        Map<String, Map<String, String>> _likeBy = new HashMap<String, Map<String, String>> ();
        _likeBy.put("likeByID",likeBy);

        HashMap<String, String> direction = new HashMap<String, String>();
        direction.put("directionIllustrationPicture",_imgUrlReturn2);
        direction.put("directionCont",directionCont);
        Map<String, Map<String, String>> _direction = new HashMap<String, Map<String, String>> ();
        _direction.put("01",direction);


        String likeKey = mDatabaseRef.child("posts/likeBy/").push().getKey();
        mDatabaseRef.child("likeBy/"+likeKey).setValue(_likeBy);

        String directionKey = mDatabaseRef.child("posts/likeBy/").push().getKey();
        mDatabaseRef.child("direction/"+directionKey).setValue(_direction);

        _dateNow = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())+"";
        final Post post = new Post(
                title,

                _dateNow,
                uid,
                _imgUrlReturn,
                description,
                ingredient,
                directionKey,
                0,
                countPeoples,
                cookTime,
                isPublic
        );


        final String postKey = mDatabaseRef.child("posts").push().getKey();
        mDatabaseRef.child("posts/"+postKey).setValue(post);

        Map<String, Object> userPost = new HashMap<String, Object>();
        userPost.put(postKey,title);
        mDatabaseRef.child("users/"+uid+"/posts").updateChildren(userPost);

        mDatabaseRef.child("users/"+uid+"/beFollowed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
//                    Log.v(TAG,""+ childDataSnapshot.getKey());
//                    Map<String, Object> friendPost = new HashMap<String, Object>();
//                    friendPost.put(postKey,_dateNow);
                    mDatabaseRef.child("users/"+childDataSnapshot.getKey()+"/friendsPosts/"+postKey).setValue(post);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case READ_EXTERNAL_STORAGE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    callGallery(GALLERY_INTENT);
                }
                return;
        }
        Toast.makeText(getApplicationContext(),"...",Toast.LENGTH_SHORT).show();
    }
    private void callGallery(int galleryIntent){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, galleryIntent);
    }
    @SuppressWarnings("VisibleForTests")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_INTENT && resultCode == RESULT_OK){

            mImgUri = data.getData();
            _imgVIllustrationPic.setImageURI(mImgUri);
            StorageReference filePath = mStorageRef.child("Post_Images").child(mImgUri.getLastPathSegment());

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
                            .into(_imgVIllustrationPic);
                    hideProgressDialog();
                }
            });
        }
        if(requestCode == GALLERY_INTENT2 && resultCode == RESULT_OK){

            mImgUri2 = data.getData();
            _imgVDirectionIllustrationPic.setImageURI(mImgUri2);
            StorageReference filePath = mStorageRef.child("Post_Images").child(mImgUri2.getLastPathSegment());

            showProgressDialog();

            filePath.putFile(mImgUri2).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUri = taskSnapshot.getDownloadUrl(); //Ignore this error

                    _imgUrlReturn2 = downloadUri.toString();
                    Glide.with(getApplicationContext())
                            .load(downloadUri)
                            .crossFade()
                            .placeholder(R.drawable.load_icon)
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .into(_imgVDirectionIllustrationPic);
                    hideProgressDialog();
                }
            });
        }
    }

    private boolean validateForm() {
        boolean valid = true;

//        String email = _etxtEmail.getText().toString();
//        if (TextUtils.isEmpty(email)) {
//            _etxtEmail.setError("Require.");
//            valid = false;
//        } else {
//            _etxtEmail.setError(null);
//        }
//
//        String password = _etxtPassword.getText().toString();
//        if (TextUtils.isEmpty(password)) {
//            _etxtPassword.setError("Require.");
//            valid = false;
//        } else {
//            _etxtPassword.setError(null);
//        }

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
