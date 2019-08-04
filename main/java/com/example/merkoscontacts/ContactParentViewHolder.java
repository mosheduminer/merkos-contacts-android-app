package com.example.merkoscontacts;

import android.view.View;
import android.widget.TextView;


public class ContactParentViewHolder extends ParentViewHolder {

    public TextView mTitleTextView;

    public ContactParentViewHolder(View itemView) {
        super(itemView);

        mTitleTextView = (TextView) itemView.findViewById(R.id.parent_list_item);
    }
}
