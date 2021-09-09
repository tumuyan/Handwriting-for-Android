package com.example.softwaretest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class BaseApplication extends Application {

	public static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		context = this;

		try {
			copyFile(context,"writableZHCN.dic",false);
			copyFile(context,"writableZHCN.dic-journal",false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void copyFile(Context context, String fileName, boolean overwrite) {
		if (fileName == null) return;

		File f = context.getFilesDir();
		if(!f.exists())
			f.mkdir();
		File file = new File(f,fileName);
		if (file.exists() && !overwrite) return;

		final String targetFileName =file.getPath();

		Log.i("copyFile",targetFileName);

		final AssetManager assetManager = context.getAssets();
		try (InputStream in = assetManager.open(fileName);
			 final FileOutputStream out = new FileOutputStream(targetFileName)) {
			final byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static Context getContext() {
		return context;
	}

	public static String composeLocation(String fileName) {
		File f = context.getFilesDir();
		if(!f.exists())
			f.mkdir();
		File file = new File(f,fileName);
		Log.i("path",file.getAbsolutePath());
		return file.getAbsolutePath();
	}

	public static String libPath(String fileName) {
		File f = context.getFilesDir();
		if(!f.exists())
			f.mkdir();

		File file = new File(f.getParent(),"lib"+File.separator+fileName);
		Log.i("libpath",file.getAbsolutePath());
		return file.getAbsolutePath();
	}

}
