package com.eldarerathis.xposedmodule.wirelesschargingxposed;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
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
import android.provider.MediaStore;

public class Settings extends PreferenceFragment implements OnSharedPreferenceChangeListener, OnPreferenceChangeListener
{
    private Activity mActivity = null;
    
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
        addPreferencesFromResource(R.xml.settings);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);

        RingtonePreference p = (RingtonePreference)findPreference("pref_key_custom_ringtone");
        p.setOnPreferenceChangeListener(this);
        updateRingtoneSummary(p, prefs.getString(p.getKey(), null));

        p = (RingtonePreference)findPreference("pref_key_custom_undock_ringtone");
        p.setOnPreferenceChangeListener(this);
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
    	String ringtoneName = "None";
    	Uri uri = Uri.parse(newValue);
    	
    	if (uri.getScheme().equals("file"))
    	{
    		ringtoneName = uri.getLastPathSegment();
    	}
    	else if (uri.getScheme().equals("content"))
    	{
    		try
    		{
	    		String[] projection = { MediaStore.Audio.Media.TITLE  };
	    		Cursor c = mActivity.getContentResolver().query(uri, projection, null, null, null);
	    		
	    		if (c != null && c.getCount() > 0)
	    		{
	    			int columnIndex = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
	    			c.moveToFirst();
	    			ringtoneName = c.getString(columnIndex);
	    		}
    		}
    		catch (SQLiteException ex)
    		{
    			Ringtone ringtone = RingtoneManager.getRingtone(mActivity, Uri.parse(newValue));
    			             
    			 if (ringtone != null)
    				 ringtoneName = ringtone.getTitle(mActivity);
    		}
    	}
    	
    	pref.setSummary(ringtoneName);

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
