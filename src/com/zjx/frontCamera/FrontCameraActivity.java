package com.zjx.frontCamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FrontCameraActivity extends Activity /* implements SurfaceHolder.Callback */{
	public FaceRecognition facerecognition;
	public ImageView iv_image; // image
	public SurfaceView sv; // surface view
	// a surface holder
	private SurfaceHolder sHolder;
	private Camera mCamera;
	private Parameters parameters;
	private Bitmap bmp;

	public int cameraId = 0;
	private FrameLayout layout;
	private FaceView faceView;
	private Preview mPreview;
	private static int position = -1;
	private Intent intent;
	public static boolean alarm;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.zjx.test.R.layout.frontcamera_main);

		try {
			alarm = false;
			intent = getIntent();
			Bundle data = intent.getExtras();
			if(data != null){
			position = data.getInt("Position");
			}
			layout = new FrameLayout(this);
			faceView = new FaceView(this,FrontCameraActivity.this);
			mPreview = new Preview(this, faceView);
			facerecognition = new FaceRecognition();
			if (facerecognition != null)
				faceView.mfacerecognition = facerecognition;
			faceView.cameraid = mPreview.cameraId;

			layout.addView(mPreview);
			layout.addView(faceView);
			setContentView(layout);
		} catch (IOException e) {
			e.printStackTrace();
			new AlertDialog.Builder(this).setMessage(e.getMessage()).create()
					.show();
		}

		iv_image = (ImageView) findViewById(com.zjx.test.R.id.imageView_front);
		View wifibutton = findViewById(com.zjx.test.R.id.button);
		SurfaceView sv = (SurfaceView) findViewById(com.zjx.test.R.id.surfaceView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);

		SubMenu front = menu.addSubMenu("camera switch");
		SubMenu training = menu.addSubMenu("training");
		return true;
	}
	
	 public void onBackPressed() {
		 	if(position == -1) super.onBackPressed();
		 	Intent result = new Intent();
		 	Bundle b = new Bundle();
		 	b.putInt("Position", position);
		 	if(alarm) b.putBoolean("Alarm", true);
		 	else b.putBoolean("Alarm", false);
		 	
//		 	b.remove("Alarm");
//		 	b.putBoolean("Alarm", true);
		 	
		 	result.putExtras(b);
	        setResult(20,result);
	        super.onBackPressed();
	    }
	 
	 public void FinishThis()
	 {
		 	if(position == -1) return;
 		 	Intent result = new Intent();
		 	Bundle b = new Bundle();
		 	b.putInt("Position", position);
		 	if(alarm) b.putBoolean("Alarm", true);
		 	else b.putBoolean("Alarm", false);
		 	
//		 	b.remove("Alarm");
//		 	b.putBoolean("Alarm", true);
		 	
		 	result.putExtras(b);
	        setResult(20,result);
	        finish();
	 }
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		String title = (String) item.getTitle();
		if (title == "camera switch") {
			if (mPreview.cameraId == 0) {
				mPreview.setcamera(1);
				faceView.cameraid = 1;
			} else {
				mPreview.setcamera(0);
				faceView.cameraid = 0;
			}
		}

		if (title == "training")
			faceView.training();

		return true;

	}
}

// ----------------------------------------------------------------------

class FaceView extends View implements Camera.PreviewCallback {
	public static final int SUBSAMPLING_FACTOR = 4;

	private static final String TAG = null;
	
	private IplImage grayImage;
	private CvHaarClassifierCascade classifier;
	private CvMemStorage storage;
	private CvSeq faces;
	public int cameraid;
	public FaceRecognition mfacerecognition;
	public IplImage[] trainingFaceImgArr;
	public int num = 30;
	public int count = 0;
	public boolean settraining = false;
	public int countTime = 0;//Oyang
	private FrontCameraActivity frontCameraActivity;
	
	public FaceView(FrontCameraActivity context, FrontCameraActivity frontCameraActivity) throws IOException {
		super(context);
		this.frontCameraActivity = frontCameraActivity;
		countTime = 0; //Oyang
		trainingFaceImgArr = new IplImage[num];

		// Load the classifier file from Java resources.
		String fileName;
		File extStore = Environment.getExternalStorageDirectory();

		File classifierFile = Loader.extractResource(getClass(),
				"/assets/haarcascade_frontalface_alt.xml",
				context.getCacheDir(), "classifier", ".xml");
		if (classifierFile == null || classifierFile.length() <= 0) {
			throw new IOException(
					"Could not extract the classifier file from Java resource.");
		}

		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);
		String str = classifierFile.getAbsolutePath();

