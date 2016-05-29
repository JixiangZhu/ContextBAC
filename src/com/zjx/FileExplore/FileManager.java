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
 * 文件管理类，负责实现机密文件权限的添加以及remove
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
	// 记录当前的父文件夹
	File currentParent;

	// 记录当前路径下的所有文件夹的文件数组
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
		
		lvFiles.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id)
			{
				builder.setItems(new String[] {"设置为机密文件","取消机密文件","打开文件"}, new OnClickListener()
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
									"取消文件 " + fc.files[position].getName() + "的机密权限"
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
			//remove文件的机密权限
			private void cancelSecret(String path)
			{
				editor.remove(path);
				editor.commit();
			};
			
			//第二层的alertDialog，设置具体的权限级别
			private void alertDialog2(final int position)
			{
				builder2.setItems(new String[] {"权限等级R3","权限等级R2","权限等级R1"}, new OnClickListener()
				{
					public void onClick(DialogInterface dialog,	int which) {
						switch(which)
						{
						case 0: {
							editor.putInt(fc.files[position].getAbsolutePath(),3);
							editor.commit();
							Toast.makeText(FileManager.this,
									"设置文件 " + fc.files[position].getName() + "为机密文件,权限级别为最高R3"
									, Toast.LENGTH_SHORT).show();
						}
						break;
						case 1: {
							editor.putInt(fc.files[position].getAbsolutePath(),2);
							editor.commit();
							Toast.makeText(FileManager.this,
									"设置文件 " + fc.files[position].getName() + "为机密文件,权限级别为一般R2"
									, Toast.LENGTH_SHORT).show();
						}
						break;
						case 2: {
							editor.putInt(fc.files[position].getAbsolutePath(),1);
							editor.commit();
							Toast.makeText(FileManager.this,
									"设置文件 " + fc.files[position].getName() + "为机密文件,权限级别为最低R1"
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
							"当前路径不可访问或者该路径下没有文件", Toast.LENGTH_LONG).show();
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
//	//监听返回键事件
//	public boolean onKeyDown(int keyCode,KeyEvent event)
//	{
//		if(keyCode == KeyEvent.KEYCODE_BACK)
//		{
//			try {
//
//				if (!fc.file.getCanonicalPath().equals("/mnt/sdcard")) {
//
//					// 获取上一级目录
//					fc.file = fc.file.getParentFile();
//					// 列出当前目录下的所有文件
//					fc.files = fc.file.listFiles();
//					// 再次更新ListView
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
	 * 根据文件夹填充ListView
	 * 
	 * @param files
	 */
	private void inflateListView(FileClass fc2) {

		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();

		for (int i = 0; i < fc2.files.length; i++) {

			Map<String, Object> listItem = new HashMap<String, Object>();
			int value = 0;
			File f = fc2.files[i];
			//如果目标为文件夹，则显示图标为文件夹，否则为文件。
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
			// 添加一个文件名称
			listItem.put("filename", fc2.files[i].getName());

			File myFile = new File(fc2.files[i].getName());

			// 获取文件的最后修改日期
			long modTime = myFile.lastModified();
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");


			// 添加一个最后修改日期
			listItem.put("modify",
					"修改日期：" + dateFormat.format(new Date(modTime)));

			listItems.add(listItem);

		}

		// 定义一个SimpleAdapter
		SimpleAdapter adapter = new SimpleAdapter(
				FileManager.this, listItems, R.layout.list_item,
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
}
