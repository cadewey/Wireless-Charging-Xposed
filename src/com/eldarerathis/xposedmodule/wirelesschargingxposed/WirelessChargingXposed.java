package com.eldarerathis.xposedmodule.wirelesschargingxposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import android.content.Context;
import android.os.Vibrator;
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
						
						releaseSuspendBlocker(param.thisObject);
						
						return null;
					}
					
				}
		);
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