		// fileName = "/assets/haarcascade_frontalface_alt.xml";
		classifier = new CvHaarClassifierCascade(cvLoad(str));
		classifierFile.delete();
		if (classifier.isNull()) {
			throw new IOException("Could not load the classifier file.");
		}
		storage = CvMemStorage.create();
	}

	public void training() {
		count = 0;
		num = 30;
		settraining = true;
	}

	public void onPreviewFrame(final byte[] data, final Camera camera) {//当预览帧可用时会调用此回调算法
		try {
			Camera.Size size = camera.getParameters().getPreviewSize();

//			String str = "2-w:h=" + Integer.toString(size.width) + ":"
//					+ Integer.toString(size.height);

			processImage(data, size.width, size.height);
			camera.addCallbackBuffer(data);
		} catch (RuntimeException e) {
			// The camera has probably just been released, ignore.
		}
	}

	public void processImage(byte[] data, int width, int height) {
		// First, downsample our image and convert it into a grayscale IplImage

		int f = SUBSAMPLING_FACTOR;
		int imageWidth;
		int imageHeight;
		int dataStride;
		int imageStride;

		/*if (width < height) //
		{
			if (grayImage == null || grayImage.width() != width / f
					|| grayImage.height() != height / f) {
				grayImage = IplImage.create(height / f, width / f,
						IPL_DEPTH_8U, 1);
			}
			imageWidth = grayImage.width();
			imageHeight = grayImage.height();
			dataStride = f * imageWidth;
			imageStride = grayImage.widthStep();
			ByteBuffer imageBuffer = grayImage.getByteBuffer();
			for (int y = 0; y < imageHeight; y++) {
				int dataLine = y * dataStride;
				int imageLine = y * imageStride;
				for (int x = 0; x < imageWidth; x++) {
					imageBuffer.put(imageLine + x, data[dataLine + f
							* (imageHeight - x)]);
				}
			}

			Log.v("Vertical", "Vertical position");
		} else */{
			if (grayImage == null || grayImage.width() != width / f
					|| grayImage.height() != height / f) {
				grayImage = IplImage.create(width / f, height / f,
						IPL_DEPTH_8U, 1);
			}
			imageWidth = grayImage.width();
			imageHeight = grayImage.height();
			//Log.v("NBA" ,"width= " + imageWidth + "height=" + imageHeight);
			dataStride = f * width;
			imageStride = grayImage.widthStep();
//			Log.v("NBA", "ImageStride: = " + imageStride + "dataStride: = " + dataStride);
			ByteBuffer imageBuffer = grayImage.getByteBuffer();
			for (int y = 0; y < imageHeight; y++) {
				int dataLine = y * dataStride;
				int imageLine = y * imageStride;
				
				for (int x = 0; x < imageWidth; x++) {
					imageBuffer.put(imageLine + x, data[dataLine + f * x]);
				}
			}
//			int dataStride2 = f * width;
//			int imageStride2 = imageHeight;
//			for(int x = 0; x < imageWidth; x ++ )
//			{
//				int imageLine = x * imageStride2;
//				for( int y = imageHeight - 1; y > -1; y --)
//				{
//					int dataLine2 = y * dataStride2;
//					imageBuffer.put(imageLine + (imageHeight -1) - y,data[dataLine2 + f * x]);
//				}
//			}
		}
		cvClearMemStorage(storage);
		faces = cvHaarDetectObjects(grayImage, classifier, storage, 1.1, 3,
				CV_HAAR_DO_CANNY_PRUNING);
		postInvalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(Color.GREEN);
		paint.setTextSize(30);
		Paint paint2 = new Paint();
		paint2.setColor(Color.RED);
		paint2.setTextSize(100);
		paint2.setStrokeWidth(2);
		paint2.setStyle(Paint.Style.FILL_AND_STROKE);
		String s = "";
		int x = 0, y = 0, w = 0, h = 0;
		// Log.v("onDraw", "coming........................");

		if (faces != null) {
			paint.setStrokeWidth(2);
			paint.setStyle(Paint.Style.STROKE);
			float scaleX = (float) getWidth() / grayImage.width();
			float scaleY = (float) getHeight() / grayImage.height();
			int total = faces.total();
			float textWidth = paint.measureText(s);
			
			for (int i = 0; i < total; i++) {
				Log.v("NBA",i+"**");
				CvRect r = new CvRect(cvGetSeqElem(faces, i));
				/*if (cameraid == 0) {
					x = r.x();
					y = r.y();
					w = r.width();
					h = r.height();
					canvas.drawRect(x * scaleX, y * scaleY, (x + w) * scaleX,
							(y + h) * scaleY, paint);
					s = Integer.toString(i);
					canvas.drawText(s, (x + w / 2) * scaleX, (y + h / 2)
							* scaleY, paint);
				} else */{
					x = grayImage.width() - r.x();
					y = r.y();
					w = r.width();
					h = r.height();
//					canvas.drawRect((x - w) * scaleX, y * scaleY, x * scaleX,
//							(y + h) * scaleY, paint);
					s = Integer.toString(i);
//					canvas.drawText(s, 70, 50, paint);
				
//					canvas.drawText("M",70 * scaleX , 50 * scaleY,paint2);
//					canvas.drawText(s, x * scaleX, y
//							* scaleY, paint);
//					canvas.drawText("S", (x-w)*scaleX, (y)*scaleY, paint);
//					canvas.drawText("M", x * scaleX,(y + h) * scaleY, paint);
				}
			}
			canvas.rotate(90);
			canvas.drawText(faces.total() + "",(float)getHeight()/2 ,0 - (float) getWidth()/2 ,paint2);
			s = faces.total() + " people are in sight";
			canvas.drawText(s, (getHeight() - paint.measureText(s)) / 2, 30 - (float) getWidth() , paint);
//			if (faces.total() != 0)
//				s = "Alarm! Alarm! please shun down secreen!";
			
			/*
			 * 下面两个if语句用于统计检测到人脸数大于1时候的次数，为防止误判，当检测到的次数大于3的时候才判断确实检测到两张人脸。
			 */
			if(faces.total() > 1) {
				countTime  ++;
				Log.v("NAA",countTime + "#######");
			}
			if(countTime > 3){
				FrontCameraActivity.alarm = true;
				Log.v("NAA",countTime + "******");
				frontCameraActivity.FinishThis();
			}
			
		}

	}
}

