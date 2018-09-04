package com.example.weifei.placessearch;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.RequestResult;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class mapFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener
{
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyC9HBExGTftsTmeBjHXLucUi5NH2QXCQkY";
    private static final String TAG = "mapFragment";
    private static final String DRIVING = TransportMode.DRIVING;
    private static final String WALKING = TransportMode.WALKING;
    private static final String BICYCLING = TransportMode.BICYCLING;
    private static final String TRANSIT = TransportMode.TRANSIT;
    private final String[] travelModes = {"Driving", "Bicycling", "Transit", "Walking"};
    private static final int overview = 0;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(25, -139), new LatLng(40, -60));

    private View view;
    private Spinner mSpinner;

    private String jsonObject;
    private JSONObject placeDetails;
    private String placeName;
    private String placeAddress;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private AutoCompleteTextView mInputLocation;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private Place mPlace;
    private String startPointName;
    private String startPointAddress;
    private LatLng startPoint;
    private LatLng destinationPoint;
    private Polyline mPolyline;
    private Marker mMarker;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_map, container, false);

        mSpinner = (Spinner)view.findViewById(R.id.modesSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, travelModes);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mSpinner.setAdapter(adapter);

        mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mInputLocation = (AutoCompleteTextView)view.findViewById(R.id.startLocation);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this.getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this.getActivity(), this)
                .build();

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this.getActivity(), mGoogleApiClient, LAT_LNG_BOUNDS, null);
        mInputLocation.setAdapter(mPlaceAutocompleteAdapter);

        Bundle bundle;
        bundle = this.getArguments();
        jsonObject = bundle.getString("jsonObj");

        try
        {
            JSONObject myJsonObject = new JSONObject(jsonObject);
            placeDetails = myJsonObject.getJSONObject("result");
            placeName = placeDetails.getString("name");
            placeAddress = placeDetails.getString("formatted_address");
            String latStr = placeDetails.getJSONObject("geometry").getJSONObject("location").getString("lat");
            String lngStr = placeDetails.getJSONObject("geometry").getJSONObject("location").getString("lng");
            final double lat = Double.parseDouble(latStr);
            final double lng = Double.parseDouble(lngStr);
            generateMap(lat, lng);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        mInputLocation.setOnItemClickListener(mAutocompleteClickListener);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                getDirection(mSpinner.getSelectedItem().toString());
            }
            public void onNothingSelected(AdapterView<?> parent)
            {
                //Must select one of them, so nothing here...
            }
        });

        return view;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    public void generateMap(final double lat, final double lng)
    {
        mMapFragment.getMapAsync(new OnMapReadyCallback()
        {
            @Override
            public void onMapReady(GoogleMap googleMap)
            {
                if (mMapFragment != null)
                {
                    mMap = googleMap;
                    destinationPoint = new LatLng(lat, lng);
                    mMap.addMarker(new MarkerOptions().position(destinationPoint).title(placeName));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(destinationPoint));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 13.0f));
                }
            }
        });
    }

    public AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            //hide keyboard
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            final AutocompletePrediction mPrediction = mPlaceAutocompleteAdapter.getItem(position);
            final String acPlaceId = mPrediction.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, acPlaceId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    public ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>()
    {
        @Override
        public void onResult(@NonNull PlaceBuffer places)
        {
            if (!places.getStatus().isSuccess())
            {
                Log.d(TAG, "Place not found");
                places.release();
            }
            final Place place = places.get(0);
            mPlace = place;
            startPointName = (String)mPlace.getName();
            startPointAddress = (String)mPlace.getAddress();
            startPoint = mPlace.getLatLng();
            getDirection(mSpinner.getSelectedItem().toString());
        }
    };

    public void getDirection(String travelMode)
    {
        String mTransportMode = travelSelection(travelMode);
        if (startPoint == null)
        {
            return;
        }
        System.out.println(travelMode);

        GoogleDirection.withServerKey(GOOGLE_API_KEY)
                .from(startPoint)
                .to(destinationPoint)
                .transportMode(mTransportMode)
                .execute(new DirectionCallback()
                {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody)
                    {
                        //hide the keyboard
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                        if (mPolyline != null)
                        {
                            mPolyline.remove();
                        }

                        if (mMarker != null)
                        {
                            mMarker.remove();
                        }

                        if (direction.isOK())
                        {
                            Route route = direction.getRouteList().get(0);
                            //Leg leg = route.getLegList().get(0);                            //Leg leg = route.getLegList().get(0);
                            //                            //ArrayList<LatLng> sectionPositionList = leg.getSectionPoint();
                            //ArrayList<LatLng> sectionPositionList = leg.getSectionPoint();
                            mMarker = mMap.addMarker(new MarkerOptions().position(startPoint).title(startPointName));

                            ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();

                            mPolyline = mMap.addPolyline(DirectionConverter.createPolyline(getActivity(), directionPositionList, 5, Color.BLUE));
                            setCameraWithCoordinationBounds(route);
                        }
                        else
                        {
                            Toast.makeText(getActivity(), "No direction founds!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        // Do something here
                    }
                });

    }

    private void setCameraWithCoordinationBounds(Route route)
    {
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    public String travelSelection(String travelMode)
    {
        if (travelMode == "Driving")
        {
            return DRIVING;
        }
        else if (travelMode == "Walking")
        {
            return WALKING;
        }
        else if (travelMode == "Bicycling")
        {
            return BICYCLING;
        }
        else
        {
            return TRANSIT;
        }
    }
}

