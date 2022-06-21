package com.example.javatrainner;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;


public class LoadingScreenActivity extends Activity {

	private int cornerIdx = 0;
	private ImageView LTCorner;
	private ImageView RTCorner;
	private ImageView RBCorner;
	private ImageView LBCorner;
	private ImageView skelaton;
	private ImageView[] cornerList = new ImageView[4];
	private int[] cornerListDrawable = {R.drawable.lfcorner, R.drawable.rtcorner,
			R.drawable.rbcorner, R.drawable.lbcorner};
	private int[] cornerListDrawableWhite = {R.drawable.ltcornerw, R.drawable.rtcornerw,
			R.drawable.rbcornerw, R.drawable.lbcornerw};
	private Thread uiThread;
	private Handler handler;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		this.LTCorner = findViewById(R.id.LTCorner);
		this.cornerList[0] = this.LTCorner;
		this.RTCorner = findViewById(R.id.RTCorner);
		this.cornerList[1] = this.RTCorner;
		this.RBCorner = findViewById(R.id.RBCorner);
		this.cornerList[2] = this.RBCorner;
		this.LBCorner = findViewById(R.id.LBCorner);
		this.cornerList[3] = this.LBCorner;
		this.skelaton = findViewById(R.id.skelaton);
		this.uiThread = Thread.currentThread();
		this.handler = new Handler();
		new Thread() {
			@Override
			public void run() {
				super.run();
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						iterateUntilReady();
					}
				};
				runnable.run();
			}
		}.start();
	}

	private void iterateUntilReady() {
		while (true) {
			this.iterateCornerColors();
			try {
				TimeUnit.MILLISECONDS.sleep(500);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public void iterateCornerColors(){
		int mod = cornerList.length;
		if (this.cornerIdx < mod){
			this.cornerList[this.cornerIdx].setImageResource(this.cornerListDrawable[this.cornerIdx]);
		}
		int prev = (this.cornerList.length + this.cornerIdx - 1) % mod;
		if (prev >= 0 && prev < mod) {
			this.cornerList[prev].setImageResource(this.cornerListDrawableWhite[prev]);
		}
		this.cornerIdx = (cornerIdx + 1) % mod;
	}
}
	
	