package com.example.kaildyhoang.mycookbookapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UserActivity extends AppCompatActivity {
    private ImageView _imgVUserAvatar,_imgVUserCover;
    private TextView _txtVShowUN;
    private Button _btnToPost,_btnFollow;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef,postDatabaseRef;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> mPostAdapter;
    private String TAG = "PostAdapter";
    private String nameFriend,keyFriend,uName,userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        initialiseScreen();
    }

    private  void initialiseScreen(){

        _imgVUserAvatar = (ImageView) findViewById(R.id.imageViewAvatarUser);
        _imgVUserCover = (ImageView) findViewById(R.id.imageViewCoverUser);

        _txtVShowUN = (TextView) findViewById(R.id.textViewNameUser);

        _btnToPost = (Button) findViewById(R.id.buttonToPost);
        _btnFollow = (Button) findViewById(R.id.buttonFollow);

        databaseRef = FirebaseDatabase.getInstance().getReference();
        postDatabaseRef = FirebaseDatabase.getInstance().getReference().child("posts");

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewUser);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(UserActivity.this));

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        userID = user.getUid();
        Toast.makeText(UserActivity.this,"Wait! Fetching list..." + userID, Toast.LENGTH_SHORT).show();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            if(bundle.containsKey("MyBundleFromMain")){
                keyFriend = intent.getBundleExtra("MyBundleFromMain").getString("keyFriend");
                Log.d(TAG,"MyBundleFromMain"+keyFriend);
                if(keyFriend.equalsIgnoreCase(userID)){
                    _btnFollow.setVisibility(View.GONE);
                }else {
                    _btnFollow.setVisibility(View.VISIBLE);
                }
            }
            if(bundle.containsKey("MyBundleFromSearch")){
                keyFriend = intent.getBundleExtra("MyBundleFromSearch").getString("keyFriend");
                Log.d(TAG,"MyBundleFromSearch"+keyFriend);
                if(keyFriend.equalsIgnoreCase(userID)){
                    _btnFollow.setVisibility(View.GONE);
                }else {
                    _btnFollow.setVisibility(View.VISIBLE);
                }
            }
        }

        _btnToPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),AddNewPostActivity.class));
            }
        });
        _btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_btnFollow.getText().equals("FOLLOW")){
                    doFollow();
                }else if(_btnFollow.getText().equals("UNFOLLOW")){
                    doUnFollow();
                }
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        databaseRef.child("users/"+userID+"/idFriendsList/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    if(childDataSnapshot.getKey().equals(keyFriend)){
                        _btnFollow.setText("UNFOLLOW");
                    }else{
                        _btnFollow.setText("FOLLOW");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mPostAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(
                Post.class,
                R.layout.activity_post_items,
                PostViewHolder.class,
                postDatabaseRef.orderByChild("postBy").equalTo(keyFriend))
        {

            @Override
            protected void populateViewHolder(final UserActivity.PostViewHolder viewHolder, Post model, final int position) {
                final String postById = model.getPostBy();

                databaseRef.child("users/"+postById).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);

                        viewHolder.postBy(user.getName());
                        viewHolder.avatarPicture(user.getAvatar());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getMessage());
                    }
                });
                viewHolder.postTitle(model.getTitle());
                viewHolder.countOfLikes(String.valueOf(model.getCountOfLikes()));
                viewHolder.illustrationPicture(model.getIllustrationPicture());

//                Item Click
                viewHolder._imgVAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Toast.makeText(getApplicationContext(),"Ahihi",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(),UserActivity.class);
//                        Bundle bundle = new Bundle();
//                        bundle.putString("uId",postById);
//                        intent.putExtra("MyBundle",bundle);
                        startActivity(intent);
                    }
                });
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(UserActivity.this);
                        builder.setMessage("Do you want to delete this data?").setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int selectedItem = position;

                                        Log.d(TAG, "Dy:onClick " + "position:" + selectedItem);

                                        mPostAdapter.getRef(selectedItem).removeValue();
                                        mPostAdapter.notifyItemRemoved(selectedItem);
                                        recyclerView.invalidate();
                                        onStart();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.setTitle("Confirm");
                        dialog.show();
                    }
                });
            }
        };
        recyclerView.setAdapter(mPostAdapter);

        databaseRef.child("users/"+keyFriend).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                nameFriend = user.getName();
                Glide.with(getApplicationContext())
                        .load(user.getAvatar())
                        .crossFade()
                        .placeholder(R.drawable.load_icon)
                        .thumbnail(0.1f)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(_imgVUserAvatar);
                _txtVShowUN.setText(user.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void doFollow(){
        FirebaseUser userMain = mAuth.getCurrentUser();
        final String uid = userMain.getUid();

        Map<String, Object> userPost = new HashMap<String, Object>();
        userPost.put(keyFriend,nameFriend);
        databaseRef.child("users/"+uid+"/idFriendsList").updateChildren(userPost);

        databaseRef.child("users/"+uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> beFollowed = new HashMap<String, Object>();
                User user = dataSnapshot.getValue(User.class);
                uName = user.getName();
                beFollowed.put(uid,uName);
                databaseRef.child("users/"+keyFriend+"/beFollowed").updateChildren(beFollowed);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        _btnFollow.setText("UNFOLLOW");
    }
    private void doUnFollow(){
        databaseRef.child("users/"+userID+"/idFriendsList/"+keyFriend).removeValue();
        _btnFollow.setText("FOLLOW");
        databaseRef.child("users/"+keyFriend+"/beFollowed/"+userID).removeValue();
    }
    //    View holder for recycler view
    public static class PostViewHolder extends RecyclerView.ViewHolder{

        private final TextView _txtVUserName, _txtVCountLikes, _txtVTitle;
        private final ImageView _imgVCover, _imgVAvatar;

        public PostViewHolder(final View itemView) {
            super(itemView);

            //TextView
            _txtVUserName = (TextView) itemView.findViewById(R.id.textViewUserName);
            _txtVCountLikes = (TextView) itemView.findViewById(R.id.textViewCountLikes);
            _txtVTitle = (TextView) itemView.findViewById(R.id.textViewTitle);

            //ImageView
            _imgVCover = (ImageView) itemView.findViewById(R.id.imageViewCover);
            _imgVAvatar = (ImageView) itemView.findViewById(R.id.imageViewAvatar);


        }

        private void postBy(String title){
            _txtVUserName.setText(title);
        }
        private void countOfLikes(String title){
            _txtVCountLikes.setText(String.valueOf(title));
        }
        private void postTitle(String title){
            _txtVTitle.setText(title);
        }
        private void illustrationPicture(String title){
            Glide.with(itemView.getContext())
                    .load(title)
                    .crossFade()
                    .placeholder(R.drawable.load_icon)
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(_imgVCover);
        }
        private void avatarPicture(String title){
            Glide.with(itemView.getContext())
                    .load(title)
                    .crossFade()
                    .placeholder(R.drawable.load_icon)
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(_imgVAvatar);
        }
    }

}
