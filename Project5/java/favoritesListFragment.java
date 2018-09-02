package com.example.weifei.placessearch;

import android.app.Activity;
import android.app.MediaRouteButton;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.example.weifei.placessearch.favoritesFragment.mFavoriteListView;
import static com.example.weifei.placessearch.favoritesFragment.noFavoritesView;

public class favoritesListFragment extends ArrayAdapter<String>
{
    public SharedPreferences mSharedPreferences;
    public SharedPreferences.Editor spEditor;

    private Context mContext;
    private final Activity context;
    private ArrayList<String> itemPlaceId;
    private ArrayList<String> itemIcon;
    private ArrayList<String> itemName;
    private ArrayList<String> itemAddress;
    private ArrayList<String> itemFavorite;
    public ImageView favoriteView;

    public favoritesListFragment(Activity context, ArrayList<String> itemplaceid, ArrayList<String> itemicon,
                                 ArrayList<String> itemname, ArrayList<String> itemaddress, ArrayList<String> itemfavorite)
    {
        super(context, R.layout.fragment_favoriteslist, itemname);

        mSharedPreferences = context.getSharedPreferences("mySP", Context.MODE_PRIVATE);
        spEditor = mSharedPreferences.edit();

        this.context=context;
        this.itemPlaceId = itemplaceid;
        this.itemIcon = itemicon;
        this.itemName = itemname;
        this.itemAddress = itemaddress;
        this.itemFavorite = itemfavorite;

        this.mContext = context;
    }

    public View getView(final int position, View view, ViewGroup parent)
    {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.fragment_favoriteslist, null,true);

        ImageView iconView = (ImageView) rowView.findViewById(R.id.resultIcon_favorite);
        TextView textName = (TextView) rowView.findViewById(R.id.resultName_favorite);
        TextView textAddress = (TextView) rowView.findViewById(R.id.resultAddress_favorite);
        favoriteView = (ImageView) rowView.findViewById(R.id.resultFavorite_favorite);

        Picasso.get().load(itemIcon.get(position)).into(iconView);
        textName.setText(itemName.get(position));
        textAddress.setText(itemAddress.get(position));
        favoriteView.setImageResource(R.drawable.heart_fill_red);

        favoriteView.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        deleteFromFavorite(position);
                    }
                }
        );

        return rowView;
    };

    public void deleteFromFavorite(int position)
    {
        Toast.makeText(context, itemName.get(position) + " was removed from favorites", Toast.LENGTH_SHORT).show();
        spEditor.remove(itemPlaceId.get(position));
        spEditor.apply();

        itemPlaceId.remove(position);
        itemIcon.remove(position);
        itemName.remove(position);
        itemAddress.remove(position);
        itemFavorite.remove(position);

        if (itemPlaceId.size() == 0)
        {
            noFavoritesView.setVisibility(View.VISIBLE);
            mFavoriteListView.setVisibility(View.GONE);
        }

        this.notifyDataSetChanged();
    }
}
