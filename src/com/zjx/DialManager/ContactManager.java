package com.zjx.DialManager;

import java.io.InputStream;
import java.util.ArrayList;

import com.zjx.test.R;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ContactManager extends ListActivity {

    Context mContext = null;
    
	public SharedPreferences sharedPrefer,sharedPrefer2;
	public SharedPreferences.Editor editor,editor2;
	
    /**获取库Phon表字段**/
    private static final String[] PHONES_PROJECTION = new String[] {
	    Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID,Phone.CONTACT_ID };
   
    /**联系人显示名称**/
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;
    
    /**电话号码**/
    private static final int PHONES_NUMBER_INDEX = 1;
    
    /**头像ID**/
    private static final int PHONES_PHOTO_ID_INDEX = 2;
   
    /**联系人的ID**/
    private static final int PHONES_CONTACT_ID_INDEX = 3;
    

    /**联系人名称**/
    private ArrayList<String> mContactsName = new ArrayList<String>();
    
    /**联系人号码**/
    private ArrayList<String> mContactsNumber = new ArrayList<String>();

    /**联系人头像**/
    private ArrayList<Bitmap> mContactsPhonto = new ArrayList<Bitmap>();
    
    ListView mListView = null;
    MyListAdapter myAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	mContext = this;
	mListView = this.getListView();
	sharedPrefer = getSharedPreferences("dialAthority", 0);
	sharedPrefer2= getSharedPreferences("BlockedNumber",0);
	editor = sharedPrefer.edit();
	editor2= sharedPrefer2.edit();
	/**得到手机通讯录联系人信息**/
	getPhoneContacts();

	myAdapter = new MyListAdapter(this);
	setListAdapter(myAdapter);

	super.onCreate(savedInstanceState);
    }

    /**得到手机通讯录联系人信息**/
    private void getPhoneContacts() {
	ContentResolver resolver = mContext.getContentResolver();

	// 获取手机联系人
	Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,PHONES_PROJECTION, null, null, null);


	if (phoneCursor != null) {
	    while (phoneCursor.moveToNext()) {

		//得到手机号码
		String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
		//当手机号码为空的或者为空字段 跳过当前循环
		if (TextUtils.isEmpty(phoneNumber))
		    continue;
		
		//得到联系人名称
		String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
		
		//得到联系人ID
		Long contactid = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);

		//得到联系人头像ID
		Long photoid = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);
		
		//得到联系人头像Bitamp
		Bitmap contactPhoto = null;

		//photoid 大于0 表示联系人有头像 如果没有给此人设置头像则给他一个默认的
		if(photoid > 0 ) {
		    Uri uri =ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contactid);
		    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(resolver, uri);
		    contactPhoto = BitmapFactory.decodeStream(input);
		}else {
		    contactPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.contact_photo);
		}
		
		mContactsName.add(contactName);
		mContactsNumber.add(phoneNumber);
		mContactsPhonto.add(contactPhoto);
	    }

	    phoneCursor.close();
	}
    }
    
    /**得到手机SIM卡联系人人信息**/
    private void getSIMContacts() {
	ContentResolver resolver = mContext.getContentResolver();
	// 获取Sims卡联系人
	Uri uri = Uri.parse("content://icc/adn");
	Cursor phoneCursor = resolver.query(uri, PHONES_PROJECTION, null, null,
		null);

	if (phoneCursor != null) {
	    while (phoneCursor.moveToNext()) {

		// 得到手机号码
		String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
		// 当手机号码为空的或者为空字段 跳过当前循环
		if (TextUtils.isEmpty(phoneNumber))
		    continue;
		// 得到联系人名称
		String contactName = phoneCursor
			.getString(PHONES_DISPLAY_NAME_INDEX);

		//Sim卡中没有联系人头像
		
		mContactsName.add(contactName);
		mContactsNumber.add(phoneNumber);
	    }

	    phoneCursor.close();
	}
    }
    
    class MyListAdapter extends BaseAdapter {
	public MyListAdapter(Context context) {
	    mContext = context;
	}

	public int getCount() {
	    //设置绘制数量
	    return mContactsName.size();
	}

	@Override
	public boolean areAllItemsEnabled() {
	    return false;
	}

	public Object getItem(int position) {
	    return position;
	}

	public long getItemId(int position) {
	    return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ImageView iamge = null;
	    TextView title = null;
	    TextView text = null;
	    CheckBox checkbox = null;
		convertView = LayoutInflater.from(mContext).inflate(
			R.layout.colorlist, null);
		iamge = (ImageView) convertView.findViewById(R.id.color_image);
		title = (TextView) convertView.findViewById(R.id.color_title);
		text = (TextView) convertView.findViewById(R.id.color_text);
	    checkbox = (CheckBox) convertView.findViewById(R.id.checkBox1);
	    //绘制联系人名称
	    title.setText(mContactsName.get(position));
	    //绘制联系人号码
	    text.setText(mContactsNumber.get(position));
	    final String num = mContactsNumber.get(position);
	    //绘制联系人头像
	    iamge.setImageBitmap(mContactsPhonto.get(position));
	    
	    if(sharedPrefer.contains(position+ ""))
	    {
	    	checkbox.setChecked(true);
	    }
	    
	    checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener()
	    {
	    	
			public void onCheckedChanged(CompoundButton arg0, boolean ischeck) {
				if(ischeck) 
					{
						Toast.makeText(mContext,"checked",Toast.LENGTH_SHORT).show();
						editor.putInt(position + "", position);
						editor2.putString(num,num);
						editor.commit();
						editor2.commit();
					}
				else 
					{
						Toast.makeText(mContext,"unchecked",Toast.LENGTH_SHORT).show();
						editor.remove(position + "");
						editor2.remove(num);
						editor.commit();
						editor2.commit();
					}
			}
	    	
	    });
	    return convertView;
	}

    }
}