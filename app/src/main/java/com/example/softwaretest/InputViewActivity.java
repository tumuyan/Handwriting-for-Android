package com.example.softwaretest;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.openwnn.legacy.OnHandWritingRecognize;
import com.googlecode.openwnn.legacy.WnnWord;
import com.googlecode.openwnn.legacy.CLOUDSONG.CandidateView;
import com.googlecode.openwnn.legacy.CLOUDSONG.OnCandidateSelected;
import com.googlecode.openwnn.legacy.CLOUDSONG.OnPinyinQueryed;
import com.googlecode.openwnn.legacy.CLOUDSONG.PinyinQueryResult;
import com.googlecode.openwnn.legacy.handwritingboard.HandWritingBoardLayout;

@SuppressLint("NewApi")
public class InputViewActivity extends Activity implements OnCandidateSelected, OnHandWritingRecognize, OnPinyinQueryed, OnClickListener {

	HandWritingBoardLayout handWritingBoard;
	TextView inputShow;

	LinearLayout container;
	RelativeLayout candidateContainer;
	Button btn_show, btn_hide;
	EditText editText;
	Button btnCleanHandWriting;
	TextView PinYinInput;

	CandidateView mCandidateView;

	private StringBuilder currentInput = new StringBuilder();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input_view_activity);




		container = (LinearLayout) findViewById(R.id.container);
		candidateContainer = (RelativeLayout) findViewById(R.id.candidateContainer);
		handWritingBoard = (HandWritingBoardLayout) findViewById(R.id.handwrtingboard);


		btnCleanHandWriting = (Button) findViewById(R.id.clean);
		PinYinInput = (TextView) findViewById(R.id.pinyinInput);
		inputShow = (TextView) findViewById(R.id.candidateselected);

//		ckManager = new CloudKeyboardInputManager();
//		ckManager.setOnPinyinQueryed(this);
		mCandidateView = new CandidateView(this);
		mCandidateView.setOnCandidateSelected(this);
		RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		lp1.addRule(RelativeLayout.LEFT_OF, R.id.btn_showMore);

		lp1.width = ViewGroup.LayoutParams.MATCH_PARENT;
		candidateContainer.addView(mCandidateView, lp1);
		System.currentTimeMillis();
		handWritingBoard.setOnHandWritingRecognize(this);


		btnCleanHandWriting.setOnClickListener(this);

		handWritingBoard.setVisibility(View.VISIBLE);
//		ckManager.delAll();
		PinYinInput.setText("这是Rime输入法的手写插件测试页面。你可以直接在下方区域测试手写识别。" +
				"\n在文本框内输入数值并点击“显示悬浮窗”按钮，可以调用HWService并打开悬浮窗，悬浮窗高度为设定的数值。" +
				"\n正常使用时，需要给本插件自启动权限、悬浮窗权限。");
		mCandidateView.clear();



		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(!Settings.canDrawOverlays(getApplicationContext())) {
				//启动Activity让用户授权
				Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
				intent.setData(Uri.parse("package:" + getPackageName()));
				startActivityForResult(intent,100);
			}
		}

		editText = findViewById(R.id.edit_height);
		btn_hide = findViewById(R.id.btn_hide);
		btn_show = findViewById(R.id.btn_show);

		btn_show.setOnClickListener(this);
		btn_hide.setOnClickListener(this);


	}




	@Override
	public void onClick(View view) {
		switch (view.getId()) {

		case R.id.clean:
			resetHandWritingRecognizeClicked();
			break;

			case R.id.btn_hide:
				cmd(-1);
				break;

			case R.id.btn_show:
				Editable s = editText.getText();
				if(s==null)
					cmd(-1);
				else
					cmd(Integer.parseInt(s.toString()));
				break;
		}
	}

	void cmd(int height){
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.example.input", "com.example.softwaretest.HWService"));

		if(height == -1){
			stopService(intent);
			return;
		}else {
			intent.putExtra("token","");
			intent.putExtra("height",height);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForegroundService(intent);
		} else
		{
			startService(intent);
		}
//		Toast.makeText(this,"set to "+height,Toast.LENGTH_SHORT).show();

	}



	void resetHandWritingRecognizeClicked() {
		resetHandWritingRecognize();
		mCandidateView.clear();
	}

	@Override
	public void candidateSelected(WnnWord wnnWord) {
		String candidate = null;
		if (wnnWord != null) {
			candidate = wnnWord.candidate;
		}
		if (!TextUtils.isEmpty(candidate)) {
			cleanCurrentPinyinView();
			appendCandidate(candidate);
			inputShow.setText(currentInput.toString());
		}
		// mOpenWnnZHCN.commitTextSelected(wnnWord);
		mCandidateView.clear();
		if (isHandWriting()) {
			resetHandWritingRecognize();
		} else {
//			ckManager.candidateSelected(wnnWord);
		}




	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 100) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (Settings.canDrawOverlays(this)) {
//					WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
//					WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//					params.type = WindowManager.LayoutParams.TYPE_PHONE;
//					params.format = PixelFormat.RGBA_8888;
//					windowManager.addView(view,params);
				} else {
					Toast.makeText(this, "ACTION_MANAGE_OVERLAY_PERMISSION权限已被拒绝", Toast.LENGTH_SHORT).show();
					;
				}
			}

		}
	}

	private void cleanCurrentPinyinView() {
		PinYinInput.setText("");
	}

	private void appendCandidate(String candidate) {
		currentInput.append(candidate);
	}

	@Override
	public void handWritingRecognized(ArrayList<WnnWord> result) {
		mCandidateView.setSuggestions(result, false, false);
	}

	// TODO 整理一下

	private void resetHandWritingRecognize() {
		handWritingBoard.reset_recognize();
	}

	/*
	 * 删除和上屏都要区分手写和字母输入；
	 */
	private boolean isHandWriting() {
		return handWritingBoard.getVisibility() == View.VISIBLE;
	}

	@Override
	public void onPinyinQueryed(PinyinQueryResult pyQueryResult) {
		if (pyQueryResult != null) {
			mCandidateView.setSuggestions(pyQueryResult.getCandidateList(), false, false);
			String pinyin = pyQueryResult.getCurrentInput();
			updatePinyin(pinyin);
		}
	}

	private void updatePinyin(String pinyin) {
		PinYinInput.setText(pinyin);
	}
}
