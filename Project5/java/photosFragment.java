package com.example.weifei.placessearch;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

public class photosFragment extends Fragment
{
    private static final String TAG = "photosFragment";

    public String jsonObject;
    public JSONObject placeDetails;
    public GeoDataClient mGeoDataClient;
    public LinearLayout mLinearLayout;
    public LinearLayout mPhotoBox;
    public TextView noPhotos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_photos, container, false);

        mLinearLayout = (LinearLayout)view.findViewById(R.id.imageLayout);
        mPhotoBox = (LinearLayout)view.findViewById(R.id.photoBox);
        noPhotos = (TextView) view.findViewById(R.id.noPhotos);
        mGeoDataClient = Places.getGeoDataClient(this.getActivity(), null);

        Bundle bundle;
        bundle = this.getArguments();
        jsonObject = bundle.getString("jsonObj");

        try
        {
            JSONObject myJsonObject = new JSONObject(jsonObject);
            placeDetails = myJsonObject.getJSONObject("result");
            String placeId = placeDetails.getString("place_id");
            getPhotos(placeId);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return view;
    }

    // Request photos and metadata for the specified place.
    private void getPhotos(String mPlaceId)
    {
        final String placeId = mPlaceId;
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>()
        {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task)
            {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                // Get the first photo in the list.

                if (photoMetadataBuffer.getCount() == 0)
                {
                    mLinearLayout.setVisibility(View.GONE);
                    noPhotos.setVisibility(View.VISIBLE);
                }
                else
                {
                    mLinearLayout.setVisibility(View.VISIBLE);
                    noPhotos.setVisibility(View.GONE);
                    for (int i = 0; i < photoMetadataBuffer.getCount(); i++)
                    {
                        PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(i);
                        // Get the attribution text.
                        CharSequence attribution = photoMetadata.getAttributions();
                        // Get a full-size bitmap for the photo.
                        Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                        photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<PlacePhotoResponse> task)
                            {
                                try
                                {
                                    PlacePhotoResponse photo = task.getResult();
                                    Bitmap bitmap = photo.getBitmap();

                                    int width = Resources.getSystem().getDisplayMetrics().widthPixels;
                                    int height = (int) (width / bitmap.getWidth() * bitmap.getHeight());
                                    bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

                                    ImageView mImageView = new ImageView(getActivity());
                                    mImageView.setImageBitmap(bitmap);
                                    mPhotoBox.addView(mImageView);
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}
