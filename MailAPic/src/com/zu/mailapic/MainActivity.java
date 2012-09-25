package com.zu.mailapic;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.Menu;
import android.view.View;
import android.view.ViewDebug.CapturedViewProperty;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends Activity
{

	static  int CAPTURE_IMAGE_REQUEST_CODE = 102;
	private Uri capturedImage;
	
	private int rotate = 90;
	
	private ImageButton image_preview;
	private Button post_email;
	private Bitmap image_bitmap = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		this.image_preview = (ImageButton) this.findViewById(R.id.image_preview);
		this.post_email = (Button) this.findViewById(R.id.send_email);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void sendChosenImage(View eventView)
	{
		if(this.image_bitmap != null)
		{			
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.setType("image/jpeg");
			sendIntent.putExtra(Intent.EXTRA_SUBJECT, "My Pic");
			sendIntent.putExtra(Intent.EXTRA_TEXT, "I thought you might like this");
			sendIntent.putExtra(Intent.EXTRA_STREAM, this.capturedImage);
			sendIntent.setType("message/rfc822");
			startActivity(Intent.createChooser(sendIntent, "Send email"));
		}
	}
	
	
	public void captureNewImage(View eventView)
	{
		try{
			
		
			File outputDir = this.getExternalCacheDir();
			File tempFile = File.createTempFile("capture", ".jpg", outputDir);
			
			this.capturedImage = Uri.fromFile(tempFile);
			
			Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			
			takePicture.putExtra(MediaStore.EXTRA_OUTPUT, this.capturedImage);
			
			startActivityForResult(takePicture, CAPTURE_IMAGE_REQUEST_CODE);
		}
		catch(IOException e)
		{
			System.out.println(e.toString());
		}
	}
	
	
	
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == CAPTURE_IMAGE_REQUEST_CODE)
		{
			if(resultCode == RESULT_OK)
			{
				this.handleReceivedImage(data);
			}
		}
	}
	
	public void handleReceivedImage(Intent data)
	{
		this.image_bitmap = this.getThumbnailBitmap(this.capturedImage, this.image_preview.getHeight(), this.image_preview.getWidth());
		
		this.image_preview.setImageBitmap(this.image_bitmap);
	}
	
	
	
	/***********************************
	 * IGNORE THIS IT'S SCARY
	 ***********************************/
	public Bitmap getThumbnailBitmap(Uri fileUri, int targetHeight, int targetWidth)
	{		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(fileUri.getPath(), options);
		
		int actualWidth = options.outWidth;
		int actualHeight = options.outHeight;
		
		Matrix matrix = new Matrix();
		matrix.postRotate(this.rotate);
		
		float floatScale = Math.min((float)actualWidth/(float)targetWidth, (float)actualHeight/(float)targetHeight);
		
		int scaleFactor = Math.min(actualWidth/targetWidth, actualHeight/targetHeight);
		
		options.inJustDecodeBounds = false;
		options.inSampleSize = scaleFactor;
		options.inPurgeable = true;
		
		Bitmap bTemp = BitmapFactory.decodeFile(fileUri.getPath(), options);
		
//		int rotatedHeight = Math.round(actualWidth/floatScale);
//		int rotatedWidth = Math.round(actualHeight/floatScale);
		
		return Bitmap.createBitmap(bTemp, 0, 0, options.outWidth,options.outHeight, matrix, true);
	}
}
