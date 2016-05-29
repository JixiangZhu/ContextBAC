package com.zjx.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zjx.DialManager.BlockMain;
import com.zjx.FileExplore.FileExplorer;
import com.zjx.FileExplore.FileManager;
import com.zjx.crowd.speakerCount;
import com.zjx.featureExtraction.mfccFeatureExtraction;
import com.zjx.frontCamera.FrontCameraActivity;

//import zjx.javax.sound.sampled.AudioSystem;

/**
 * @author zjx
 * 
 */

public class MainActivity extends Activity {
	LocationManager locManager;
	Time time = new Time();
	TimerTask timertask;
	Timer timer = new Timer();
	int start, end, clock;
	public boolean isOverTime;
	public boolean isOK = false;
	public float accuracy;
	// private Handler handler;

	public Instances unlabeled;
	public Instances labeled;
	public Button startbt, setfileauthoritybt, viewfilebt, dialbt,
			facedetectionbt;
	public FrameLayout scenepicture;
	public ImageView picture;
	public TextView scenetitle, scenename, crowdtitle, crowdvalue,
			secureleveltitle, securelevelvalue;
	public AudioRecord maudiorecorder;
	public String scene;
	public int crowd = -1;
	public String audioname;
	public String resultfilename;
	public File extractionresult;
	// public MFCC m_mfcc;
	public mfccFeatureExtraction mfccfe;
	public speakerCount speakercount;
	public double[] mfcc;

	public short[] inputSignal;
	public float[] framedSignal;
	public boolean b_stop = false;
	public int is_indoor = 1;
	public double clsLabel;

	// audio
	// 音频获取源
	private int audioSource = MediaRecorder.AudioSource.MIC;
	// 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
	private static int sampleRateInHz = 8000;
	// 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
	private static int channelConfig = AudioFormat.CHANNEL_IN_MONO;
	// 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
	private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
	// 缓冲区字节大小
	private int bufferSizeInBytes;
	byte[] b;
	// byte[] audiodata = new byte[bufferSizeInBytes];
	private boolean isRecord = false;// 设置正在录制的状态
	private boolean haveread = false;

	// AudioName裸音频数据文件
	private static final String AudioName = "myRecord.raw";
	// NewAudioName可播放的音频文件
	private static final String NewAudioName = "myRecord.wav";
	private File sdDir = Environment.getExternalStorageDirectory();

	// private static final String AudioName = "/sdcard/love.raw";
	// protected final static int frameLength = 512;
	/**
	 * Number of overlapping samples (usually 50% of frame length)
	 */
	// protected final static int shiftInterval = frameLength / 2;

	// end

	public enum SecureLevel {
		S1, S2, S3
	};

	public SecureLevel securelevel;

	@SuppressLint("ShowToast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		startbt = (Button) findViewById(R.id.start);
		facedetectionbt = (Button) findViewById(R.id.face_detection);
		setfileauthoritybt = (Button) findViewById(R.id.authority_set);
		viewfilebt = (Button) findViewById(R.id.view_file);
		dialbt = (Button) findViewById(R.id.dial);
		scenepicture = (FrameLayout) findViewById(R.id.scene_picture);
		picture = (ImageView) findViewById(R.id.picture);
		scenetitle = (TextView) findViewById(R.id.scene_title);
		scenename = (TextView) findViewById(R.id.scene_name);
		crowdtitle = (TextView) findViewById(R.id.crowd_title);
		crowdvalue = (TextView) findViewById(R.id.crowd_value);
		secureleveltitle = (TextView) findViewById(R.id.secure_level_title);
		securelevelvalue = (TextView) findViewById(R.id.secure_level_value);

		mfccfe = new mfccFeatureExtraction();
		speakercount = new speakerCount();
		securelevel = SecureLevel.S3;
		// TODO Auto-generated method stub
		time.setToNow();
		Log.d("GPS", "GPS running");
		start = time.second;
		// 创建LocationManager对象
		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// 从GPS获取最近的最近的定位信息
		// Location location = locManager.getLastKnownLocation(
		// LocationManager.GPS_PROVIDER);
		// 使用location根据EditText的显示
		// updateView(location);
		// 设置每1秒获取一次GPS的定位信息

		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
				8, new LocationListener() {
					public void onLocationChanged(Location location) {
						// 当GPS定位信息发生改变时，更新位置
						updateView(location);
						if (!Judge())
							is_indoor = 0;
						else
							is_indoor = 1;
						// accuracy = newLocation.getAccuracy();
						time.setToNow();
						end = time.second;
					}

					@SuppressLint("ShowToast")
					@Override
					public void onProviderDisabled(String provider) {
						// TODO Auto-generated method stub
						updateView(null);
						Toast.makeText(getApplicationContext(),
								"GPS Provider Disabled", 1000);
					}

					@SuppressLint("ShowToast")
					@Override
					public void onProviderEnabled(String provider) {
						// TODO Auto-generated method stub
						locManager.getLastKnownLocation(provider);
						updateView(locManager.getLastKnownLocation(provider));
					}

					private void updateView(Location newLocation) {
						// TODO Auto-generated method stub
						if (newLocation != null) {
							accuracy = newLocation.getAccuracy();
						} else {
							// 如果传入的Location对象为空则清空EditText
							Toast.makeText(getApplicationContext(),
									"Get New Location Failed", 1000);
						}
					}

					@SuppressLint("ShowToast")
					@Override
					public void onStatusChanged(String provider, int status,
							Bundle extras) {
						// TODO Auto-generated method stub

					}
				});

