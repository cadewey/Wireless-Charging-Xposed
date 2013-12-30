package com.eldarerathis.xposedmodule.wirelesschargingxposed;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;

public class Settings extends PreferenceFragment implements OnSharedPreferenceChangeListener, OnPreferenceChangeListener
{
    private Activity mActivity = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesMode(1);
        addPreferencesFromResource(R.xml.settings);
        
        RingtonePreference p = (RingtonePreference)findPreference("pref_key_custom_ringtone");
        p.setOnPreferenceChangeListener(this);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        updateRingtoneSummary(p, prefs.getString(p.getKey(), null));
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
    {
        updateSummary(key);
    }
    
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue)
    {
        updateRingtoneSummary((RingtonePreference)preference, (String)newValue);
        return true;
    }
    
    private void updateSummary(String key) 
    {
        if(findPreference(key) instanceof CheckBoxPreference) 
        {
            CheckBoxPreference p = (CheckBoxPreference)findPreference(key);
            String summary = p.isChecked() ? p.getSummaryOn().toString() : p.getSummaryOff().toString();
            p.setSummary(summary);
        }
    }
    
    private void updateRingtoneSummary(RingtonePreference pref, String newValue)
    {
        if (mActivity != null)
        {
            Ringtone ringtone = RingtoneManager.getRingtone(mActivity, Uri.parse(newValue));
            
            if (ringtone != null)
                pref.setSummary(ringtone.getTitle(mActivity));
            else
                pref.setSummary("None");
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
    
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mActivity = activity;
    }
}
