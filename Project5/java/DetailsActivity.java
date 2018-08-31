package com.example.weifei.placessearch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static android.support.design.widget.TabLayout.MODE_SCROLLABLE;

public class DetailsActivity extends AppCompatActivity
{
    private static final String TAG = "DetailsActivity";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private MainActivity.SectionsPagerAdapter mSectionsPagerAdapter;

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor spEditor;

    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private Menu menu;

    public Bundle bundle = new Bundle();
    public JSONObject jsonObject;
    public JSONObject placeDetails;
    public String[] saveStr;
    private String placeId;
    private String placeName;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSharedPreferences = this.getSharedPreferences("mySP", Context.MODE_PRIVATE);
        spEditor = mSharedPreferences.edit();

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.detailsContainer);

        tabLayout = (TabLayout) findViewById(R.id.detailsTabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setTabMode(MODE_SCROLLABLE);

        try
        {
            receiveData();
            placeName = placeDetails.getString("name");
            setTitle(placeName);
            bundle.putString("jsonObj", jsonObject.toString());
            setupViewPager(mViewPager);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;

        if (mSharedPreferences.contains(placeId))
        {
            menu.getItem(1).setIcon(R.drawable.heart_fill_white);
        }

        return super.onCreateOptionsMenu(menu);
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

            case R.id.share:
                try
                {
                    shareToTwitter();
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                return true;

            case R.id.like:
                try
                {
                    addToFavorite();
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupViewPager(ViewPager viewPager)
    {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager());

        infoFragment mInfoFragment = new infoFragment();
        mInfoFragment.setArguments(bundle);
        Spannable infoSpan = new SpannableString("   INFO");
        Drawable infoImage = getBaseContext().getDrawable(R.drawable.info_outline);
        infoImage.setBounds(25, 25, 75, 75);
        ImageSpan infoImageSpan = new ImageSpan(infoImage, ImageSpan.ALIGN_BASELINE);
        infoSpan.setSpan(infoImageSpan, 0,1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        adapter.addFragment(mInfoFragment, infoSpan);

        photosFragment mPhotosFragment = new photosFragment();
        mPhotosFragment.setArguments(bundle);
        Spannable photosSpan = new SpannableString("   PHOTOS");
        Drawable photosImage = getBaseContext().getDrawable(R.drawable.photos);
        photosImage.setBounds(25, 25, 75, 75);
        ImageSpan photosImageSpan = new ImageSpan(photosImage, ImageSpan.ALIGN_BASELINE);
        photosSpan.setSpan(photosImageSpan, 0,1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        adapter.addFragment(mPhotosFragment, photosSpan);

        mapFragment mMapFragment = new mapFragment();
        mMapFragment.setArguments(bundle);
        Spannable mapSpan = new SpannableString("   MAP");
        Drawable mapImage = getBaseContext().getDrawable(R.drawable.maps);
        mapImage.setBounds(25, 25, 75, 75);
        ImageSpan mapImageSpan = new ImageSpan(mapImage, ImageSpan.ALIGN_BASELINE);
        mapSpan.setSpan(mapImageSpan, 0,1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        adapter.addFragment(mMapFragment, mapSpan);

        reviewsFragment mReviewsFragment = new reviewsFragment();
        mReviewsFragment.setArguments(bundle);
        Spannable reviewsSpan = new SpannableString("   REVIEWS");
        Drawable reviewsImage = getBaseContext().getDrawable(R.drawable.review);
        reviewsImage.setBounds(25, 25, 75, 75);
        ImageSpan reviewsImageSpan = new ImageSpan(reviewsImage, ImageSpan.ALIGN_BASELINE);
        reviewsSpan.setSpan(reviewsImageSpan, 0,1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        adapter.addFragment(mReviewsFragment, reviewsSpan);

        viewPager.setAdapter(adapter);
    }

    public void receiveData() throws JSONException
    {
        Intent mIntent = getIntent();
        String receivedData = mIntent.getStringExtra("jsonObj");
        jsonObject = new JSONObject(receivedData);
        placeDetails = jsonObject.getJSONObject("result");
        placeId = placeDetails.getString("place_id");
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<Spannable> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        public void addFragment(Fragment fragment, Spannable title)
        {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public Fragment getItem(int position)
        {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return mFragmentList.get(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position)
        {
            return mFragmentTitleList.get(position);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return mFragmentList.size();
        }
    }

    public void shareToTwitter() throws JSONException
    {
        String name = placeDetails.getString("name");
        String address = placeDetails.getString("formatted_address");
        String website = placeDetails.getString("website");
        String myMessage = "Check out " + name + " located at " + address;
        if (placeDetails.has("website"))
        {
            myMessage += '\n' + "Website: " + website;
        }

        Intent tweet = new Intent(Intent.ACTION_SEND);
        tweet.putExtra(Intent.EXTRA_TEXT, "This is a new tweet.");
        tweet.setType("text/plain");

        PackageManager packManager = getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweet, PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;
        for (ResolveInfo resolveInfo : resolvedInfoList)
        {
            if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android"))
            {
                tweet.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                resolved = true;
                break;
            }
        }
        if (resolved)
        {
            startActivity(tweet);
        }
        else
        {
            Intent i = new Intent();
            i.putExtra(Intent.EXTRA_TEXT, myMessage);
            i.setAction(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://twitter.com/intent/tweet?text=" + urlEncode(myMessage)));
            startActivity(i);
        }
    }

    private String urlEncode(String s)
    {
        try
        {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return "";
        }
    }

    public void addToFavorite() throws JSONException
    {
        if (mSharedPreferences.contains(placeId))
        {
            spEditor.remove(placeId);
            spEditor.apply();

            menu.getItem(1).setIcon(R.drawable.heart_outline_white);
            Toast.makeText(this, placeName + " was removed from favorites", Toast.LENGTH_SHORT).show();
        }
        else
        {
            saveStr = new String[5];
            saveStr[0] = placeId;
            saveStr[1] = placeDetails.getString("icon");
            saveStr[2] = placeDetails.getString("name");
            saveStr[3] = placeDetails.getString("vicinity");
            saveStr[4] = "yes";

            Gson gson = new Gson();
            String myStr = gson.toJson(saveStr);

            spEditor.putString(placeId, myStr);
            spEditor.apply();

            menu.getItem(1).setIcon(R.drawable.heart_fill_white);
            Toast.makeText(this, placeName + " was added to favorites", Toast.LENGTH_SHORT).show();
        }
    }
}
