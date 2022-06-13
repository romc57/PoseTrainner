package com.example.javatrainner;

import java.util.Arrays;

public class knnWrapper {
	private KNeighborsClassifier knnClassifier;

	public knnWrapper(int nNeighbors, int nClasses,
					  double [][] x, int[] y) {
		this.knnClassifier = new KNeighborsClassifier(nNeighbors, nClasses, 2, x, y);
	}

	public int predict(double [][][] x) {
		return this.knnClassifier.predict(flatten(x));

	}
	private double[] flatten(double [][][] x) {
		double[] newX = Arrays.stream(x)
				.flatMap(Arrays::stream)
				.flatMapToDouble(Arrays::stream)
				.toArray();

		System.out.println(Arrays.toString(newX));
		return newX;
	}
}