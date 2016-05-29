package com.zjx.FileExplore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.zjx.frontCamera.FrontCameraActivity;
import com.zjx.test.MainActivity;
import com.zjx.test.R;
/**
 * ������ļ����в鿴�Լ������û��鿴Ȩ��
 * @author yang
 *
 */
public class FileExplorer extends Activity {

	private TextView tvpath;
	private ListView lvFiles;
	private Button btnParent;
	public int secure_value;
	public SharedPreferences sharedPrefer;
	public SharedPreferences.Editor editor;
	final static int MAX_SIZE = 1000;
	// ��¼��ǰ�ĸ��ļ���
	File currentParent;

	// ��¼��ǰ·���µ������ļ��е��ļ�����
	File[] currentFiles ;
	public FileClass fc = new FileClass();

	public void onResume()
	{
		inflateListView(fc);
		super.onResume();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { 
		   case 20:
		    Bundle bundle=data.getExtras(); //dataΪB�лش���Intent
		    boolean alarm=bundle.getBoolean("Alarm");//str��Ϊ�ش���ֵ
		    secure_value = bundle.getInt("securevalue");
			Log.d("securevalue", "securevalue is " + secure_value);
			
			SetSafeClass(secure_value);
			
		    final int position=bundle.getInt("Position");
		    Log.v("NBA", "Alarm := " + alarm );
		    Log.v("NBA","Position := " + position);
		    //���������������û��������ѣ������ֱ�Ӵ���Ӧ���ļ�������Ŀ¼
		    if(alarm) {
		    	final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    	builder.setTitle("Alarm!");
		    	builder.setMessage("������͵������ע��");
		    	builder.setPositiveButton("ȡ����", new OnClickListener()
		    	{

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						inflateListView(fc);
						return;
						
					}
		    		
		    	});
		    	builder.setNegativeButton("��Ȼ��", new OnClickListener()
		    	{
					public void onClick(DialogInterface arg0, int arg1) {
						OpenDocument(position);
						return;
					}
		    	});
		    	
		    	builder.create().show();
		    }
		    else
		    {
		    	OpenDocument(position);
		    }
		    break;
		default:
		    break;
		    }
		}
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_main);
		lvFiles = (ListView) this.findViewById(R.id.files);
		tvpath = (TextView) this.findViewById(R.id.tvpath);
		btnParent = (Button) this.findViewById(R.id.btnParent);
		sharedPrefer = getSharedPreferences("fileAthority", 0);
		
		editor = sharedPrefer.edit();
		this.SetSafeClass(3);
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);

		// ��ȡϵͳ��SDCard��Ŀ¼
		File root = new File("/mnt/sdcard/");
		
		// ���SD�����ڵĻ�
		if (root.exists()) {
			fc.file = root;
			fc.files = root.listFiles();
			fc.SCLass = new int[MAX_SIZE];
			// ʹ�õ�ǰĿ¼�µ�ȫ���ļ����ļ��������ListView
			inflateListView(fc);
		}
		
		lvFiles.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id)
			{
//				if (isBlock(fc.files[position].getAbsolutePath())&&!isSafe())
				if(!canRead(fc.files[position].getAbsolutePath()))
				{
					Toast.makeText(FileExplorer.this, "�˻����²��˴򿪴��ļ�", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if(getFileClass(fc.files[position].getAbsolutePath()) == 3)
					//�����Զ���ת�����activity��
				{
					Bundle data = new Bundle();
					data.putInt("Position", position);
					Intent intent = new Intent(FileExplorer.this, FrontCameraActivity.class);
					intent.putExtras(data);
					startActivityForResult(intent, 100);
				}
				else{
				if (fc.files[position].isFile()) {
					Toast.makeText(FileExplorer.this,
							"���ļ����ܻ�û���ƣ������ڴ���һ�汾", Toast.LENGTH_LONG).show();
					return;
				}
				// �ж��ļ�Ȩ��
				OpenDocument(position);
//				// ��ȡ�û�������ļ��� �µ������ļ�
//				File[] tem = fc.files[position].listFiles();
//				if (tem == null || tem.length == 0) {
//
//					Toast.makeText(FileExplorer.this,
//							"��ǰ·�����ɷ��ʻ��߸�·����û���ļ�", Toast.LENGTH_LONG).show();
//				}
//				else 
//				{
//					// ��ȡ�û��������б����Ӧ���ļ��У���Ϊ��ǰ�ĸ��ļ���
//					fc.file = fc.files[position];
//					// ���浱ǰ�ĸ��ļ����ڵ�ȫ���ļ����ļ���
//					fc.files = tem;
//					// �ٴθ���ListView
//					inflateListView(fc);
//				}
				}
			}

		});
		// ��ȡ��һ��Ŀ¼
		btnParent.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				try {

					if (!fc.file.getCanonicalPath().equals("/mnt/sdcard")) {

						// ��ȡ��һ��Ŀ¼
						fc.file = fc.file.getParentFile();
						// �г���ǰĿ¼�µ������ļ�
						fc.files = fc.file.listFiles();
						// �ٴθ���ListView
						inflateListView(fc);
					}
				} catch (Exception e) {
					// TODO: handle exception
				}

			}
		});

	}
	
	public void OpenDocument(int position){
		if (fc.files[position].isFile()) {
			// Ҳ���Զ�����չ������ļ���
			Toast.makeText(FileExplorer.this,
					"���ļ����ܻ�û���ƣ������ڴ���һ�汾", Toast.LENGTH_LONG).show();
			return;
		}
		File[] tem = fc.files[position].listFiles();
		if (tem == null || tem.length == 0) {

			Toast.makeText(FileExplorer.this,
					"��ǰ·�����ɷ��ʻ��߸�·����û���ļ�", Toast.LENGTH_LONG).show();
		}
		else 
		{
			// ��ȡ�û��������б����Ӧ���ļ��У���Ϊ��ǰ�ĸ��ļ���
			fc.file = fc.files[position];
			// ���浱ǰ�ĸ��ļ����ڵ�ȫ���ļ����ļ���
			fc.files = tem;
			// �ٴθ���ListView
			inflateListView(fc);
		}
	}
	//�������ؼ��¼�
