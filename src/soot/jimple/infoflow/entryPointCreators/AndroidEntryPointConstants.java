package soot.jimple.infoflow.entryPointCreators;

import java.util.Arrays;
import java.util.List;

public class AndroidEntryPointConstants {
	
	public static final String ACTIVITYCLASS = "android.app.Activity";
	public static final String SERVICECLASS = "android.app.Service";
	public static final String BROADCASTRECEIVERCLASS = "android.content.BroadcastReceiver";
	public static final String CONTENTPROVIDERCLASS = "android.content.ContentProvider";
	
	public static final String ACTIVITY_ONCREATE = "void onCreate(android.os.Bundle)";
	public static final String ACTIVITY_ONSTART = "void onStart()";
	public static final String ACTIVITY_ONRESUME = "void onResume()";
	public static final String ACTIVITY_ONPAUSE = "void onPause()";
	public static final String ACTIVITY_ONSTOP = "void onStop()";
	public static final String ACTIVITY_ONRESTART = "void onRestart()";
	public static final String ACTIVITY_ONDESTROY = "void onDestroy()";
	
	public static final String SERVICE_ONCREATE = "void onCreate()";
	public static final String SERVICE_ONSTART1 = "void onStart(android.content.Intent,int)";
	public static final String SERVICE_ONSTART2 = "int onStartCommand(android.content.Intent,int,int)";
	public static final String SERVICE_ONBIND = "android.os.IBinder onBind(android.content.Intent)";
	public static final String SERVICE_ONREBIND = "void onRebind(android.content.Intent)";
	public static final String SERVICE_ONUNBIND = "boolean onUnbind(android.content.Intent)";
	public static final String SERVICE_ONDESTROY = "void onDestroy()";
	
	public static final String BROADCAST_ONRECEIVE = "void onReceive(android.content.Context,android.content.Intent)";
	
	public static final String CONTENTPROVIDER_ONCREATE = "boolean onCreate()";
	
	private static final String[] activityMethods = {ACTIVITY_ONCREATE, ACTIVITY_ONDESTROY, ACTIVITY_ONPAUSE, ACTIVITY_ONRESTART, ACTIVITY_ONRESUME, ACTIVITY_ONSTART, ACTIVITY_ONSTOP};
	private static final String[] serviceMethods = {SERVICE_ONCREATE, SERVICE_ONDESTROY, SERVICE_ONSTART1, SERVICE_ONSTART2, SERVICE_ONBIND, SERVICE_ONREBIND, SERVICE_ONUNBIND};
	private static final String[] broadcastMethods = {BROADCAST_ONRECEIVE};
	private static final String[] contentproviderMethods = {CONTENTPROVIDER_ONCREATE};
	
	public static List<String> getActivityLifecycleMethods(){
		return Arrays.asList(activityMethods);
	}
	
	public static List<String> getServiceLifecycleMethods(){
		return Arrays.asList(serviceMethods);
	}
	
	public static List<String> getBroadcastLifecycleMethods(){
		return Arrays.asList(broadcastMethods);
	}
	
	public static List<String> getContentproviderLifecycleMethods(){
		return Arrays.asList(contentproviderMethods);
	}

}
