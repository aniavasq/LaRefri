package com.larefri;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

public class AppEULA {
    private final String EULA_PREFIX = "appeula";
    private Activity mContext;
    private String eulaKey;
    private PackageInfo versionInfo;
 
    public AppEULA(Activity context) {
        mContext = context;
        versionInfo = getPackageInfo();
        eulaKey = EULA_PREFIX + versionInfo.versionCode;
    }
 
    private PackageInfo getPackageInfo() {
        PackageInfo info = null;
        try {
            info = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }
 
    public void show() {
 
        // The eulaKey changes every time you increment the version number in
        // the AndroidManifest.xml
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);
 
        boolean bAlreadyAccepted = prefs.getBoolean(eulaKey, false);
        if (bAlreadyAccepted == false) {
 
            // EULA title
            String title = mContext.getString(R.string.app_name) + " v"
                    + versionInfo.versionName;
 
            // EULA text
            String message = mContext.getString(R.string.eula_string);
 
            // Disable orientation changes, to prevent parent activity
            // re-initialization
            mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                    .setTitle(title)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.accept_eula,
                            new Dialog.OnClickListener() {
 
                                @Override
                                public void onClick(
                                        DialogInterface dialog, int i) {
                                    // Mark this version as read.
                                    SharedPreferences.Editor editor = prefs
                                            .edit();
                                    editor.putBoolean(eulaKey, true);
                                    editor.commit();
 
                                    // Close dialog
                                    dialog.dismiss(); 
                                    // Enable orientation changes based on
                                    // device's sensor
                                    mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                                    //Create User
                                    NewAccountActivity.initUser(mContext);
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new Dialog.OnClickListener() {
 
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    // Close the activity as they have declined
                                    // the EULA
                                	SharedPreferences.Editor editor = prefs
                                            .edit();
                                    editor.putBoolean(eulaKey, false);
                                    editor.commit();
                                    mContext.finish();// Close dialog
                                    dialog.dismiss(); 
                                    mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                                }
 
                            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

	public String getEulaKey() {
		return eulaKey;
	}
}
