package com.zjx.FileExplore;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zjx.DialManager.BlockMain.SafeClass;
import com.zjx.test.MainActivity;
import com.zjx.test.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SDCardFileExplorerActivity extends Activity {

	private TextView tvpath;
	private ListView lvFiles;
	private Button btnParent;
	public enum SafeClass {S1, S2, S3};
	SafeClass safeClass;
	final static int MAX_SIZE = 1000;
	// ��¼��ǰ�ĸ��ļ���
	File currentParent;

	// ��¼��ǰ·���µ������ļ��е��ļ�����
	File[] currentFiles ;
	public FileClass fc = new FileClass();
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_main);

		lvFiles = (ListView) this.findViewById(R.id.files);
		tvpath = (TextView) this.findViewById(R.id.tvpath);
		btnParent = (Button) this.findViewById(R.id.btnParent);
		//Set Secure Class
		Bundle bundle = this.getIntent().getExtras(); 
		int securevalue = bundle.getInt("securevalue");
		Log.d("securevalue", "securevalue is " + securevalue);
		SetSafeClass(securevalue);
		Log.d("safeClass", "safeClass is " + safeClass.name());
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
		
		lvFiles.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				// ����û��������ļ���ֱ�ӷ��أ������κδ���
				if (fc.files[position].isFile()) {
					// Ҳ���Զ�����չ������ļ���
					return;
				}
				// �жϰ�ȫ�����Լ��ļ�Ȩ��
				if (isBlock(position) && !isSafe())
				{
					Log.e("notsafe", "is not safe");
					Toast.makeText(SDCardFileExplorerActivity.this, "�˻����²��˴򿪴��ļ�", Toast.LENGTH_SHORT).show();
					return;
				}
				// ��ȡ�û�������ļ��� �µ������ļ�
				File[] tem = fc.files[position].listFiles();
				if (tem == null || tem.length == 0) {

					Toast.makeText(SDCardFileExplorerActivity.this,
							"��ǰ·�����ɷ��ʻ��߸�·����û���ļ�", Toast.LENGTH_SHORT).show();
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
		
		lvFiles.setOnItemLongClickListener(new OnItemLongClickListener(){

			public boolean onItemLongClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (fc.SCLass[position] == 0)
					fc.SCLass[position] = 1;
				else fc.SCLass[position] = 0;
				inflateListView(fc);
				return false;
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
	//�������ؼ��¼�
	public boolean onKeyDown(int keyCode,KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			try {

				if (!fc.file.getCanonicalPath().equals("/mnt/sdcard")) {

					// ��ȡ��һ��Ŀ¼
					fc.file = fc.file.getParentFile();
					// �г���ǰĿ¼�µ������ļ�
					fc.files = fc.file.listFiles();
					// �ٴθ���ListView
					inflateListView(fc);
				}
				else
				{
				//startActivity(new Intent(SDCardFileExplorerActivity.this,MainActivity.class));
				System.exit(0);
				}
				// TODO: handle exception
			}catch (Exception e) {
				// TODO: handle exception
			}
		}
			return false;
	}
	/**
	 * �����ļ������ListView
	 * 
	 * @param files
	 */
	private void inflateListView(FileClass fc2) {

		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();

		for (int i = 0; i < fc2.files.length; i++) {

			Map<String, Object> listItem = new HashMap<String, Object>();

			if (fc2.files[i].isDirectory()) {
				// ������ļ��о���ʾ��ͼƬΪ�ļ��е�ͼƬ
				if (fc2.SCLass[i] == 1)
					listItem.put("icon", R.drawable.folder2);
				else{
				listItem.put("icon", R.drawable.folder);
				}
			} else {
				if (fc2.SCLass[i] == 1)
				{
					listItem.put("icon", R.drawable.file2);
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
			System.out.println(dateFormat.format(new Date(modTime)));

			// ���һ������޸�����
			//listItem.put("modify",
				//	"�޸����ڣ�" + dateFormat.format(new Date(modTime)));

			listItems.add(listItem);

		}

		// ����һ��SimpleAdapter
		SimpleAdapter adapter = new SimpleAdapter(
				SDCardFileExplorerActivity.this, listItems, R.layout.list_item,
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
	
	public boolean isBlock(int position) 
	{	
		
		if(fc.SCLass[position] > 0)
			{
				return true;
			}
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