package com.larefri;

import com.parse.Parse;
import com.parse.ParseObject;

import android.app.Activity;

public class ParseConnector {

	private static ParseConnector INSTANCE = null;
	private static final String APP_ID = "SUbJkdEdIIE5PB3lQU5IDbXnnbZt0bj5iSJlVvC1";
	private static final String CLIENT_KEY = "masayeGZhHKhgGIVLPvx9vk0x1AAX3BRZA0UDHwK";
	private static Activity master;
	
    private ParseConnector(Activity master){
    	ParseConnector.master = master;
    	Parse.enableLocalDatastore(ParseConnector.master);
		Parse.initialize(ParseConnector.master, APP_ID, CLIENT_KEY);
    }
 
    private synchronized static void createInstance(Activity master) {
        if (INSTANCE == null) { 
            INSTANCE = new ParseConnector(master);
        }else{
        	ParseConnector.setMaster(master);
        }
    }
 
    public static ParseConnector getInstance(Activity master) {
        if (INSTANCE == null) createInstance(master);
        return INSTANCE;
    }
    
    private static void setMaster(Activity new_master){
    	ParseConnector.master = new_master;
    }
    
	public static void registerSubclass(Class<?extends ParseObject> c){
    	ParseObject.registerSubclass(c);
    }
}
