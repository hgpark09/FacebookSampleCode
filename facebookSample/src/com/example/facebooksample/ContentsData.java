package com.example.facebooksample;

import java.io.File;

import android.net.Uri;

public class ContentsData {
	private Uri mImageURI = null;
	private String mTextContents = "";
	private File mImageFile = null;
	
	public File getImageFile() {
		return mImageFile;
	}
	public void setImageFile(File mImageFile) {
		this.mImageFile = mImageFile;
	}
	public Uri getImageURI() {
		return mImageURI;
	}
	public void setImageURI(Uri mImageURI) {
		this.mImageURI = mImageURI;
	}
	public String getTextContents() {
		return mTextContents;
	}
	public void setTextContents(String mTextCont) {
		this.mTextContents = mTextCont;
	}
}
