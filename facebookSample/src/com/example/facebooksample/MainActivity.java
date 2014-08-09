package com.example.facebooksample;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.android.Facebook;
import com.facebook.model.GraphUser;

public class MainActivity extends Activity implements OnClickListener {

	ImageView mImage; // 이미지 컨텐츠.
	EditText mText; // 텍스트 컨텐츠.
	Button mCamera, mGallery, mSubmit; // 카메라,갤러리,페북쓰기 버튼.
	
	private Uri mImageCaptureUri; 		//원본 이미지를 담는 URI
	ContentsData mContentsData = new ContentsData();
	
	public final static int DONE_GALLERY = 0x02000;
	public final static int DONE_CAMERA = 0x02001;
	
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
    private boolean pendingPublishReauthorization = false;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		FaceBookUtil.SessionOpen(this, (TextView)findViewById(R.id.welcome));
		
		initView();
	}

	private void initView() {
		mImage = (ImageView) findViewById(R.id.img_content);
		mText = (EditText) findViewById(R.id.text_content);

		mCamera = (Button) findViewById(R.id.camera);
		mGallery = (Button) findViewById(R.id.gallery);
		mSubmit = (Button) findViewById(R.id.submit);
		
		mCamera.setOnClickListener(this);
		mGallery.setOnClickListener(this);
		mSubmit.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.camera:
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, PictureUtil.getTempImageUri(true));
			startActivityForResult(intent, DONE_CAMERA);
			break;
		case R.id.gallery:
			Intent pickImageIntent = new Intent(Intent.ACTION_PICK);
			pickImageIntent.setType("image/*");
			startActivityForResult(pickImageIntent, DONE_GALLERY);
			break;
		case R.id.submit:
			mContentsData.setTextContents(mText.getText().toString().trim());
			if(mContentsData.getImageURI() == null && mContentsData.getTextContents().length() == 0){
				Toast.makeText(MainActivity.this, "전송할수없습니다. 내용을 입력해 주세요.", Toast.LENGTH_SHORT).show();
			}else{
				FaceBookUtil.ShareToFaceBook(MainActivity.this,mContentsData);
			}
			break;

		}

	}
	
    byte[] imgbytes = null;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case DONE_GALLERY : {
			if(resultCode == RESULT_OK) {
				try {
					// 단말기 특성에 따라 null로 오는 경우 Crop을 하지 않고 이미지 경로를 바로 보내준다.
					if(data.getData() != null) {
						mImageCaptureUri = data.getData();
					}
						File original_file = PictureUtil.getImageFile(MainActivity.this, mImageCaptureUri);
						String imagePath = original_file.getAbsolutePath();
						mContentsData.setImageFile(new File(imagePath));
						mContentsData.setImageURI(Uri.fromFile(mContentsData.getImageFile()));
						mImage.setImageURI(mContentsData.getImageURI());
						
				} catch (Exception e) {
					e.printStackTrace();

					AlertDialog.Builder altMessage = new AlertDialog.Builder(MainActivity.this);
					altMessage.setCancelable(true);
					altMessage.setTitle("알 림");
					altMessage.setMessage("실패했습니다.");
					altMessage.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					altMessage.show();
				}
			}
		}
			break;
			
		case DONE_CAMERA : {
			if(resultCode == RESULT_OK) {
				Uri tempImageUri = PictureUtil.getTempImageUri(false);
				PictureUtil.correctCameraOrientation(new File(tempImageUri.getPath()), tempImageUri.getPath(), false);
				if(tempImageUri.getPath() != null || !tempImageUri.getPath().equals("")) {
					mContentsData.setImageFile(new File(tempImageUri.getPath()));
					mContentsData.setImageURI(Uri.fromFile(mContentsData.getImageFile()));
					mImage.setImageURI(mContentsData.getImageURI());
				}
			}
		}
			break;

		}
		super.onActivityResult(requestCode, resultCode, data);
		
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	


}
