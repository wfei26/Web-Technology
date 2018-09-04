package com.example.weifei.placessearch;

import android.app.Activity;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import static android.content.Context.MODE_PRIVATE;

public class resultsListFragment extends ArrayAdapter<String>
{
    public SharedPreferences mSharedPreferences;
    public SharedPreferences.Editor spEditor;

    private Context mContext;
    private final Activity context;
    private final String[] itemPlaceId;
    private final String[] itemIcon;
    private final String[] itemName;
    private final String[] itemAddress;
    private final String[] itemFavorite;
    private String[] saveStr;

    public ImageView favoriteView;

    public resultsListFragment(Activity context, String[] itemplaceid, String[] itemicon,
                               String[] itemname, String[] itemaddress, String[] itemfavorite)
    {
        super(context, R.layout.fragment_resultslist, itemname);

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
        View rowView=inflater.inflate(R.layout.fragment_resultslist, null,true);

        LinearLayout resultLayout = (LinearLayout) rowView.findViewById(R.id.resultLinearLayout);
        ImageView iconView = (ImageView) rowView.findViewById(R.id.resultIcon);
        TextView textName = (TextView) rowView.findViewById(R.id.resultName);
        TextView textAddress = (TextView) rowView.findViewById(R.id.resultAddress);
        favoriteView = (ImageView) rowView.findViewById(R.id.resultFavorite);

        if (itemPlaceId[position] != null)
        {
            Picasso.get().load(itemIcon[position]).into(iconView);
            textName.setText(itemName[position]);
            textAddress.setText(itemAddress[position]);

            if (itemFavorite[position] == "no")
            {
                favoriteView.setImageResource(R.drawable.heart_outline_black);
            }
            else
            {
                favoriteView.setImageResource(R.drawable.heart_fill_red);
            }
        }
        else
        {
            resultLayout.setVisibility(View.GONE);
            iconView.setVisibility(View.GONE);
            favoriteView.setVisibility(View.GONE);
            textName.setVisibility(View.GONE);
            textAddress.setVisibility(View.GONE);
            rowView.setVisibility(View.GONE);
        }

        favoriteView.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        addToFavorite(position);
                    }
                }
        );

        return rowView;
    };

    public void addToFavorite(int position)
    {
        String placeName = itemName[position];
        if (itemFavorite[position] == "yes")
        {
            itemFavorite[position] = "no";
            spEditor.remove(itemPlaceId[position]);
            spEditor.apply();

            this.notifyDataSetChanged();
            Toast.makeText(context, placeName + " was removed from favorites", Toast.LENGTH_SHORT).show();
        }
        else
        {
            itemFavorite[position] = "yes";
            saveStr = new String[5];
            saveStr[0] = itemPlaceId[position];
            saveStr[1] = itemIcon[position];
            saveStr[2] = itemName[position];
            saveStr[3] = itemAddress[position];
            saveStr[4] = itemFavorite[position];

            Gson gson = new Gson();
            String myStr = gson.toJson(saveStr);

            spEditor.putString(itemPlaceId[position], myStr);
            spEditor.apply();

            this.notifyDataSetChanged();
            Toast.makeText(context, placeName + " was added to favorites", Toast.LENGTH_SHORT).show();
        }
    }
}
