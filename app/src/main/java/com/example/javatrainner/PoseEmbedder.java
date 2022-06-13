package com.example.javatrainner;
import java.util.stream.IntStream;

public class PoseEmbedder {
	private double torsoSizeMultiplier;
	private final String[] landmarksNames = {"Nose", "LEye_i", "LEye", "REye_o", "REye_i",
			"REye", "REye_o", "LEar", "REar", "LLip", "RLip", "LShoulder", "RShoulder", "LHip",
			"RHip", "LKnee", "RKnee", "LAnkle", "RAnkle", "LHeel", "RHeel", "LTows", "RTows"};

	public PoseEmbedder(double torsoSizeMultiplier) {
		this.torsoSizeMultiplier = torsoSizeMultiplier;
	}

	public double[][] embeddedLandmarks(double[][] landmarks) {
		if (landmarks.length != this.landmarksNames.length) {
			return null;
		}
		double[][] normalizeLandmarks = this.normalizePoseLandmarks(landmarks);
		return this.getPoseDistanceEmbedding(normalizeLandmarks);
	}

	private double[][] normalizePoseLandmarks(double[][] landmarks) {
		double[] poseCenter = this.getPoseCenter(landmarks);
		for (int i = 0; i < landmarks.length; i++) {
			landmarks[i][0] = landmarks[i][0] - poseCenter[0];
			landmarks[i][1] = landmarks[i][1] - poseCenter[1];
			landmarks[i][2] = landmarks[i][2] - poseCenter[2];
		}
		double poseSize = this.getPoseSize(landmarks);
		for (int i = 0; i < landmarks.length; i++) {
			landmarks[i][0] = landmarks[i][0] * (100 / poseSize);
			landmarks[i][1] = landmarks[i][1] * (100 / poseSize);
			landmarks[i][2] = landmarks[i][2] * (100 / poseSize);
		}
		return landmarks;

	}

	private double[] getPoseCenter(double[][] landmarks) {
		double[] lHip = landmarks[this.findIndex("LHip")];
		double[] rHip = landmarks[this.findIndex("RHip")];
		double xCenter = (lHip[0] + rHip[0]) * 0.5;
		double yCenter = (lHip[1] + rHip[1]) * 0.5;
		double zCenter = (lHip[2] + rHip[2]) * 0.5;
		return new double[]{xCenter, yCenter, zCenter};
	}

	private double getPoseSize(double[][] landmarks) {
		double[] lHip = landmarks[this.findIndex("LHip")];
		double[] rHip = landmarks[this.findIndex("RHip")];
		double[] lShoulder = landmarks[this.findIndex("LShoulder")];
		double[] rShoulder = landmarks[this.findIndex("RShoulder")];
		double[] hips = {lHip[0] + rHip[0] * 0.5, lHip[1] + rHip[1] * 0.5};
		double[] shoulders = {lShoulder[0] + rShoulder[0] * 0.5, lShoulder[1] + rShoulder[1] * 0.5};
		double torsoSize = Math.sqrt(Math.pow(shoulders[0] - hips[0], 2) + Math.pow(shoulders[1] - hips[1],
																					2));
		double[] poseCenter = this.getPoseCenter(landmarks);
		double maxValue = 0;
		for (double[] landmark: landmarks) {
			double currentVal =
					Math.sqrt(Math.pow(landmark[0] - poseCenter[0], 2) + Math.pow(landmark[1] - poseCenter[1],
																						 2));
			if (currentVal > maxValue) {
				maxValue = currentVal;
			}
		}
		return Math.max(torsoSize * this.torsoSizeMultiplier, maxValue);
	}

	private double[][] getPoseDistanceEmbedding(double[][] landmarks) {
		return new double[][]{
				this.getDistance(
						this.getAverageByName(landmarks, "LHip", "RHip"),
						this.getAverageByName(landmarks, "LShoulder", "RShoulder")
				),
				this.getDistanceByName(landmarks, "LHip", "LKnee"),
				this.getDistanceByName(landmarks, "RHip", "RKnee"),
				this.getDistanceByName(landmarks, "LKnee", "LAnkle"),
				this.getDistanceByName(landmarks, "RKnee", "RAnkle"),
				this.getDistanceByName(landmarks, "LHip", "LAnkle"),
				this.getDistanceByName(landmarks, "RHip", "RAnkle"),
				this.getDistanceByName(landmarks, "LShoulder", "LAnkle"),
				this.getDistanceByName(landmarks, "RShoulder", "RAnkle"),
				this.getDistanceByName(landmarks, "LKnee", "RKnee"),
				this.getDistanceByName(landmarks, "LAnkle", "RAnkle"),
		};
	}

	private double[] getAverageByName(double[][] landmarks, String nameFrom, String nameTo) {
		double[] lmkFrom = landmarks[this.findIndex(nameFrom)];
		double[] lmkTo = landmarks[this.findIndex(nameTo)];
		return this.getAverage(lmkFrom, lmkTo);
	}

	private double[] getDistanceByName(double[][] landmarks, String nameFrom, String nameTo) {
		double[] lmkFrom = landmarks[this.findIndex(nameFrom)];
		double[] lmkTo = landmarks[this.findIndex(nameTo)];
		return this.getDistance(lmkFrom, lmkTo);
	}

	private double[] getDistance(double[] lmkFrom, double[] lmkTo) {
		return new double[]{lmkTo[0] - lmkFrom[0], lmkTo[1] - lmkFrom[1], lmkTo[2] - lmkFrom[2]};
	}

	private double[] getAverage(double[] lmkFrom, double[] lmkTo) {
		return new double[]{(lmkTo[0] + lmkFrom[0]) * 0.5, (lmkTo[1] + lmkFrom[1]) * 0.5,
				(lmkTo[2] + lmkFrom[2]) * 0.5};
	}

	private Integer findIndex(String t) {
		int len = this.landmarksNames.length;
		for (int i = 0; i < len; i++){
			if (this.landmarksNames[i] == t) {
				return i;
			}
		}
		return null;
	}
}
