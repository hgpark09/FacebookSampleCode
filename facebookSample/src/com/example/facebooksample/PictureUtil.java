package com.example.facebooksample;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;

public class PictureUtil {
	private static String path;
	private static Uri mTempImageUri = null;
	private static String mImagePath;
	private static Context mContext;
	private static int mImageSizeBoundary = 1000;
	public static String mainImagePath;
	
	/**
	 * 이미지 경로에서 파일을 반환함
	 * @param uri
	 * @return
	 */
	public static File getImageFile(Context ctx, Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		if (uri == null) {
			uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		}

		Cursor mCursor = ctx.getContentResolver().query(uri, projection, null, null, 
				MediaStore.Images.Media.DATE_MODIFIED + " desc");
		if(mCursor == null || mCursor.getCount() < 1) {
			return null; // no cursor or no record
		}
		int column_index = mCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		mCursor.moveToFirst();

		String path = mCursor.getString(column_index);

		if (mCursor !=null ) {
			mCursor.close();
			mCursor = null;
		}

		return new File(path);
	}
	
	public static byte[] resizeImage(Context ctx, File file) {
		Uri uri = Uri.fromFile(file);
		Bitmap bitmap;
		try {
			bitmap = Images.Media.getBitmap(ctx.getContentResolver(), uri);
//			Bitmap bitmapReSize = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/5, bitmap.getHeight()/5, false);
			Bitmap bitmapReSize = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
			ByteArrayOutputStream blob = new ByteArrayOutputStream();
			bitmapReSize.compress(CompressFormat.PNG, 0, blob);

			return blob.toByteArray();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	
	public static class CorrectOrientationTask extends AsyncTask<String, Integer, Integer> {
		protected Integer doInBackground(String... paths) {
			int count = paths.length;
			mainImagePath = paths[0];
			for (String path : paths) {
				correctCameraOrientation(new File(path), path, false);
			}
			return count;
		}

		protected void onProgressUpdate(Integer... progress) {
		}

		@Override
		protected void onPreExecute() {
			//여기다가는 로딩중 다열로그.
			super.onPreExecute();
		}

		protected void onPostExecute(Integer result) {
			//다열로그 숨김.
			//이미지 path를 보내주자.
			
			super.onPostExecute(result);
		}
	}
	
	// 카메라로 사진을 찍었을때 90도로 회전되서 저장되는 경우가 있음. 
		// 항상 같은 방향으로 저장 해야함.
		public static Bitmap correctCameraOrientation(File imagefile, String path, boolean useOriginal) {

	        try {
	            Bitmap bitmap;
	            if (useOriginal) {
	                bitmap = BitmapFactory.decodeFile(imagefile.getAbsolutePath());
	            } else {
	                bitmap = loadImageWithSampleSize(imagefile.getAbsolutePath());
	            }
	            ExifInterface exif = new ExifInterface(imagefile.getAbsolutePath());
	            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
	            int exifRotateDegree = exifOrientationToDegrees(exifOrientation);
	            Bitmap rotatedIamge = rotateImage(bitmap, exifRotateDegree);

	            try {
	                File file = new File(path);
	                FileOutputStream out = null;
	                file.createNewFile();
	                out = new FileOutputStream(imagefile);
	                rotatedIamge.compress(CompressFormat.JPEG, 80, out);
	                out.close();

	            } catch (Exception e) {
	                e.printStackTrace();
	            }

	            return rotatedIamge;
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
	
		public static Bitmap rotateImage(Bitmap bitmap, int degrees) {
	        if (degrees != 0 && bitmap != null) {
	            Matrix m = new Matrix();
	            m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
	            try {
	                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
	                if (bitmap != converted) {
	                    bitmap.recycle();
	                    bitmap = converted;
	                }
	            } catch (OutOfMemoryError ex) {
	                ex.printStackTrace();
	            }
	        }
	        return bitmap;
	    }

	    public static int exifOrientationToDegrees(int exifOrientation) {
	        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
	            return 90;
	        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
	            return 180;
	        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
	            return 270;
	        }
	        return 0;
	    }

	  //out of memory가 발생할수있으므로 샘플사이즈를 줘서 용량을 줄이자.
		public static Bitmap loadImageWithSampleSize(String path) {
	        BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inJustDecodeBounds = true;
	        BitmapFactory.decodeFile(path, options);
	        int width = options.outWidth;
	        int height = options.outHeight;
	        int longSide = Math.max(width, height);
	        int sampleSize = 1;
	        if (longSide > mImageSizeBoundary) {
	            sampleSize = longSide / mImageSizeBoundary;
	        }
	        options.inJustDecodeBounds = false;
	        options.inSampleSize = sampleSize;
	        options.inPurgeable = true;
	        options.inDither = false;

	        return BitmapFactory.decodeFile(path, options);
	    }
		
	    private static String getBaseDirPath() {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == true) {
				return Environment.getExternalStorageDirectory().getAbsolutePath() + "/.temp/";
			}
			return mContext.getFilesDir().getAbsolutePath() + "/.temp/";
		}
		
		/* Override this method to use another name */
		protected static String generateTempFileName() {
			return String.valueOf(System.currentTimeMillis());
		}
	    
	    public static Uri getTempImageUri(boolean refresh) {
			if (refresh == true || mTempImageUri == null) {
				String sdPath = getBaseDirPath();
				File fileDir = new File(sdPath);
				if (!fileDir.isDirectory()) {
					fileDir.mkdirs();
				}

				path = "tmp_" + generateTempFileName() + ".jpg";
				mTempImageUri = Uri.fromFile(new File(fileDir, path));
			}
			return mTempImageUri;
		}
}
