package com.example.merkoscontacts;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class ContactsChildViewHolder extends ChildViewHolder {

    TextView country, city, address, age, jewish_knowledge, phone, email, affiliation, children, marital_status, spouse, languages, notes, _id;
    Button updateButton;

    public ContactsChildViewHolder(View itemView) {
        super(itemView);

        country = (TextView) itemView.findViewById(R.id.country);
        city = (TextView) itemView.findViewById(R.id.city);
        address = (TextView) itemView.findViewById(R.id.address);
        age = (TextView) itemView.findViewById(R.id.age);
        jewish_knowledge = (TextView) itemView.findViewById(R.id.jewish_knowledge);
        phone = (TextView) itemView.findViewById(R.id.phone);
        email = (TextView) itemView.findViewById(R.id.email);
        affiliation = (TextView) itemView.findViewById(R.id.affiliation);
        children = (TextView) itemView.findViewById(R.id.children);
        marital_status = (TextView) itemView.findViewById(R.id.marital_status);
        spouse = (TextView) itemView.findViewById(R.id.spouse);
        languages = (TextView) itemView.findViewById(R.id.languages);
        notes = (TextView) itemView.findViewById(R.id.notes);
        updateButton = (Button) itemView.findViewById(R.id.update_button);
        _id = (TextView) itemView.findViewById(R.id._id);
    }
}
