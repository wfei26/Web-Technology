package com.example.weifei.placessearch;

import android.media.Image;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class reviewsFragment extends Fragment
{
    private static final String TAG = "reviewsFragment";
    public final String[] reviewSources = {"Google reviews", "Yelp reviews"};
    public final String[] reviewOrders = {"Default order", "Highest rating", "Lowest rating", "Most recent", "Least recent"};

    public ListView mListView;
    public Spinner mReivewSpinner;
    public Spinner mOrderSpinner;
    public TextView noResultsView;

    public String jsonObject;
    public JSONObject placeDetails;
    public JSONObject yelpJsonObject;
    public String[][] googleReviewsArr;
    public String[][] originalGoogleReviewsArr;
    public String[][] yelpReviewsArr;
    public String[][] originalYelpReviewsArr;
    public boolean ifHasGoogleReviews;
    public boolean ifHasYelpReviews;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_reviews, container, false);
        mListView = (ListView)view.findViewById(R.id.reviewList);
        noResultsView = (TextView)view.findViewById(R.id.noReviews);

        mReivewSpinner = (Spinner)view.findViewById(R.id.reviewSpinner);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, reviewSources);
        adapter1.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mReivewSpinner.setAdapter(adapter1);

        mOrderSpinner = (Spinner)view.findViewById(R.id.orderSpinner);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, reviewOrders);
        adapter2.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mOrderSpinner.setAdapter(adapter2);

        mReivewSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                apiAndOrderSelector(mReivewSpinner.getSelectedItem().toString(), mOrderSpinner.getSelectedItem().toString());
            }
            public void onNothingSelected(AdapterView<?> parent)
            {
                //Must select one of them, so nothing here...
            }
        });

        mOrderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                apiAndOrderSelector(mReivewSpinner.getSelectedItem().toString(), mOrderSpinner.getSelectedItem().toString());
            }
            public void onNothingSelected(AdapterView<?> parent)
            {
                //Must select one of them, so nothing here...
            }
        });

        Bundle bundle;
        bundle = this.getArguments();
        jsonObject = bundle.getString("jsonObj");
        //System.out.println(jsonObject);

        try
        {
            JSONObject myJsonObject = new JSONObject(jsonObject);
            placeDetails = myJsonObject.getJSONObject("result");
            JSONArray googleReviews = placeDetails.getJSONArray("reviews");
            generateGoogleReviews(googleReviews);
            ifHasGoogleReviews = true;
            requestYelpApi();
        }
        catch (JSONException e)
        {
            ifHasGoogleReviews = false;
            mListView.setVisibility(View.GONE);
            noResultsView.setVisibility(View.VISIBLE);
            e.printStackTrace();
        }
        return view;
    }

    public void generateGoogleReviews(JSONArray reviewsObject) throws JSONException
    {
        googleReviewsArr = new String[reviewsObject.length()][6];
        for (int i = 0; i < reviewsObject.length(); i++)
        {
            if (reviewsObject.getJSONObject(i).getString("profile_photo_url") != null)
            {
                googleReviewsArr[i][0] = reviewsObject.getJSONObject(i).getString("profile_photo_url");
            }
            else
            {
                googleReviewsArr[i][0] = "https://www.oculustraining.com/wp-content/uploads/2015/12/profile-blank.png";
            }

            googleReviewsArr[i][1] = reviewsObject.getJSONObject(i).getString("author_name");

            googleReviewsArr[i][2] = reviewsObject.getJSONObject(i).getString("rating");

            String reviewTime = transferTime(reviewsObject.getJSONObject(i).getString("time"));
            googleReviewsArr[i][3] = reviewTime;

            googleReviewsArr[i][4] = reviewsObject.getJSONObject(i).getString("text");

            googleReviewsArr[i][5] = reviewsObject.getJSONObject(i).getString("author_url");
        }

        originalGoogleReviewsArr = new String[reviewsObject.length()][6];
        for (int i = 0; i < reviewsObject.length(); i++)
        {
            for (int j = 0; j < 6; j++)
            {
                originalGoogleReviewsArr[i][j] = googleReviewsArr[i][j];
            }
        }
        setAdapterForListView(googleReviewsArr);
    }

    public void requestYelpApi() throws JSONException
    {
        String fullAddress = placeDetails.getString("formatted_address");
        String addressParts[] = fullAddress.split(", ");
        String ifYelp = "yelpData";
        String name = placeDetails.getString("name");
        name = name.replaceAll(" ", "%20").toLowerCase();
        String address1 = addressParts[0];
        address1 = address1.replaceAll(" ", "%20").toLowerCase();
        String address2 = addressParts[1] + ", " + addressParts[2];
        address2 = address2.replaceAll(" ", "%20").toLowerCase();
        String city = " ";
        String state = " ";
        if (addressParts.length == 4)
        {
            city = addressParts[1];
            city = city.replaceAll(" ", "%20").toLowerCase();
            state = addressParts[2].substring(0, addressParts[2].indexOf(" "));
        }
        else
        {
            city = city = addressParts[0];
            city = city.replaceAll(" ", "%20").toLowerCase();
            state = addressParts[1].substring(0, addressParts[1].indexOf(" "));
        }
        String country = "US";

        String mUrl = "http://cs571placesearch-env.us-east-2.elasticbeanstalk.com/?";
        mUrl += "ifYelp=" + ifYelp + "&name=" + name + "&address1=" + address1 +
                "&address2=" + address2 + "&city=" + city + "&state=" + state + "&country=" + country;
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
                    yelpJsonObject = new JSONObject(response);
                    JSONArray yelpArr = yelpJsonObject.getJSONArray("reviews");
                    generateYelpReviews(yelpArr);
                    ifHasYelpReviews = true;
                }
                catch (JSONException e)
                {
                    ifHasYelpReviews = false;
                    noResultsView.setVisibility(View.VISIBLE);
                    mListView.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        //Toast.makeText(getActivity(), "No connection! Please check your internet connection.", Toast.LENGTH_SHORT).show();
                        System.out.println("Request error!");
                        System.out.println(error);
                    }
                });
        queue.add(stringRequest);
    }

    public void generateYelpReviews(JSONArray reviewsObject) throws JSONException
    {
        yelpReviewsArr = new String[reviewsObject.length()][6];
        for (int i = 0; i < reviewsObject.length(); i++)
        {
            if (reviewsObject.getJSONObject(i).getJSONObject("user").getString("image_url") != null)
            {
                yelpReviewsArr[i][0] = reviewsObject.getJSONObject(i).getJSONObject("user").getString("image_url");
            }
            else
            {
                yelpReviewsArr[i][0] = "https://s3-media1.fl.yelpcdn.com/photo/uup2RtyJCfUuMALwNBxITA/o.jpg";
            }

            yelpReviewsArr[i][1] = reviewsObject.getJSONObject(i).getJSONObject("user").getString("name");
            yelpReviewsArr[i][2] = reviewsObject.getJSONObject(i).getString("rating");
            yelpReviewsArr[i][3] = reviewsObject.getJSONObject(i).getString("time_created");
            yelpReviewsArr[i][4] = reviewsObject.getJSONObject(i).getString("text");
            yelpReviewsArr[i][5] = reviewsObject.getJSONObject(i).getString("url");
        }

        originalYelpReviewsArr = new String[reviewsObject.length()][6];
        for (int i = 0; i < reviewsObject.length(); i++)
        {
            for (int j = 0; j < 6; j++)
            {
                originalYelpReviewsArr[i][j] = yelpReviewsArr[i][j];
            }
        }
    }

    public void setAdapterForListView(String[][] operateArr)
    {
        String[] profileImageArr = new String[operateArr.length];
        String[] nameArr = new String[operateArr.length];
        String[] ratingArr = new String[operateArr.length];
        String[] timeArr = new String[operateArr.length];
        String[] textArr = new String[operateArr.length];
        String[] urlArr = new String[operateArr.length];

        for (int j = 0; j < operateArr.length; j++)
        {
            profileImageArr[j] = operateArr[j][0];
            nameArr[j] = operateArr[j][1];
            ratingArr[j] = operateArr[j][2];
            timeArr[j] = operateArr[j][3];
            textArr[j] = operateArr[j][4];
            urlArr[j] = operateArr[j][5];
        }

        reviewsListFragment reviewListAdapter = new reviewsListFragment(this.getActivity(),
                profileImageArr, nameArr, ratingArr, timeArr, textArr, urlArr);
        mListView.setAdapter(reviewListAdapter);
    }

    public String transferTime(String secondsStr)
    {
        long unixSeconds = Long.parseLong(secondsStr);
        Date date = new java.util.Date(unixSeconds * 1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT-4"));
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    public void apiAndOrderSelector(String apiType, String orderType)
    {
        if (apiType.equals("Google reviews") && ifHasGoogleReviews == false)
        {
            noResultsView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }
        else if (apiType.equals("Yelp reviews") && ifHasYelpReviews == false)
        {
            noResultsView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }
        else
        {
            noResultsView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            if (orderType.equals("Default order"))
            {
                if (apiType.equals("Google reviews"))
                {
                    setAdapterForListView(originalGoogleReviewsArr);
                }
                else
                {
                    setAdapterForListView(originalYelpReviewsArr);
                }
            }
            else if (orderType.equals("Highest rating"))
            {
                if (apiType.equals("Google reviews"))
                {
                    sortDescending(googleReviewsArr, 2);
                    setAdapterForListView(googleReviewsArr);
                }
                else
                {
                    sortDescending(yelpReviewsArr, 2);
                    setAdapterForListView(yelpReviewsArr);
                }
            }
            else if (orderType.equals("Lowest rating"))
            {
                if (apiType.equals("Google reviews"))
                {
                    sortAscending(googleReviewsArr, 2);
                    setAdapterForListView(googleReviewsArr);
                }
                else
                {
                    sortAscending(yelpReviewsArr, 2);
                    setAdapterForListView(yelpReviewsArr);
                }
            }
            else if (orderType.equals("Most recent"))
            {
                if (apiType.equals("Google reviews"))
                {
                    sortDescending(googleReviewsArr, 3);
                    setAdapterForListView(googleReviewsArr);
                }
                else
                {
                    sortDescending(yelpReviewsArr, 3);
                    setAdapterForListView(yelpReviewsArr);
                }
            }
            else if (orderType.equals("Least recent"))
            {
                if (apiType.equals("Google reviews"))
                {
                    sortAscending(googleReviewsArr, 3);
                    setAdapterForListView(googleReviewsArr);
                }
                else
                {
                    sortAscending(yelpReviewsArr, 3);
                    setAdapterForListView(yelpReviewsArr);
                }
            }
        }
    }

    public void sortAscending(String[][] data, final int index)
    {
        Arrays.sort(data, new Comparator<String[]>()
        {
            @Override
            public int compare(final String[] entry1, final String[] entry2)
            {
                final String time1 = entry1[index];
                final String time2 = entry2[index];
                return time1.compareTo(time2);
            }
        });
    }

    public void sortDescending(String[][] data, final int index)
    {
        Arrays.sort(data, new Comparator<String[]>()
        {
            @Override
            public int compare(final String[] entry1, final String[] entry2)
            {
                final String time1 = entry1[index];
                final String time2 = entry2[index];
                return time2.compareTo(time1);
            }
        });
    }
}
