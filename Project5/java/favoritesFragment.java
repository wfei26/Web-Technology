package com.example.weifei.placessearch;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class favoritesFragment extends Fragment
{
    private static final String TAG = "favoriteFragment";

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor spEditor;

    public static ListView mFavoriteListView;
    public static TextView noFavoritesView;
    public ProgressDialog mProgressDialog;
    public Intent newIntent;

    private ArrayList<String> placeId;
    private ArrayList<String> icon;
    private ArrayList<String> name;
    private ArrayList<String> address;
    private ArrayList<String> ifFavorite;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);
        //setUserVisibleHint(true);

        if (view != null)
        {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        mSharedPreferences = this.getActivity().getSharedPreferences("mySP", Context.MODE_PRIVATE);
        spEditor = mSharedPreferences.edit();

        mFavoriteListView = (ListView)view.findViewById(R.id.favoriteList);
        noFavoritesView = (TextView)view.findViewById(R.id.noFavorites);
        newIntent = new Intent(this.getActivity(), favoritesFragment.class);

        getFavoriteList();
        return view;
    }

    public void onResume()
    {
        super.onResume();
        getFavoriteList();
    }

    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser || isResumed())
        {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }

    public void getFavoriteList()
    {
        int numOfSP = mSharedPreferences.getAll().size();

        if (numOfSP == 0)
        {
            noFavoritesView.setVisibility(View.VISIBLE);
            mFavoriteListView.setVisibility(View.GONE);
        }
        else
        {
            noFavoritesView.setVisibility(View.GONE);
            mFavoriteListView.setVisibility(View.VISIBLE);

            placeId = new ArrayList<>(numOfSP);
            icon = new ArrayList<>(numOfSP);
            name = new ArrayList<>(numOfSP);
            address = new ArrayList<>(numOfSP);
            ifFavorite = new ArrayList<>(numOfSP);

            Map<String,?> keys = mSharedPreferences.getAll();

            String[] spElement;
            int index = 0;
            for(Map.Entry<String,?> entry : keys.entrySet())
            {
                //Log.d("map values",entry.getKey() + ": " + entry.getValue());
                spElement = entry.getValue().toString().split(",");

                String tempStr = spElement[0].substring(2, spElement[0].length()-1);
                placeId.add(index, tempStr);
                icon.add(index, spElement[1].substring(1, spElement[1].length()-1));
                spElement[2] = spElement[2].replace("\\u0027", "'");
                name.add(index, spElement[2].substring(1, spElement[2].length()-1));
                if (spElement.length == 6)
                {
                    address.add(index, spElement[3].substring(1, spElement[3].length()-1) + ", " + spElement[4].substring(1, spElement[4].length()-1));
                    ifFavorite.add(index, spElement[5].substring(1, spElement[5].length()-2));
                }
                else
                {
                    address.add(index, spElement[3].substring(1, spElement[3].length()-1));
                    ifFavorite.add(index, spElement[4].substring(1, spElement[4].length()-2));
                }

                index++;
            }
            setAdapterForFavoriteListView();
            mFavoriteListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    newIntent = new Intent(getActivity(), DetailsActivity.class);
                    requestDetails(placeId.get(position));
                }
            });
        }
    }

    public void setAdapterForFavoriteListView()
    {
        favoritesListFragment favoritesListAdapter = new favoritesListFragment(this.getActivity(), placeId, icon, name, address, ifFavorite);
        mFavoriteListView.setAdapter(favoritesListAdapter);
        mFavoriteListView.deferNotifyDataSetChanged();
    }

    public void requestDetails(String mPlaceId)
    {
        mProgressDialog = new ProgressDialog(this.getActivity());
        mProgressDialog.setMessage("Fetching details");
        mProgressDialog.show();

        String mUrl = "https://maps.googleapis.com/maps/api/place/details/json?";
        mUrl += "placeid=" + mPlaceId;
        mUrl += "&key=AIzaSyC9HBExGTftsTmeBjHXLucUi5NH2QXCQkY";
        System.out.println(mUrl);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this.getActivity());
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, mUrl, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONObject jsonObject = new JSONObject(response);
                    newIntent.putExtra("jsonObj", jsonObject.toString());
                    getActivity().startActivity(newIntent);
                    mProgressDialog.dismiss();
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast.makeText(getActivity(), "No connection! Please check your internet connection.", Toast.LENGTH_SHORT).show();
                        System.out.println("Request error!");
                        System.out.println(error);
                    }
                });
        queue.add(stringRequest);
    }
}
