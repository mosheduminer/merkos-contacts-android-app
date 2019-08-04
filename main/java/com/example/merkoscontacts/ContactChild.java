package com.example.merkoscontacts;


public class ContactChild {

    public String _id;
    public Number age;
    public String jewish_knowledge;
    public String country;
    public String city;
    public String street_address;
    public Number[] phone;
    public String email;
    public String affiliation;
    public String children;
    public String marital_status;
    public String spouse;
    public String languages;
    public String[] notes;

    public ContactChild(ContactInfo contactInfo) {
        if (contactInfo.city != null)
            this.city = contactInfo.city;
        if (contactInfo.street_address != null)
            this.street_address = contactInfo.street_address;
        if (contactInfo._id != null)
            this._id = contactInfo._id;
        if (contactInfo.age != null)
            this.age = contactInfo.age;
        if (contactInfo.jewish_knowledge != null)
            this.jewish_knowledge = contactInfo.jewish_knowledge;
        if (contactInfo.country != null)
            this.country = contactInfo.country;
        if (contactInfo.phone != null)
            this.phone = contactInfo.phone;
        if (contactInfo.email != null)
            this.email= contactInfo.email;
        if (contactInfo.affiliation != null)
            this.affiliation = contactInfo.affiliation;
        if (contactInfo.children!= null)
            this.children = contactInfo.children;
        if (contactInfo.marital_status != null)
            this.marital_status  = contactInfo.marital_status;
        if (contactInfo.spouse != null)
            this.spouse = contactInfo.spouse;
        if (contactInfo.languages != null)
            this.languages = contactInfo.languages;
        if (contactInfo.notes != null)
            this.notes = contactInfo.notes;
    }

}
