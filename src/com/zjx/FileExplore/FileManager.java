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

import com.zjx.FileExplore.SDCardFileExplorerActivity.SafeClass;
import com.zjx.test.R;
/**
 * �ļ������࣬����ʵ�ֻ����ļ�Ȩ�޵�����Լ�remove
 * @author yang
 *
 */
public class FileManager extends Activity {

	private TextView tvpath;
	private ListView lvFiles;
	private Button btnParent;
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
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_main);

		lvFiles = (ListView) this.findViewById(R.id.files);
		tvpath = (TextView) this.findViewById(R.id.tvpath);
		btnParent = (Button) this.findViewById(R.id.btnParent);
		sharedPrefer = getSharedPreferences("fileAthority", 0);
		editor = sharedPrefer.edit();
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
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
				builder.setItems(new String[] {"����Ϊ�����ļ�","ȡ�������ļ�","���ļ�"}, new OnClickListener()
				{

					public void onClick(DialogInterface dialog, int which) {
						switch(which)
						{
						case 0: {
							alertDialog2(position);
						}
						break;
						case 1: {
							cancelSecret(fc.files[position].getAbsolutePath());
							inflateListView(fc);
							Toast.makeText(FileManager.this,
									"ȡ���ļ� " + fc.files[position].getName() + "�Ļ���Ȩ��"
									, Toast.LENGTH_SHORT).show();
						}
						break;
						case 2: Open(position);
						break;
						}
					}

					
				});
				builder.create().show();

			}
			//remove�ļ��Ļ���Ȩ��
			private void cancelSecret(String path)
			{
				editor.remove(path);
				editor.commit();
			};
			
			//�ڶ����alertDialog�����þ����Ȩ�޼���
			private void alertDialog2(final int position)
			{
				builder2.setItems(new String[] {"Ȩ�޵ȼ�R3","Ȩ�޵ȼ�R2","Ȩ�޵ȼ�R1"}, new OnClickListener()
				{
					public void onClick(DialogInterface dialog,	int which) {
						switch(which)
						{
						case 0: {
							editor.putInt(fc.files[position].getAbsolutePath(),3);
							editor.commit();
							Toast.makeText(FileManager.this,
									"�����ļ� " + fc.files[position].getName() + "Ϊ�����ļ�,Ȩ�޼���Ϊ���R3"
									, Toast.LENGTH_SHORT).show();
						}
						break;
						case 1: {
							editor.putInt(fc.files[position].getAbsolutePath(),2);
							editor.commit();
							Toast.makeText(FileManager.this,
									"�����ļ� " + fc.files[position].getName() + "Ϊ�����ļ�,Ȩ�޼���Ϊһ��R2"
									, Toast.LENGTH_SHORT).show();
						}
						break;
						case 2: {
							editor.putInt(fc.files[position].getAbsolutePath(),1);
							editor.commit();
							Toast.makeText(FileManager.this,
									"�����ļ� " + fc.files[position].getName() + "Ϊ�����ļ�,Ȩ�޼���Ϊ���R1"
									, Toast.LENGTH_SHORT).show();
						}
						break;
						}
						inflateListView(fc);
					}
					
				});
				builder2.create().show();
			}
			private void Open(int position){
				File[] tem = fc.files[position].listFiles();
				if (tem == null || tem.length == 0) {

					Toast.makeText(FileManager.this,
							"��ǰ·�����ɷ��ʻ��߸�·����û���ļ�", Toast.LENGTH_LONG).show();
				} else {
					// ��ȡ�û��������б����Ӧ���ļ��У���Ϊ��ǰ�ĸ��ļ���
					fc.file = fc.files[position];
					// ���浱ǰ�ĸ��ļ����ڵ�ȫ���ļ����ļ���
					fc.files = tem;
					// �ٴθ���ListView
					inflateListView(fc);
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
//	//�������ؼ��¼�
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
//				//startActivity(new Intent(FileManager.this,MainActivity.class));
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
			int value = 0;
			File f = fc2.files[i];
			//���Ŀ��Ϊ�ļ��У�����ʾͼ��Ϊ�ļ��У�����Ϊ�ļ���
			if (f.isDirectory()) 
			{
				if (sharedPrefer.contains(f.getAbsolutePath()))
				{
					value = sharedPrefer.getInt(f.getAbsolutePath(),0);
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
				FileManager.this, listItems, R.layout.list_item,
				new String[] { "filename", "icon", "modify" }, new int[] {
						R.id.file_name, R.id.icon, R.id.file_modify });

		// ������ݼ�
		lvFiles.setAdapter(adapter);

		try {
			tvpath.setText("��ǰ·��Ϊ:" + fc.file.getCanonicalPath());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
