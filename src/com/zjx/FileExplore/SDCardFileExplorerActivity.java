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
	// 记录当前的父文件夹
	File currentParent;

	// 记录当前路径下的所有文件夹的文件数组
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
		// 获取系统的SDCard的目录
		File root = new File("/mnt/sdcard/");
		
		// 如果SD卡存在的话
		if (root.exists()) {
			fc.file = root;
			fc.files = root.listFiles();
			fc.SCLass = new int[MAX_SIZE];
			// 使用当前目录下的全部文件、文件夹来填充ListView
			inflateListView(fc);
		}
		
		lvFiles.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				// 如果用户单击了文件，直接返回，不做任何处理
				if (fc.files[position].isFile()) {
					// 也可自定义扩展打开这个文件等
					return;
				}
				// 判断安全级别以及文件权限
				if (isBlock(position) && !isSafe())
				{
					Log.e("notsafe", "is not safe");
					Toast.makeText(SDCardFileExplorerActivity.this, "此环境下不宜打开此文件", Toast.LENGTH_SHORT).show();
					return;
				}
				// 获取用户点击的文件夹 下的所有文件
				File[] tem = fc.files[position].listFiles();
				if (tem == null || tem.length == 0) {

					Toast.makeText(SDCardFileExplorerActivity.this,
							"当前路径不可访问或者该路径下没有文件", Toast.LENGTH_SHORT).show();
				} else {
					// 获取用户单击的列表项对应的文件夹，设为当前的父文件夹
					fc.file = fc.files[position];
					// 保存当前的父文件夹内的全部文件和文件夹
					fc.files = tem;
					// 再次更新ListView
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
		
		// 获取上一级目录
		btnParent.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				try {

					if (!fc.file.getCanonicalPath().equals("/mnt/sdcard")) {

						// 获取上一级目录
						fc.file = fc.file.getParentFile();
						// 列出当前目录下的所有文件
						fc.files = fc.file.listFiles();
						// 再次更新ListView
						inflateListView(fc);
					}
				} catch (Exception e) {
					// TODO: handle exception
				}

			}
		});

	}
	//监听返回键事件
	public boolean onKeyDown(int keyCode,KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			try {

				if (!fc.file.getCanonicalPath().equals("/mnt/sdcard")) {

					// 获取上一级目录
					fc.file = fc.file.getParentFile();
					// 列出当前目录下的所有文件
					fc.files = fc.file.listFiles();
					// 再次更新ListView
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
	 * 根据文件夹填充ListView
	 * 
	 * @param files
	 */
	private void inflateListView(FileClass fc2) {

		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();

		for (int i = 0; i < fc2.files.length; i++) {

			Map<String, Object> listItem = new HashMap<String, Object>();

			if (fc2.files[i].isDirectory()) {
				// 如果是文件夹就显示的图片为文件夹的图片
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
			// 添加一个文件名称
			listItem.put("filename", fc2.files[i].getName());

			File myFile = new File(fc2.files[i].getName());

			// 获取文件的最后修改日期
			long modTime = myFile.lastModified();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			System.out.println(dateFormat.format(new Date(modTime)));

			// 添加一个最后修改日期
			//listItem.put("modify",
				//	"修改日期：" + dateFormat.format(new Date(modTime)));

			listItems.add(listItem);

		}

		// 定义一个SimpleAdapter
		SimpleAdapter adapter = new SimpleAdapter(
				SDCardFileExplorerActivity.this, listItems, R.layout.list_item,
				new String[] { "filename", "icon", "modify" }, new int[] {
						R.id.file_name, R.id.icon, R.id.file_modify });

		// 填充数据集
		lvFiles.setAdapter(adapter);

		try {
			tvpath.setText("当前路径为:" + fc.file.getCanonicalPath());
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