//	public boolean onKeyDown(int keyCode,KeyEvent event)
//	{
//		if(keyCode == KeyEvent.KEYCODE_BACK)
//		{
//			try {
//
//				if (!fc.file.getCanonicalPath().equals("/mnt/sdcard")) {
//
//					// ��ȡ��һ��Ŀ¼
//					fc.file = fc.file.getParentFile();
//					// �г���ǰĿ¼�µ������ļ�
//					fc.files = fc.file.listFiles();
//					// �ٴθ���ListView
//					inflateListView(fc);
//				}
//				else
//				{
//				//startActivity(new Intent(FileExplorer.this,MainActivity.class));
//				System.exit(0);
//				}
//				// TODO: handle exception
//			}catch (Exception e) {
//				// TODO: handle exception
//			}
//		}
//			return false;
//	}
	/**
	 * �����ļ������ListView
	 * 
	 * @param files
	 */
	private void inflateListView(FileClass fc2) {

		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();

		for (int i = 0; i < fc2.files.length; i++) {

			Map<String, Object> listItem = new HashMap<String, Object>();
			File f = fc2.files[i];
			int value = 0;
			if (f.isDirectory()) 
			{
				if (sharedPrefer.contains(f.getAbsolutePath()))
				{
					value = sharedPrefer.getInt(f.getAbsolutePath(),-1);
					Log.v("NBA",value+"");
					switch(value)
					{
					case 3: listItem.put("icon", R.drawable.folder_r3);
					break;
					case 2: listItem.put("icon", R.drawable.folder_r2);
					break;
					case 1: listItem.put("icon", R.drawable.folder_r1);
					break;
					}
				}
				else{
				listItem.put("icon", R.drawable.folder);
				}
			} 
			else 
			{
				if (sharedPrefer.contains(f.getAbsolutePath()))
				{
					value = sharedPrefer.getInt(f.getAbsolutePath(),0);
					switch(value)
					{
					case 3: listItem.put("icon", R.drawable.file_r3);
					break;
					case 2: listItem.put("icon", R.drawable.file_r2);
					break;
					case 1: listItem.put("icon", R.drawable.file_r1);
					break;
					}
				}
				else{
				listItem.put("icon", R.drawable.file);
				}
			}
			// ���һ���ļ�����
			listItem.put("filename", fc2.files[i].getName());

			File myFile = new File(fc2.files[i].getName());

			// ��ȡ�ļ�������޸�����
			long modTime = myFile.lastModified();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");

			// ���һ������޸�����
			listItem.put("modify",
					"�޸����ڣ�" + dateFormat.format(new Date(modTime)));

			listItems.add(listItem);

		}

		// ����һ��SimpleAdapter
		SimpleAdapter adapter = new SimpleAdapter(
				FileExplorer.this, listItems, R.layout.list_item,
				new String[] { "filename", "icon", "modify" }, new int[] {
						R.id.file_name, R.id.icon, R.id.file_modify });

		// ������ݼ�
		lvFiles.setAdapter(adapter);

		try {
			tvpath.setText("��ǰ·��Ϊ:" + fc.file.getCanonicalPath());
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * �ж��ļ��Ƿ���Ա���ȡ
	 * @param path
	 * @return
	 */
	private boolean canRead(String path)
	{
		int fileclass = getFileClass(path);
		int safeclass = this.secure_value;
		if(safeclass >= fileclass)
			return true;
		return false;
	}
	/**
	 * ����ļ������Ƽ���
	 * @param path
	 * @return
	 */
	public int getFileClass(String path)
	{
		return sharedPrefer.getInt(path, -1);
	}
	public boolean SetSafeClass(int secure_value)
	{
		this.secure_value = secure_value;
		return true;
	}
}
