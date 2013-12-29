package com.eldarerathis.xposedmodule.wirelesschargingxposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import android.content.Context;
import android.os.Vibrator;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class WirelessChargingXposed implements IXposedHookLoadPackage
{
	private final boolean DEBUG = false;
	private XSharedPreferences mPrefs = new XSharedPreferences("com.eldarerathis.xposedmodule.wirelesschargingxposed");
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable 
	{
		findAndHookMethod("com.android.server.power.Notifier",
				lpparam.classLoader, "playWirelessChargingStartedSound",
				new XC_MethodReplacement()
				{
					@Override
					protected Object replaceHookedMethod(MethodHookParam param) throws Throwable 
					{
						mPrefs.reload();
						
						LogD("Hooking playWirelessChargingStartedSound()");
						
						
						if (mPrefs.getBoolean("pref_key_sound", false))
						{
							LogD("Charging started - playing notification sound");
							
							XposedBridge.invokeOriginalMethod(param.method, param.thisObject, null);
						}
						
						if (mPrefs.getBoolean("pref_key_vibrate", false))
						{
							LogD("Charging started - vibrating");
							
							Context ctxt = (Context)getObjectField(param.thisObject, "mContext");
							Vibrator v = (Vibrator) ctxt.getSystemService(Context.VIBRATOR_SERVICE);
							long[] pattern = new long[] { 0, 100, 100, 100 };
							v.vibrate(pattern, -1);
						}
						
						return null;
					}
					
				}
		);
	}

	private void LogD(String message)
	{
		if (DEBUG)
			XposedBridge.log(message);
	}
}
