package com.example.facebooksample;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

public class FaceBookUtil {
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
    private static boolean pendingPublishReauthorization = false;
    private static ProgressDialog m_dlgProgress = null;
    
    public static void SessionOpen(Activity act,final TextView tv){
    	Session.openActiveSession(act, true, new Session.StatusCallback() {

		      // callback when session changes state
		      @Override
		      public void call(Session session, SessionState state, Exception exception) {
		        if (session.isOpened()) {

		          // make request to the /me API
		          Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

		            // callback after Graph API response with user object
		            @Override
		            public void onCompleted(GraphUser user, Response response) {
		              if (user != null) {
		                tv.setText("Hello " + user.getName() + "!");
		              }
		            }
		          });
		        }
		      }
		    });
    }
    
	public static void ShareToFaceBook(final Context con,ContentsData data){
		 Session session = Session.getActiveSession();

	       if (session != null) {

	           // Check for publish permissions
	           List<String> permissions = session.getPermissions();
	           if (!isSubsetOf(PERMISSIONS, permissions)) {
	               pendingPublishReauthorization = true;
	               Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest((Activity) con, PERMISSIONS);
	               session.requestNewPublishPermissions(newPermissionsRequest);
	               return;
	           }

	           Bundle postParams = new Bundle();
	           
	           if(data.getImageFile() != null){
	           	postParams.putString("name", data.getTextContents());
	           	postParams.putByteArray("picture", convertFileToByteArray(data.getImageFile()));
	           }else{
	           	postParams.putString("name", data.getTextContents());
		            postParams.putString("caption",data.getTextContents());
		            postParams.putString("description", data.getTextContents());
		            postParams.putString("message", data.getTextContents());
		            postParams.putString("link", "");
		            postParams.putString("picture", "");
	           }
	           Request.Callback callback = new Request.Callback() {
	               public void onCompleted(Response response) {
	                   Log.e("", "response" + response.toString());
	                   if (response != null) {
	                       JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
	                       String postId = null;
	                       try {
	                           postId = graphResponse.getString("id");
	                       } catch (JSONException e) {
	                           Log.i("","JSON error " + e.getMessage());
	                       }
	                       FacebookRequestError error = response.getError();
	                       if (error != null) {
	                           Toast.makeText(con,error.getErrorMessage(), Toast.LENGTH_SHORT).show();
	                       } else {
	                    	   if(m_dlgProgress.isShowing()){
	                    		   m_dlgProgress.dismiss();
	                    	   }
	                           Toast.makeText(con,postId,Toast.LENGTH_LONG).show();
	                       }
	                   }
	               }
	           };
	           Request request;
	           
	           if(data.getImageFile() != null){
	           	request = new Request(session, "me/photos", postParams,HttpMethod.POST, callback);
	           }else{
	           	 request = new Request(session, "me/feed", postParams, HttpMethod.POST, callback);
	           }
	           RequestAsyncTask task = new RequestAsyncTask(request);
	           task.execute();
	           if(m_dlgProgress == null) {
					m_dlgProgress = new ProgressDialog(con);
				}
				if(!m_dlgProgress.isShowing()) {
					m_dlgProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					m_dlgProgress.setCancelable(false);
					m_dlgProgress.setMessage("담벼락에 작성중입니다.");
					m_dlgProgress.show();
				}
	       }
	}
	
	
	// 로그인 여부 확인
	public static boolean isLogined(){
        Session session = Session.getActiveSession();
        if(session == null)
            return false;
         
        if(!session.isOpened())
            return false;
         
        return true;
    }
	
	public static byte[] convertFileToByteArray(File f)
	 {
	 byte[] byteArray = null;
	 try
	 {
	 InputStream inputStream = new FileInputStream(f);
	 ByteArrayOutputStream bos = new ByteArrayOutputStream();
	 byte[] b = new byte[1024*8];
	 int bytesRead =0;
	 
	 while ((bytesRead = inputStream.read(b)) != -1)
	 {
	 bos.write(b, 0, bytesRead);
	 }
	 
	 byteArray = bos.toByteArray();
	 }
	 catch (IOException e)
	 {
	 e.printStackTrace();
	 }
	 return byteArray;
	 }

   private static boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
       for (String string : subset) {
           if (!superset.contains(string)) {
               return false;
           }
       }
       return true;
   }
	
}
