package com.example.weifei.placessearch;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ResultsActivity extends AppCompatActivity
{
    private static final String TAG = "ResultsActivity";
    SharedPreferences.Editor spEditor;
    SharedPreferences mSharedPreferences;

    public ListView mListView;
    public TextView noResultsView;
    public String[] placeIdList;
    public String[] iconList;
    public String[] nameList;
    public String[] addressList;
    public String[] favoriteList;

    public JSONArray jsonArray;
    public String[][][] rowData = new String[3][5][20];
    public ProgressDialog mProgressDialog;
    public String nextPageToken;
    public boolean[] ifHasNextPage = new boolean[3];
    public Button mPrevButton;
    public Button mNextButton;
    public int currentPage = 0;
    private boolean ifFisrtTime;

    public Intent newIntent;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        setTitle("Search Results");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        spEditor = this.getSharedPreferences("mySP", Context.MODE_PRIVATE).edit();
        mSharedPreferences = this.getSharedPreferences("mySP", Context.MODE_PRIVATE);

        mNextButton = (Button)findViewById(R.id.nextButton);
        mPrevButton = (Button)findViewById(R.id.previousButton);
        mListView = (ListView)findViewById(R.id.resultsList);
        noResultsView = (TextView) findViewById(R.id.noResults);
        newIntent = new Intent(this, DetailsActivity.class);

        try
        {
            receiveData();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        mNextButton.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        getNextPage();
                    }
                }
        );

        mPrevButton.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        getPreviousPage();
                    }
                }
        );

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                newIntent = new Intent(ResultsActivity.this, DetailsActivity.class);
                requestDetails(rowData[currentPage][2][position], rowData[currentPage][0][position]);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void onResume()
    {
        super.onResume();
        if (!ifFisrtTime)
        {
            checkIfFavorite();
            setAdapterForListView();
        }
        ifFisrtTime = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void receiveData() throws JSONException
    {
        Intent mIntent = getIntent();
        String receivedData = mIntent.getStringExtra("jsonObj");
        JSONObject jsonObject = new JSONObject(receivedData);

        if (jsonObject.getString("status").equals("OK"))
        {
            ifFisrtTime = true;
            hasResults();
            generateTable(jsonObject);
        }
        else
        {
            noResults();
        }
    }

    public void setAdapterForListView()
    {
        resultsListFragment resultsListAdapter = new resultsListFragment(this,rowData[currentPage][0], rowData[currentPage][1],
                rowData[currentPage][2], rowData[currentPage][3], rowData[currentPage][4]);
        mListView.setAdapter(resultsListAdapter);
    }

    public void generateTable(JSONObject jsonObject) throws JSONException
    {
        try
        {
            if (jsonObject.has("next_page_token"))
            {
                ifHasNextPage[currentPage] = true;
                mNextButton.setEnabled(true);
                nextPageToken = jsonObject.getString("next_page_token");
            }
            else
            {
                ifHasNextPage[currentPage] = false;
                mNextButton.setEnabled(false);
            }

            jsonArray = jsonObject.getJSONArray("results");
            placeIdList = new String[jsonArray.length()];
            iconList = new String[jsonArray.length()];
            nameList = new String[jsonArray.length()];
            addressList = new String[jsonArray.length()];
            favoriteList = new String[jsonArray.length()];

            for (int i = 0; i < jsonArray.length(); i++)
            {
                String place_id = jsonArray.getJSONObject(i).getString("place_id");
                String icon = jsonArray.getJSONObject(i).getString("icon");
                String name = jsonArray.getJSONObject(i).getString("name");
                String address = jsonArray.getJSONObject(i).getString("vicinity");

                placeIdList[i] = place_id;
                iconList[i] = icon;
                nameList[i] = name;
                addressList[i] = address;
                if (mSharedPreferences.contains(placeIdList[i]))
                {
                    favoriteList[i] = "yes";
                }
                else
                {
                    favoriteList[i] = "no";
                }

                rowData[currentPage][0][i] = placeIdList[i];
                rowData[currentPage][1][i] = iconList[i];
                rowData[currentPage][2][i] = nameList[i];
                rowData[currentPage][3][i] = addressList[i];
                rowData[currentPage][4][i] = favoriteList[i];
            }
            resultsListFragment resultsListAdapter = new resultsListFragment(this, placeIdList, iconList, nameList, addressList, favoriteList);
            mListView.setAdapter(resultsListAdapter);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void checkIfFavorite()
    {
        for (int i = 0; i < rowData[currentPage][0].length; i++)
        {
            if (mSharedPreferences.contains(rowData[currentPage][0][i]))
            {
                rowData[currentPage][4][i] = "yes";
            }
            else
            {
                rowData[currentPage][4][i] = "no";
            }
        }
    }

    public void getNextPage()
    {
        if (currentPage == 0)
        {
            if (rowData[currentPage+1][0][0] == null)
            {
                if (ifHasNextPage[currentPage])
                {
                    requestNextPage();
                    currentPage++;
                    mPrevButton.setEnabled(true);
                }
                else
                {
                    mNextButton.setEnabled(false);
                }
            }
            else
            {
                currentPage++;
                checkIfFavorite();
                setAdapterForListView();
                mPrevButton.setEnabled(true);
                if (ifHasNextPage[currentPage])
                {
                    mNextButton.setEnabled(true);
                }
                else
                {
                    mNextButton.setEnabled(false);
                }
            }
        }
        else if (currentPage == 1)
        {
            if (rowData[currentPage+1][0][0] == null)
            {
                if (ifHasNextPage[currentPage] == true)
                {
                    requestNextPage();
                    currentPage++;
                    mPrevButton.setEnabled(true);
                }
                else
                {
                    mNextButton.setEnabled(false);
                }
            }
            else
            {
                currentPage++;
                checkIfFavorite();
                setAdapterForListView();
                mPrevButton.setEnabled(true);
                mNextButton.setEnabled(false);
            }
        }
        else
        {
            mPrevButton.setEnabled(true);
            mNextButton.setEnabled(false);
        }

    }

    public void requestNextPage()
    {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Fetching next page");
        mProgressDialog.show();

        String mUrl = "http://cs571placesearch-env.us-east-2.elasticbeanstalk.com/?";
        mUrl += "nextPageToken=" + nextPageToken;
        System.out.println(mUrl);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, mUrl, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONObject jsonObject = new JSONObject(response);
                    generateTable(jsonObject);
                    //System.out.println(jsonObject.toString());
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
                System.out.println("Request error!");
                System.out.println(error);
            }
        });
        queue.add(stringRequest);
    }

    public void getPreviousPage()
    {
        if (currentPage == 1)
        {
            currentPage--;
            checkIfFavorite();
            setAdapterForListView();
            mPrevButton.setEnabled(false);
            mNextButton.setEnabled(true);
        }
        else if (currentPage == 2)
        {
            currentPage--;
            checkIfFavorite();
            setAdapterForListView();
            mPrevButton.setEnabled(true);
            mNextButton.setEnabled(true);
        }
        else
        {
            mPrevButton.setEnabled(false);
            mNextButton.setEnabled(true);
        }
    }

    public void redirect()
    {
        this.startActivity(newIntent);
    }

    public void requestDetails(String mName, String mPlaceId)
    {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Fetching details");
        mProgressDialog.show();

        String mUrl = "https://maps.googleapis.com/maps/api/place/details/json?";
        mUrl += "placeid=" + mPlaceId;
        mUrl += "&key=AIzaSyC9HBExGTftsTmeBjHXLucUi5NH2QXCQkY";
        System.out.println(mUrl);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, mUrl, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONObject jsonObject = new JSONObject(response);
                    //System.out.println(jsonObject.toString());
                    newIntent.putExtra("jsonObj", jsonObject.toString());
                    redirect();
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
                        System.out.println("Request error!");
                        System.out.println(error);
                    }
                });
        queue.add(stringRequest);
    }

    public void hasResults()
    {
        mListView.setVisibility(View.VISIBLE);
        mNextButton.setVisibility(View.VISIBLE);
        mPrevButton.setVisibility(View.VISIBLE);
        noResultsView.setVisibility(View.GONE);
    }

    public void noResults()
    {
        mListView.setVisibility(View.GONE);
        mNextButton.setVisibility(View.GONE);
        mPrevButton.setVisibility(View.GONE);
        noResultsView.setVisibility(View.VISIBLE);
    }
}
