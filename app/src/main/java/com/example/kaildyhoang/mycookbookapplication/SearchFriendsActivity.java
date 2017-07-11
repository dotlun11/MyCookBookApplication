package com.example.kaildyhoang.mycookbookapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class SearchFriendsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SearchView _searchView;
    private DatabaseReference userDatabaseRef;
    private ProgressDialog progressDialog;
    private FirebaseRecyclerAdapter<User, UserViewHolder> mUserAdapter;
    private String TAG = "UserAdapter";
    private String nameFriend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friends);

        initialiseScreen();

    }

    private void initialiseScreen(){
        userDatabaseRef = FirebaseDatabase.getInstance().getReference();
        _searchView = (SearchView) findViewById(R.id.searchViewFriends);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewSearchFriends);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchFriendsActivity.this));
        Toast.makeText(SearchFriendsActivity.this,"Wait! Fetching list...", Toast.LENGTH_SHORT).show();

        _searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Query dbR = userDatabaseRef.child("users").orderByChild("email").equalTo(newText);
                loadRecy(dbR);
                return false;

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void loadRecy(Query dbR){

        mUserAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(
                User.class,
                R.layout.activity_friend_items,
                UserViewHolder.class,
                dbR)
        {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, User model, final int position) {
               // nameFriend = model.getName();
                viewHolder.userName(model.getName());
                viewHolder.illustrationPicture(model.getAvatar());

//--------------------click---
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String keyUser = mUserAdapter.getRef(position).getKey();
                        //Toast.makeText(getApplicationContext(),keyUser,Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(),UserActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("keyFriend",keyUser);
                        intent.putExtra("MyBundleFromSearch",bundle);
                        startActivity(intent);
                    }
                });
            }


        };
        recyclerView.setAdapter(mUserAdapter);
        mUserAdapter.notifyDataSetChanged();
    }
//-------------------search--------------
  /*  @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Query search = userDatabaseRef.child("users").orderByChild("name").startAt(newText).endAt("~");
        FirebaseRecyclerAdapter<User,UserViewHolder> firebaseRecyclerAdapter22 = new FirebaseRecyclerAdapter<User, UserViewHolder>(
                User.class, R.layout.activity_friend_items, UserViewHolder.class, search) {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, User model, int position) {
                viewHolder.userName(model.getName());
                viewHolder.illustrationPicture(model.getAvatar());
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter22);
        return false;
    }
*/
    //    View holder for recycler view
    public static class UserViewHolder extends RecyclerView.ViewHolder{

        private final TextView  _txtVUserName;
        private final ImageView _imgVAvatar;

        public UserViewHolder(final View itemView) {
            super(itemView);

            //TextView
            _txtVUserName = (TextView) itemView.findViewById(R.id.textViewUserName);

            //ImageView
            _imgVAvatar = (ImageView) itemView.findViewById(R.id.imageViewAvatar);


        }
        private void userName(String title){_txtVUserName.setText(title);
        }
        private void illustrationPicture(String title){
            Glide.with(itemView.getContext())
                    .load(title)
                    .crossFade()
                    .placeholder(R.drawable.load_icon)
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(_imgVAvatar);
        }
    }

    private void showProgressDialog(){
        progressDialog.setMessage("Waiting...");
        progressDialog.show();
    }
    private void hideProgressDialog(){
        progressDialog.dismiss();
    }
    private void setFilter(){

    }
}
