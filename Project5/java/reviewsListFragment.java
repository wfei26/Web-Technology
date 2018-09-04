package com.example.weifei.placessearch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Rating;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class reviewsListFragment extends ArrayAdapter<String>
{
    private final Activity context;
    private final String[] itemImage;
    private final String[] itemName;
    private final String[] itemRating;
    private final String[] itemTime;
    private final String[] itemText;
    private final String[] itemUrl;

    public reviewsListFragment(Activity context, String[] itemimage, String[] itemname, String[] itemrating, String[] itemtime, String[] itemtext, String[] itemurl)
    {
        super(context, R.layout.fragment_reviewslist, itemname);

        this.context=context;
        this.itemImage = itemimage;
        this.itemName = itemname;
        this.itemRating = itemrating;
        this.itemTime = itemtime;
        this.itemText = itemtext;
        this.itemUrl = itemurl;
    }


    public View getView(final int position, final View view, ViewGroup parent)
    {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.fragment_reviewslist, null,true);

        LinearLayout mLinearLayout = (LinearLayout)rowView.findViewById(R.id.reviewLayout);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.profilePhoto);
        TextView textName = (TextView) rowView.findViewById(R.id.reviewName);
        RatingBar textRating = (RatingBar) rowView.findViewById(R.id.reviewRating);
        TextView textTime = (TextView) rowView.findViewById(R.id.reviewTime);
        TextView textText = (TextView) rowView.findViewById(R.id.reviewText);

        textText.setMovementMethod(LinkMovementMethod.getInstance());
        Picasso.get().load(itemImage[position]).into(imageView);
        textName.setText(itemName[position]);
        textRating.setRating(Float.parseFloat(itemRating[position]));
        textTime.setText(itemTime[position]);
        textText.setText(itemText[position]);

        imageView.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(itemUrl[position]));
                context.startActivity(browserIntent);
            }
        });

        textText.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(itemUrl[position]));
                context.startActivity(browserIntent);
            }
        });

        mLinearLayout.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(itemUrl[position]));
                context.startActivity(browserIntent);
            }
        });

        return rowView;
    };

}