package com.eldarerathis.xposedmodule.wirelesschargingxposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Vibrator;
import android.text.TextUtils;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WirelessChargingXposed implements IXposedHookLoadPackage
{
    private final boolean DEBUG = false;
    private static XSharedPreferences mPrefs;
    
    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable 
    {
		mPrefs = new XSharedPreferences(this.getClass().getPackage().getName());
		
		findAndHookMethod("com.android.server.power.PowerManagerService",
    			lpparam.classLoader, "shouldWakeUpWhenPluggedOrUnpluggedLocked",
    			boolean.class, int.class, boolean.class,
    			new XC_MethodReplacement()
		    	{
					@Override
					protected Object replaceHookedMethod(MethodHookParam param) throws Throwable 
					{
						mPrefs.reload();
						
						int plugType = (Integer)getObjectField(param.thisObject, "mPlugType");
						int oldPlugType = (Integer)param.args[1];
						boolean isPowered = (Boolean)getObjectField(param.thisObject, "mIsPowered");
						boolean wasPowered = (Boolean)param.args[0];
						boolean dockedOnWirelessCharger = (Boolean)param.args[2];
						
						if (plugType == BatteryManager.BATTERY_PLUGGED_WIRELESS)
						{
							if (!wasPowered && isPowered && dockedOnWirelessCharger)
								return mPrefs.getBoolean("pref_key_wake_on_charge", true);
						}
						
						if (oldPlugType == BatteryManager.BATTERY_PLUGGED_WIRELESS)
						{
							if (wasPowered && !isPowered) {
                                if (mPrefs.getBoolean("pref_key_undock_sound", false)) {
                                    Context ctxt = (Context)getObjectField(param.thisObject, "mContext");
                                    String soundPath = mPrefs.getString("pref_key_custom_undock_ringtone", null);
                                    playNotificationSound(ctxt, soundPath);
                                }
                                return mPrefs.getBoolean("pref_key_wake_on_undock", false);
                            }
						}
						
						return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
					}
		    	});
    	
        findAndHookMethod("com.android.server.power.Notifier",
                lpparam.classLoader, "playWirelessChargingStartedSound",
                new XC_MethodReplacement()
                {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable 
                    {
                        mPrefs.reload();
                        
                        Context ctxt = (Context)getObjectField(param.thisObject, "mContext");
                        
                        LogD("Hooking playWirelessChargingStartedSound()");
                        
                        if (mPrefs.getBoolean("pref_key_sound", false))
                        {
                            LogD("Charging started - playing notification sound");
                        
                            if (mPrefs.getBoolean("pref_key_custom_sound", false))
                                playNotificationSound(ctxt, mPrefs.getString("pref_key_custom_ringtone", null));
                            else
                                XposedBridge.invokeOriginalMethod(param.method, param.thisObject, null);
                        }
                        
                        if (mPrefs.getBoolean("pref_key_vibrate", false))
                        {
                            LogD("Charging started - vibrating");

                            Vibrator v = (Vibrator) ctxt.getSystemService(Context.VIBRATOR_SERVICE);
                            long[] pattern = new long[] { 0, 100, 100, 100 };
                            v.vibrate(pattern, -1);
                        }
                        
                        releaseSuspendBlocker(param.thisObject);
                        
                        return null;
                    }
                    
                }
        );
    }
    
    private void playNotificationSound(Context ctxt, final String soundPath)
    {
        if (!TextUtils.isEmpty(soundPath))
        {
            final Uri soundUri = Uri.parse(soundPath);
            if (soundUri != null) 
            {
                final Ringtone sfx = RingtoneManager.getRingtone(ctxt, soundUri);
                if (sfx != null) 
                {
                    sfx.setStreamType(AudioManager.STREAM_NOTIFICATION);
                    sfx.play();
                }
            }
        }
    }
    
    private void releaseSuspendBlocker(Object thisObject)
    {
        Object suspendBlocker = getObjectField(thisObject, "mSuspendBlocker");
        
        if (suspendBlocker == null)
        {
            LogD("Couldn't find mSuspendBlocker; falling back to invoking finishPendingBroadcastLocked()");
            
            callMethod(thisObject, "finishPendingBroadcastLocked");
        }
        else
        {
            try 
            {
                Method m = suspendBlocker.getClass().getMethod("release");
                m.invoke(thisObject);
            } 
            catch (NoSuchMethodException e) { handleInvokeException(e, thisObject); }
            catch (IllegalArgumentException e) { handleInvokeException(e, thisObject); }
            catch (IllegalAccessException e) { handleInvokeException(e, thisObject); }
            catch (InvocationTargetException e) { handleInvokeException(e, thisObject); }
        }
    }
    
    private void handleInvokeException(Exception e, Object thisObject)
    {
        e.printStackTrace();
        
        LogD(e.getLocalizedMessage());
        LogD("Error invoking mSuspendBlocker.release(); falling back to invoking finishPendingBroadcastLocked()");
        
        callMethod(thisObject, "finishPendingBroadcastLocked");
    }

    private void LogD(String message)
    {
        if (DEBUG)
            XposedBridge.log(message);
    }
}
