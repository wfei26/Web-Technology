package com.example.weifei.placessearch;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

public class infoFragment extends Fragment
{
    private static final String TAG = "infoFragment";

    public String jsonObject;
    public JSONObject placeDetails;

    public TextView mAddress;
    public TextView mPhoneNumber;
    public TextView mPriceLevel;
    public TextView mRating;
    public TextView mGooglePage;
    public TextView mWebsite;
    public TextView mAddressContent;
    public TextView mPhoneNumberContent;
    public TextView mPriceLevelContent;
    public RatingBar mRatingContent;
    public TextView mGooglePageContent;
    public TextView mWebsiteContent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_info, container, false);


        mAddress = (TextView)view.findViewById(R.id.address);
        mPhoneNumber = (TextView)view.findViewById(R.id.phoneNumber);
        mPriceLevel = (TextView)view.findViewById(R.id.priceLevel);
        mRating = (TextView)view.findViewById(R.id.rating);
        mGooglePage = (TextView)view.findViewById(R.id.googlePage);
        mWebsite = (TextView)view.findViewById(R.id.website);

        mAddressContent = (TextView)view.findViewById(R.id.addressContent);
        mPhoneNumberContent = (TextView)view.findViewById(R.id.phoneNumberContent);
        mPriceLevelContent = (TextView)view.findViewById(R.id.priceLevelContent);
        mRatingContent = (RatingBar)view.findViewById(R.id.ratingContent);
        mGooglePageContent = (TextView)view.findViewById(R.id.googlePageContent);
        mWebsiteContent = (TextView)view.findViewById(R.id.websiteContent);


        Bundle bundle;
        bundle = this.getArguments();
        jsonObject = bundle.getString("jsonObj");

        try
        {
            JSONObject myJsonObject = new JSONObject(jsonObject);
            placeDetails = myJsonObject.getJSONObject("result");
            String placeName = placeDetails.getString("name");

            generateDetails();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return view;
    }

    public void generateDetails() throws JSONException
    {
        if (placeDetails.has("formatted_address"))
        {
            String address = placeDetails.getString("formatted_address");
            setVisible(mAddress, mAddressContent, address);
        }

        if (placeDetails.has("formatted_phone_number"))
        {
            String phoneNumber = placeDetails.getString("formatted_phone_number");
            setVisible(mPhoneNumber, mPhoneNumberContent, phoneNumber);
        }

        if (placeDetails.has("price_level"))
        {
            int price = parseInt(placeDetails.getString("price_level"));
            String priceLevel = "";
            if (price == 1)
            {
                priceLevel = "$";
            }
            else if (price == 2)
            {
                priceLevel = "$$";
            }
            else if (price == 3)
            {
                priceLevel = "$$$";
            }
            else if (price == 4)
            {
                priceLevel = "$$$$";
            }
            else
            {
                priceLevel = " ";
            }
            setVisible(mPriceLevel, mPriceLevelContent, priceLevel);
        }

        if (placeDetails.has("rating"))
        {
            float rating = parseFloat(placeDetails.getString("rating"));
            mRating.setVisibility(View.VISIBLE);
            mRatingContent.setVisibility(View.VISIBLE);
            mRatingContent.setRating(rating);
        }

        if (placeDetails.has("url"))
        {
            String googlePage = placeDetails.getString("url");
            setVisible(mGooglePage, mGooglePageContent, googlePage);
        }

        if (placeDetails.has("website"))
        {
            String website = placeDetails.getString("website");
            setVisible(mWebsite, mWebsiteContent, website);
        }
    }

    public void setVisible(TextView title, TextView content, String text)
    {
        title.setVisibility(View.VISIBLE);
        content.setVisibility(View.VISIBLE);
        content.setText(text);
    }
}
