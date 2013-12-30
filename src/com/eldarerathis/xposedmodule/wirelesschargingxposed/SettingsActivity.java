package com.eldarerathis.xposedmodule.wirelesschargingxposed;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class SettingsActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new Settings()).commit();
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
    }
}