		startbt.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (b_stop)
					try {
						stop();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				else
					start();

			}
		});
		// Face detection
		facedetectionbt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.v("NBA","***" );
				startActivity(new Intent(MainActivity.this,
						FrontCameraActivity.class));
				Log.v("NBA","***2" );
			}
		});
		// View Files
		viewfilebt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,
						FileExplorer.class);
				Bundle bundle = new Bundle();
				bundle.putInt("securevalue", securelevel.ordinal());
				Log.d("securelevel",
						"current securevalue is " + securelevel.ordinal());

				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		setfileauthoritybt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, FileManager.class);
				Bundle bundle = new Bundle();
				bundle.putInt("securevalue", securelevel.ordinal());
				Log.d("securelevel",
						"current securevalue is " + securelevel.ordinal());

				intent.putExtras(bundle);
				startActivity(intent);
			}

		});
		dialbt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, BlockMain.class);
				Bundle bundle = new Bundle();
				// bundle.putInt("securelevel", securelevel.hashCode());
				bundle.putInt("securevalue", securelevel.ordinal());
				Log.d("securelevel",
						"current securevalue is " + securelevel.ordinal());
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});

		timertask = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				clock = clock + 1;
				// String s = clock + "";
				// Log.v("NBA",s);
			}
		};
		timer.schedule(timertask, 0, 1000);

	}

	// start button was clicked
	private void start() {
		b_stop = true;
		startbt.setText("停止");
		if (maudiorecorder == null) {
			// 获取缓冲区大小
			bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
					channelConfig, audioFormat);
			// Log.d("size", "bufferSizeInBytes = "+ bufferSizeInBytes);
			// 创建AudioRecord对象
			maudiorecorder = new AudioRecord(audioSource, sampleRateInHz,
					channelConfig, audioFormat, bufferSizeInBytes);
			// 开始录音
			maudiorecorder.startRecording();
			isRecord = true;
			// 开启获取音频数据线程
			new Thread(new WriteAudioRecordDataThread()).start();

		}

	}

	// start button was clicked again,which means stop
	private void stop() throws Exception {
		// TODO Auto-generated method stub
		b_stop = false;
		startbt.setText("开始");
		if (maudiorecorder != null) {
			isRecord = false; // 停止文件写入

			maudiorecorder.stop();
			maudiorecorder.release();
			maudiorecorder = null;
		}

		// add recording
		File rawfile = new File(sdDir, audioname + ".raw");
		//File wavfile = new File(sdDir, audioname + ".wav");
		// File wavfile = new File(sdDir, NewAudioName );
		//copyWaveFile(rawfile, rawfile);

		// read audio data thread
		// inputSignal = preProcessRecording(wavfile);
		readAudioDataFromExistFile();

		// extract audio feature
		while (haveread == true) {
			mfcc = mfccfe.process(inputSignal, sampleRateInHz);

			for (int i = 0; i < mfcc.length; i++) {
				Log.d("mfcc", "mfcc[" + i + "]" + "=" + mfcc[i]);
			}

			crowd = speakercount.Count(inputSignal, sampleRateInHz);

			if (crowd != -1) {
				unlabeled = Generatunlabeleddata(mfcc, is_indoor);

				// save unlabeled
				/*
				 * File unlabeleddata = new File(sdDir, audioname); ArffSaver
				 * saver = new ArffSaver(); saver.setInstances(unlabeled);
				 * saver.setFile(unlabeleddata); saver.writeBatch();
				 */

				unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
				clsLabel = BayesNet(unlabeled);

				Log.d("clsLabel", "clsLabel is " + clsLabel);
				scene = labeled.classAttribute().value((int) clsLabel);
				Log.d("scene", "scene is " + scene);
				securelevel = judgeSecureLevel(clsLabel, crowd);
				displayResultToMainActivity();
			}

			try {
				WriteResult();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("WriteResult", "WriteResult Failed");
				e.printStackTrace();
			}

			haveread = false;
		}

	}

	private void readAudioDataFromExistFile() {
		// TODO Auto-generated method stub
		File file = new File(sdDir, audioname + ".raw");

		// audioInputStream = AudioSystem.getAudioInputStream(file);
		try {
			FileInputStream fis = new FileInputStream(file);
			b = new byte[fis.available()];
			Log.d("bsize", "bsize = " + b.length);
			while (fis.read(b) != -1) {
				getInputSignal();
			}
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		haveread = true;
		// delete the recording
		if (file.exists()) {
			file.delete();
		}
	}

	private void getInputSignal() {
		//convert byte[2] to short
		int i;
		inputSignal = new short [b.length/2];
		for ( i = 0;i< b.length/2;i++ ) {
			inputSignal[i] = (short) ((b[i*2] & 0xff) | (b[i*2+1]<<8) & 0xff00);
		}
	}

	
	

	private SecureLevel judgeSecureLevel(double clslabel, int crowd2) {
		// TODO Auto-generated method stub
		SecureLevel securelevel;
		// initialize;
		securelevel = SecureLevel.S1;
		switch ((int) clslabel) {
		case 0:
			if (crowd2 > 1)
				securelevel = SecureLevel.S1;
			else
				securelevel = SecureLevel.S2;
			break;
		case 1:
			securelevel = SecureLevel.S3;
			break;
		case 2:
			if (crowd2 > 1)
				securelevel = SecureLevel.S1;
			else
				securelevel = SecureLevel.S2;
			break;
		case 3:
			if (crowd2 > 1)
				securelevel = SecureLevel.S1;
			else
				securelevel = SecureLevel.S2;
			break;
		case 4:
			if (crowd2 > 1)
				securelevel = SecureLevel.S2;
			else
				securelevel = SecureLevel.S3;
			break;
		case 5:
			if (crowd2 > 1)
				securelevel = SecureLevel.S1;
			else
				securelevel = SecureLevel.S2;
			break;
		case 6:
			if (crowd2 > 1)
				securelevel = SecureLevel.S1;
			else
				securelevel = SecureLevel.S2;
			break;
		case 7:
			if (crowd2 > 1)
				securelevel = SecureLevel.S1;
			else
				securelevel = SecureLevel.S2;
			break;
		case 8:
			if (crowd2 > 1)
				securelevel = SecureLevel.S1;
			else
				securelevel = SecureLevel.S2;
			break;
		}

		return securelevel;
	}

	private void displayResultToMainActivity() {
		// TODO Auto-generated method stub
		scenename.setText(scene);
		crowdvalue.setText(String.valueOf(crowd));
		int flag = (int) clsLabel;
		Log.d("flag", "flag is " + flag);
		switch (flag) {
		case 0:
			picture.setImageResource(R.drawable.classroom);
			break;
		case 1:
			picture.setImageResource(R.drawable.quietroom);
			break;
		case 2:
			picture.setImageResource(R.drawable.train_station);
			break;
		case 3:
			picture.setImageResource(R.drawable.markethall);
			break;
		case 4:
			picture.setImageResource(R.drawable.square);
			break;
		case 5:
			picture.setImageResource(R.drawable.street);
			break;
		case 6:
			picture.setImageResource(R.drawable.bus);
			break;
		case 7:
			picture.setImageResource(R.drawable.metro);
			break;
		case 8:
			picture.setImageResource(R.drawable.train);
			break;
		}
		securelevelvalue.setText(securelevel.name());
	}

	class WriteAudioRecordDataThread implements Runnable {

		@SuppressLint("SimpleDateFormat")
		@Override
		public void run() {
			// TODO Auto-generated method stub

			FileOutputStream fos = null;
			int readsize = 0;

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
			String currentDateandTime = sdf.format(new Date());
			audioname = currentDateandTime;
			File file = new File(sdDir, audioname + ".raw");
			if (file.exists()) {
				file.delete();
			}
			try {
				fos = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // 建立一个可存取字节的文件

			byte[] audiodata = new byte[bufferSizeInBytes];
			while (isRecord == true) {

				readsize = maudiorecorder.read(audiodata, 0, bufferSizeInBytes);
				if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
					try {
						fos.write(audiodata);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param mfcc2
	 *            MFCC values
	 * @param is_indoor2
	 *            is_indoor values
	 * @return unlabeled Instances
	 */
	private Instances Generatunlabeleddata(double[] mfcc2, int is_indoor2) {
		// TODO Auto-generated method stub
		FastVector atts;
		Instances data;
		double[] vals;
		int i;
		int numMFCC = 13;
		// 1. set up attributes
		atts = new FastVector();
		// - numeric
		for (int j = 0; j < numMFCC; j++) {
			atts.addElement(new Attribute("MFCC" + j));
		}
		// - numeric
		atts.addElement(new Attribute("Is_indoor"));

		// - nominal
		FastVector values = new FastVector(9);
		values.addElement("Classroom");
		values.addElement("QuietRoom");
		values.addElement("TrainStation");
		values.addElement("MarketHall");
		values.addElement("Square");
		values.addElement("Street");
		values.addElement("Bus");
		values.addElement("Metro");
		values.addElement("Train");
		atts.addElement(new Attribute("Scene", values));

		// 2. create Instances object
		data = new Instances("jAudio", atts, 0);

		// 3. fill with data
		// first instance
		vals = new double[data.numAttributes()];
		// - numeric
		for (i = 0; i < numMFCC; i++) {
			vals[i] = mfcc2[i];
		}
		vals[i] = is_indoor2;
		// vals[i + 1] = values.indexOf("Street");

		// add
		data.add(new Instance(1.0, vals));
		data.setClassIndex(data.numAttributes() - 1);
		// Log.d("data.numInstances", "number of instances is" +
		// data.numInstances());
		return data;

	}

	
	public static double findMaximumSampleValue(int bit_depth) {
		int max_sample_value_int = 1;
		for (int i = 0; i < (bit_depth - 1); i++)
			max_sample_value_int *= 2;
		max_sample_value_int--;
		double max_sample_value = ((double) max_sample_value_int) - 1.0;
		Log.d("zjx", "MaximumSampleValue is " + max_sample_value);
		return max_sample_value;
	}

	/**
	 * write extraction result
	 * 
	 * @throws IOException
	 */
	@SuppressLint("SimpleDateFormat")
	public void WriteResult() throws IOException {

		resultfilename = audioname + ".txt";
		extractionresult = new File(sdDir, resultfilename);
		FileOutputStream fos = new FileOutputStream(extractionresult);
		PrintWriter out = new PrintWriter(fos);
		// DataOutputStream dos = new DataOutputStream(new
		// FileOutputStream(extractionresult));
		for (int i = 0; i < mfcc.length; i++) {
			/*
			 * dos.writeDouble(mfcc[i]); dos.flush(); dos.close();
			 */
			out.println(mfcc[i]);
		}
		out.close();
	}

	/**
	 * classify and precise using BayesNet
	 * 
	 * @param unlabeled
	 *            Instances
	 * @return labeled Instances
	 */
	private double BayesNet(Instances unlabeled) {
		try {
			// deserialize model
			Classifier cls = (Classifier) weka.core.SerializationHelper
					.read(getResources().openRawResource(R.raw.bayesnet));

			// Log.d("Classifier", "Classifier loaded");

			// classify unlabled data
			unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
			labeled = new Instances(unlabeled);

			clsLabel = cls.classifyInstance(unlabeled.instance(0));
			labeled.instance(0).setClassValue(clsLabel);
			// String scene = new String();
			// scene = labeled.classAttribute().value((int) clsLabel);
			// Log.d("SceneLabel", "Scene label is" + clsLabel);
			// Log.d("SceneName", "Scene name is" +
			// labeled.classAttribute().value((int) clsLabel));

			return clsLabel;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("Not classified", "An Error has occured");
			e.printStackTrace();
		}
		return 0;

	}

	private boolean Judge() {
		// TODO Auto-generated method stub
		int peroid = end - start;
		if (peroid < 0)
			peroid = peroid + 60;
		if (peroid < 10 && accuracy < 100)
			isOK = true;
		return isOK;
	}

	// Exit
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			System.exit(0);
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
