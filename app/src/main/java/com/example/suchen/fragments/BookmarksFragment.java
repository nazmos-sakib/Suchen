package com.example.suchen.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.suchen.Adaptar.BookmarksAdapter;
import com.example.suchen.MainActivity;
import com.example.suchen.Model.BookmarkLocationModel;
import com.example.suchen.R;
import com.example.suchen.RecyclerViewClickListener;
import com.example.suchen.databinding.FragmentBookmarksBinding;
import com.example.suchen.fragments.Authentication.LoginFragment;
import com.google.android.material.snackbar.Snackbar;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class BookmarksFragment extends Fragment implements RecyclerViewClickListener {
    private static final String TAG = "BookmarksFragment->";
    FragmentBookmarksBinding binding;
    private Context activityContext;


    public BookmarksFragment(Context ctx) {
        this.activityContext = ctx;
    }

    private BookmarksAdapter bookmarksAdapter;
    private ArrayList<BookmarkLocationModel> bookmarkLocationArrayList = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            // do stuff with the user
            Log.d(TAG, "onCreate: login user->"+currentUser.getUsername());

        } else {
            //user not logged in
            // show the signup or login screen
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer_mainActivity,new LoginFragment())
                    .commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding =  FragmentBookmarksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setRecViewAdapter();
    }

    private void setRecViewAdapter() {
        bookmarksAdapter = new BookmarksAdapter(this);
        bookmarksAdapter.setAdapterData(bookmarkLocationArrayList);
        binding.recViewBookmarksFragment.setAdapter(bookmarksAdapter);
        binding.recViewBookmarksFragment.setLayoutManager(new LinearLayoutManager(this.getContext()));
        //bookmarksAdapter.setAdapterData(fetchDataFromServer());
        Log.d(TAG, "setRecViewAdapter: "+bookmarksAdapter.getItemCount());
        fetchDataFromServer();
        Log.d(TAG, "setRecViewAdapter: "+bookmarksAdapter.getItemCount());

        //swipe implement
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(binding.recViewBookmarksFragment);

    }

    private void fetchDataFromServer(){
        ArrayList<BookmarkLocationModel> arrayList = new ArrayList<>();
        ParseUser currentUser = ParseUser.getCurrentUser();
        //SELECT * FROM 'TABLE' WHERE 'COLUMN' = 'VALUE' LIMIT 1 ; --implementation
        ParseQuery<ParseObject> query1  = new ParseQuery<ParseObject>("Bookmarks");
        query1.orderByDescending("createdAt");
        query1.whereEqualTo("user_id",currentUser.getObjectId());
        query1.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if (objects.size()>0){
                        for (ParseObject object:objects){
                            Log.d(TAG, "done: "+object.toString());
                            BookmarkLocationModel b = new BookmarkLocationModel(
                                    object.getObjectId(),
                                    object.getString("user_id"),
                                    object.getString("title"),
                                    object.getCreatedAt().toString(),
                                    new GeoPoint(
                                            object.getParseGeoPoint("location").getLatitude(),
                                            object.getParseGeoPoint("location").getLongitude()
                                    )
                            );
                            arrayList.add(b);
                        }

                        bookmarksAdapter.setAdapterData(arrayList);
                        binding.progressBarBookmarkFragment.setVisibility(View.INVISIBLE);

                    }
                }

            }
        });
        //bookmarksAdapter.setAdapterData(arrayList);
    }

    //implementation of the RecyclerViewClickListener
    //it holds the current clicked position
    @Override
    public void onRecViewItemClick(int position) {
        // Handle item click event
        BookmarkLocationModel bookmarkLocationModel = bookmarksAdapter.getItemData(position);
        Toast.makeText(getContext(), "Clicked: " + bookmarkLocationModel.getLocationName(), Toast.LENGTH_SHORT).show();

        //redirect to search fragment with clicked object
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer_mainActivity,new ShowBookmarkFragment(activityContext,bookmarkLocationModel))
                .commit();

        /*Intent intent = new Intent(this, CoinDetailsActivity.class);
        intent.putExtra("position",String.valueOf(position+1));
        intent.putExtra("coin_id",clickedCoin.getCoin_id());
        startActivity(intent);*/
    }

    ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            BookmarkLocationModel deletedObj = bookmarksAdapter.getBookmarkLocationArrayList().get(viewHolder.getAdapterPosition());
            //Delete a Row--------------------------
            ParseQuery<ParseObject> queryD = ParseQuery.getQuery("Bookmarks");
            queryD.getInBackground(deletedObj.getId(), new GetCallback<ParseObject>() { //objectId is the primary key
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e==null && object != null) {
                        object.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e==null){
                                    //delete success
                                    //bookmarkLocationArrayList.remove(viewHolder.getAdapterPosition());
                                    bookmarksAdapter.getBookmarkLocationArrayList().remove(viewHolder.getAdapterPosition());
                                    bookmarksAdapter.notifyDataSetChanged();


                                    //soft acknowledgement
                                    Snackbar snackbar =  Snackbar.make(binding.parentBookmarksFragment,"Item Deleted",Snackbar.LENGTH_SHORT);
                                    snackbar.show();
                                } else {
                                    e.getMessage();
                                }
                            }
                        });
                    }
                }
            });


        }
    };
}