// ----------------------------------------------------------------------

class Preview extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = null;
	public SurfaceHolder mHolder;
	public Camera mCamera;
	public Camera.PreviewCallback previewCallback;
	public int cameraId;
	public boolean horizontal;

	Preview(Context context, Camera.PreviewCallback previewCallback) {
		super(context);
		this.previewCallback = previewCallback;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		cameraId = findFrontFacingCamera();
		horizontal = false;
	}

	public void setcamera(int id) {
		cameraId = id;

		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
		mCamera = Camera.open(cameraId);
		try {
			mCamera.setPreviewDisplay(mHolder);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.v("NBA","###########");
		Camera.Parameters parameters = mCamera.getParameters();
		mCamera.setDisplayOrientation(90);// zwei
		List<Size> sizes = parameters.getSupportedPreviewSizes();
		// Size optimalSize = getOptimalPreviewSize(sizes, w, h);
		// parameters.setPreviewSize(optimalSize.width, optimalSize.height);

		// String str = "w:h="+ Integer.toString(w) +":"+ Integer.toString(h);
		// Log.v(TAG, str);
		
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			mCamera.setDisplayOrientation(90);// 将镜头旋转90度。
			Log.i("por", "1");
		}

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			parameters.set("orientation", "landscape");
			parameters.set("rotation", 0);
		}
		Log.v("NBA", "LLL");
		
		mCamera.setParameters(parameters);
		if (previewCallback != null) {
			mCamera.setPreviewCallbackWithBuffer(previewCallback);
			Camera.Size size = parameters.getPreviewSize();
			byte[] data = new byte[size.width
					* size.height
					* ImageFormat
							.getBitsPerPixel(parameters.getPreviewFormat()) / 8];
			mCamera.addCallbackBuffer(data);
		}
		mCamera.startPreview();

	}

	public int findFrontFacingCamera() {
		int cameraId = -1;
		// Search for the front facing camera
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				Log.v("MyActivity", "Camera found");
				cameraId = i;
				break;
			}
		}
		return cameraId;
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		// if (cameraId > 0) {
		safeCameraOpen(cameraId);
		// }
		try {
			mCamera.setPreviewDisplay(holder);

		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;
		}
	}

	public void safeCameraOpen(int id) {
		boolean qOpened = false;
		try {
			releaseCamera();
			mCamera = Camera.open(id);
			Log.v("NBA",id + "****");
			qOpened = (mCamera != null);
			Log.v("NBA",qOpened + "***dd*");
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();
		// mCamera.setDisplayOrientation(90);//zwei

		List<Size> sizes = parameters.getSupportedPreviewSizes();
		Size optimalSize = getOptimalPreviewSize(sizes, w, h);
		parameters.setPreviewSize(optimalSize.width, optimalSize.height);

		if (w > h)
			horizontal = true;
		else
			horizontal = false;
		String str = "1-w:h=" + Integer.toString(w) + ":" + Integer.toString(h);
		
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			mCamera.setDisplayOrientation(90);// 将镜头旋转90度。
			Log.i("por", "1");
		}

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			parameters.set("orientation", "landscape");
			parameters.set("rotation", 0);
		}

		mCamera.setParameters(parameters);
		if (previewCallback != null) {
			mCamera.setPreviewCallbackWithBuffer(previewCallback);
			Camera.Size size = parameters.getPreviewSize();
			byte[] data = new byte[size.width
					* size.height
					* ImageFormat
							.getBitsPerPixel(parameters.getPreviewFormat()) / 8];
			mCamera.addCallbackBuffer(data);
		}
		mCamera.startPreview();
	}

}
