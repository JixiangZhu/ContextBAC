/**
 * 
 */
package com.zjx.DialManager;

import java.lang.reflect.Method;
import java.util.ArrayList;

import com.android.internal.telephony.ITelephony;
import com.zjx.test.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class BlockMain extends Activity
{
	// 记录机密名单的List
	ArrayList<String> blockList = new ArrayList<String>();
	TelephonyManager tManager;
	// 监听通话状态的监听器
	CustomPhoneCallListener cpListener;
	public SharedPreferences sharedPrefer;
	public SharedPreferences.Editor editor;
	public enum SafeClass {S1, S2, S3};
	SafeClass safeClass = SafeClass.S2;
	SmsManager sManager = SmsManager.getDefault();
	Button dail;
	EditText te;
	
	public class CustomPhoneCallListener extends PhoneStateListener
	{
		@Override
		public void onCallStateChanged(int state, String incomingNumber)
		{
			switch (state)
			{
				case TelephonyManager.CALL_STATE_IDLE:
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					break;
				// 当电话呼入时
				case TelephonyManager.CALL_STATE_RINGING:
					// 如果该号码属于机密名单
					if (isBlock(incomingNumber)&& !isSafe())
					{
						try
						{
							Method method = Class.forName(
								"android.os.ServiceManager").getMethod(
								"getService", String.class);
							// 获取远程TELEPHONY_SERVICE的IBinder对象的代理
							IBinder binder = (IBinder) method.invoke(null,
								new Object[] { TELEPHONY_SERVICE });
							// 将IBinder对象的代理转换为ITelephony对象
							ITelephony telephony = ITelephony.Stub
								.asInterface(binder);
							// 挂断电话
							telephony.endCall();
							PendingIntent pi = PendingIntent.getActivity(BlockMain.this, 0, new Intent(), 0);
							sManager.sendTextMessage(incomingNumber, null, 
									"The User is in a unsafed circumstance and therefore unable to receive your call."
									, pi, null);
							Toast.makeText(BlockMain.this,"此环境不适宜接听此通话", Toast.LENGTH_SHORT).show();
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					break;
			}
			super.onCallStateChanged(state, incomingNumber);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.dial_main);
		
		Bundle bundle = this.getIntent().getExtras(); 
		int securevalue = bundle.getInt("securevalue");
		Log.d("securevalue", "securevalue is " + securevalue);
		
		SetSafeClass(securevalue);
		Log.d("safeClass", "safeClass is " + safeClass.name());
		
		sharedPrefer = getSharedPreferences("BlockedNumber", 0);
		editor = sharedPrefer.edit();
		// 获取系统的TelephonyManager管理器
		tManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		cpListener = new CustomPhoneCallListener();
		// 通过TelephonyManager监听通话状态的改变
		tManager.listen(cpListener, PhoneStateListener.LISTEN_CALL_STATE);
		// 获取程序的按钮，并为它的单击事件绑定监听器
		dail = (Button)findViewById(R.id.dial2);
		dail.setOnClickListener(new OnClickListener(){
		
			
			@Override
			public void onClick(View v) { 
				startActivity(new Intent(BlockMain.this,ContactDial.class));
//				Uri telUri = Uri.parse("tel:"+num);
//				Intent intent = new Intent(Intent.ACTION_CALL,telUri);
//				startActivity(intent);
			}
			
		});
		findViewById(R.id.managerBlock).setOnClickListener(
			new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(BlockMain.this,ContactManager.class));
			}
		});
	}

	// 判断某个电话号码是否在机密名单之内
	public boolean isBlock(String phone)
	{
		if(sharedPrefer.contains(phone))
			return true;
		else
		return false;
	}
	
	public boolean isSafe()
	{
		
		if(safeClass == SafeClass.S3)
		{
			return true;
		}
		return false;
	}
	public boolean SetSafeClass(int safeClass)
	{
		switch (safeClass)
		{
		case 0: this.safeClass = SafeClass.S1;
		break;
		case 1: this.safeClass = SafeClass.S2;
		break;
		case 2: this.safeClass = SafeClass.S3;
		break;
		default: return false;
		};
		return true;
	}
}
