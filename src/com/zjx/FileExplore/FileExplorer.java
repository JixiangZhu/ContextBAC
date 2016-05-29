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
 * 负责对文件进行查看以及限制用户查看权限
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
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { 
		   case 20:
		    Bundle bundle=data.getExtras(); //data为B中回传的Intent
		    boolean alarm=bundle.getBoolean("Alarm");//str即为回传的值
		    secure_value = bundle.getInt("securevalue");
			Log.d("securevalue", "securevalue is " + secure_value);
			
			SetSafeClass(secure_value);
			
		    final int position=bundle.getInt("Position");
		    Log.v("NBA", "Alarm := " + alarm );
		    Log.v("NBA","Position := " + position);
		    //如果发出报警则对用户进行提醒，否则就直接打开相应的文件或者是目录
		    if(alarm) {
		    	final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		    	builder.setTitle("Alarm!");
		    	builder.setMessage("有人在偷看，请注意");
		    	builder.setPositiveButton("取消打开", new OnClickListener()
		    	{

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						inflateListView(fc);
						return;
						
					}
		    		
		    	});
		    	builder.setNegativeButton("依然打开", new OnClickListener()
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
//				if (isBlock(fc.files[position].getAbsolutePath())&&!isSafe())
				if(!canRead(fc.files[position].getAbsolutePath()))
				{
					Toast.makeText(FileExplorer.this, "此环境下不宜打开此文件", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if(getFileClass(fc.files[position].getAbsolutePath()) == 3)
					//这里自动跳转到监控activity。
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
							"打开文件功能还没完善，敬请期待下一版本", Toast.LENGTH_LONG).show();
					return;
				}
				// 判断文件权限
				OpenDocument(position);
//				// 获取用户点击的文件夹 下的所有文件
//				File[] tem = fc.files[position].listFiles();
//				if (tem == null || tem.length == 0) {
//
//					Toast.makeText(FileExplorer.this,
//							"当前路径不可访问或者该路径下没有文件", Toast.LENGTH_LONG).show();
//				}
//				else 
//				{
//					// 获取用户单击的列表项对应的文件夹，设为当前的父文件夹
//					fc.file = fc.files[position];
//					// 保存当前的父文件夹内的全部文件和文件夹
//					fc.files = tem;
//					// 再次更新ListView
//					inflateListView(fc);
//				}
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
	
	public void OpenDocument(int position){
		if (fc.files[position].isFile()) {
			// 也可自定义扩展打开这个文件等
			Toast.makeText(FileExplorer.this,
					"打开文件功能还没完善，敬请期待下一版本", Toast.LENGTH_LONG).show();
			return;
		}
		File[] tem = fc.files[position].listFiles();
		if (tem == null || tem.length == 0) {

			Toast.makeText(FileExplorer.this,
					"当前路径不可访问或者该路径下没有文件", Toast.LENGTH_LONG).show();
		}
		else 
		{
			// 获取用户单击的列表项对应的文件夹，设为当前的父文件夹
			fc.file = fc.files[position];
			// 保存当前的父文件夹内的全部文件和文件夹
			fc.files = tem;
			// 再次更新ListView
			inflateListView(fc);
		}
	}
	//监听返回键事件
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
	 * 根据文件夹填充ListView
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
				FileExplorer.this, listItems, R.layout.list_item,
				new String[] { "filename", "icon", "modify" }, new int[] {
						R.id.file_name, R.id.icon, R.id.file_modify });

		// 填充数据集
		lvFiles.setAdapter(adapter);

		try {
			tvpath.setText("当前路径为:" + fc.file.getCanonicalPath());
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * 判断文件是否可以被读取
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
	 * 获得文件的限制级别
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
