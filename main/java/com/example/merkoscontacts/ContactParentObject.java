package com.example.merkoscontacts;

import com.example.merkoscontacts.model.Parent;

import java.util.List;

public class ContactParentObject implements Parent {

    /* Create an instance variable for your list of children */
    private List<Object> mChildrenList;

    public String title;

    public ContactParentObject(String title) {
        this.title = title;
    }

    public void setChildObjectList(List<Object> list) {
        mChildrenList = list;
    }

    @Override
    public List getChildList() {
        return mChildrenList;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
