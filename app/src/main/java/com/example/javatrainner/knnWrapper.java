package com.example.javatrainner;

import android.util.Log;

public class knnWrapper {
	public KNeighborsClassifier knnClassifier;

	public knnWrapper(int nNeighbors, int nClasses,
					  double [][] x, int[] y) {
		this.knnClassifier = new KNeighborsClassifier(nNeighbors, nClasses, 2, x, y);
	}

	public int predict(double [][][] x) {
		return this.knnClassifier.predict(flatten(x));
	}

	private double[] flatten(double [][][] x) {
		double[] output = new double[x.length * x[0].length * x[0][0].length];
		String test = "";
		int l = 0;
		for (int i = 0; i < x.length; i++){
			for (int j = 0; j < x[i].length; j++){
				for (int k = 0; k < x[i][j].length; k++){
					output[l] = x[i][j][k];
					test = test + x[i][j][k] + "; ";
					l++;
				}
			}
		}
		Log.i("Rep", "Flat to size " + output.length);
		Log.i("Rep: ", test);
		return output;
	}
}