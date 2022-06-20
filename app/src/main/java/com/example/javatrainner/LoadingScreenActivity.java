package com.example.javatrainner;

import android.util.Log;
import android.widget.ImageView;



public class LoadingScreenActivity{

	private int cornerIdx = 0;
	private ImageView LTCorner;
	private ImageView RTCorner;
	private ImageView RBCorner;
	private ImageView LBCorner;
	private ImageView skelaton;
	private ImageView[] cornerList = new ImageView[4];
	private int[] cornerListDrawable = {R.drawable.lfcorner, R.drawable.rtcorner, R.drawable.rbcorner,
			R.drawable.lbcorner};
	private int[] cornerListDrawableWhite = {R.drawable.ltcornerw, R.drawable.rtcornerw,
			R.drawable.rbcornerw, R.drawable.lbcornerw};

	LoadingScreenActivity(ImageView LTCorner, ImageView RTCorner, ImageView RBCorner,
						  ImageView LBCorner, ImageView skelaton) {
		this.LTCorner = LTCorner;
		this.cornerList[0] = this.LTCorner;
		this.RTCorner = RTCorner;
		this.cornerList[1] = this.RTCorner;
		this.RBCorner = RBCorner;
		this.cornerList[2] = this.RBCorner;
		this.LBCorner = LBCorner;
		this.cornerList[3] = this.LBCorner;
		this.skelaton = skelaton;
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
	
	