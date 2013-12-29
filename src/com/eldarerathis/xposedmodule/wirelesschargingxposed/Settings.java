package com.eldarerathis.xposedmodule.wirelesschargingxposed;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;

public class Settings extends PreferenceFragment implements OnSharedPreferenceChangeListener
{
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesMode(1);
        addPreferencesFromResource(R.xml.settings);
    }
	
	@Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
	{
        updateSummary(key);
    }
	
	private void updateSummary(String key) 
	{
        if(findPreference(key) instanceof CheckBoxPreference) {
            CheckBoxPreference p = (CheckBoxPreference)findPreference(key);
            String summary = p.isChecked() ? p.getSummaryOn().toString() : p.getSummaryOff().toString();
            p.setSummary(summary);
        }
    }
	
	@Override
    public void onResume() 
	{
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() 
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
