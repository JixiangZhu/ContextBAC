package com.zjx.DialManager;

import java.io.InputStream;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.zjx.test.R;

public class ContactDial extends ListActivity {

    Context mContext = null;
	public SharedPreferences sharedPrefer;
	public SharedPreferences.Editor editor;
	
    /**��ȡ��Phon���ֶ�**/
    private static final String[] PHONES_PROJECTION = new String[] {
	    Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID,Phone.CONTACT_ID };
   
    /**��ϵ����ʾ����**/
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;
    
    /**�绰����**/
    private static final int PHONES_NUMBER_INDEX = 1;
    
    /**ͷ��ID**/
    private static final int PHONES_PHOTO_ID_INDEX = 2;
   
    /**��ϵ�˵�ID**/
    private static final int PHONES_CONTACT_ID_INDEX = 3;
    

    /**��ϵ������**/
    private ArrayList<String> mContactsName = new ArrayList<String>();
    
    /**��ϵ�˺���**/
    private ArrayList<String> mContactsNumber = new ArrayList<String>();

    /**��ϵ��ͷ��**/
    private ArrayList<Bitmap> mContactsPhonto = new ArrayList<Bitmap>();
    
    ListView mListView = null;
    MyListAdapter myAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	mContext = this;
	mListView = this.getListView();
	sharedPrefer = getSharedPreferences("dialAthority", 0);
	editor = sharedPrefer.edit();
	/**�õ��ֻ�ͨѶ¼��ϵ����Ϣ**/
	getPhoneContacts();

	myAdapter = new MyListAdapter(this);
	setListAdapter(myAdapter);


	mListView.setOnItemClickListener(new OnItemClickListener() {

	   public void onItemClick(AdapterView<?> adapterView, View view,
		    final int position, long id) {
		//����ϵͳ��������绰
		if(sharedPrefer.contains(position + "")){
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setMessage("�˻��������˲���˺���Ӵ");
			builder.setPositiveButton("��Ȼ����", new OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent dialIntent = new Intent(Intent.ACTION_CALL, Uri
							.parse("tel:" + mContactsNumber.get(position)));
						startActivity(dialIntent);
				}
				
			});
			Log.v("NBA","position###");
			builder.setNegativeButton("ȡ��", new OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
				
			});
			builder.create().show();
		}

	    }
	});

	super.onCreate(savedInstanceState);
    }

    /**�õ��ֻ�ͨѶ¼��ϵ����Ϣ**/
    private void getPhoneContacts() {
	ContentResolver resolver = mContext.getContentResolver();

	// ��ȡ�ֻ���ϵ��
	Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,PHONES_PROJECTION, null, null, null);


	if (phoneCursor != null) {
	    while (phoneCursor.moveToNext()) {

		//�õ��ֻ�����
		String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
		//���ֻ�����Ϊ�յĻ���Ϊ���ֶ� ������ǰѭ��
		if (TextUtils.isEmpty(phoneNumber))
		    continue;
		
		//�õ���ϵ������
		String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
		
		//�õ���ϵ��ID
		Long contactid = phoneCursor.getLong(PHONES_CONTACT_ID_INDEX);

		//�õ���ϵ��ͷ��ID
		Long photoid = phoneCursor.getLong(PHONES_PHOTO_ID_INDEX);
		
		//�õ���ϵ��ͷ��Bitamp
		Bitmap contactPhoto = null;

		//photoid ����0 ��ʾ��ϵ����ͷ�� ���û�и���������ͷ�������һ��Ĭ�ϵ�
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
    
    /**�õ��ֻ�SIM����ϵ������Ϣ**/
    private void getSIMContacts() {
	ContentResolver resolver = mContext.getContentResolver();
	// ��ȡSims����ϵ��
	Uri uri = Uri.parse("content://icc/adn");
	Cursor phoneCursor = resolver.query(uri, PHONES_PROJECTION, null, null,
		null);

	if (phoneCursor != null) {
	    while (phoneCursor.moveToNext()) {

		// �õ��ֻ�����
		String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
		// ���ֻ�����Ϊ�յĻ���Ϊ���ֶ� ������ǰѭ��
		if (TextUtils.isEmpty(phoneNumber))
		    continue;
		// �õ���ϵ������
		String contactName = phoneCursor
			.getString(PHONES_DISPLAY_NAME_INDEX);

		//Sim����û����ϵ��ͷ��
		
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
	    //���û�������
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

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView iamge = null;
	    TextView title = null;
	    TextView text = null;
	    CheckBox checkbox = null;
		convertView = LayoutInflater.from(mContext).inflate(
			R.layout.colorlist2, null);
		iamge = (ImageView) convertView.findViewById(R.id.color_image);
		title = (TextView) convertView.findViewById(R.id.color_title);
		text = (TextView) convertView.findViewById(R.id.color_text);
	    //������ϵ������
	    title.setText(mContactsName.get(position));
	    //������ϵ�˺���
	    text.setText(mContactsNumber.get(position));
	    //������ϵ��ͷ��
	    iamge.setImageBitmap(mContactsPhonto.get(position));

	    return convertView;
	}

    }
}
