package com.washmywhip.wmwvendor;

/**
 * Created by Ross on 4/2/2016.
 */
public class NavDrawerItem {
    private String title;
    private int icon;

    public NavDrawerItem(String title, int icon){
        this.title = title;
        this.icon = icon;
    }
    public void setIcon(int icon){
        this.icon = icon;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle(){
        return this.title;
    }
    public int getIcon(){
        return this.icon;
    }
}
