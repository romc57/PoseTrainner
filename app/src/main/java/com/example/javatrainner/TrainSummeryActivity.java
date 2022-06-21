
	 
	/*
	 *	This content is generated from the API File Info.
	 *	(Alt+Shift+Ctrl+I).
	 *
	 *	@desc 		
	 *	@file 		program_details
	 *	@date 		Friday 17th of June 2022 11:14:22 AM
	 *	@title 		Program Details
	 *	@author 	
	 *	@keywords 	
	 *	@generator 	Export Kit v1.3.xd
	 *
	 */
	

package com.example.javatrainner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

public class TrainSummeryActivity extends Activity {

	private ImageView firstStar;
	private ImageView secondStar;
	private ImageView thirdStar;
	private ImageView fourthStar;
	private ImageView fifthStar;
	private TextView shortFeedback;
	private TextView smallerShortFeedback;
	private TextView fireIcon;
	private TextView fireValue;
	private TextView fireCompare;
	private TextView accuracy;
	private TextView accuracyValue;
	private TextView accuracyCompare;
	private TextView timeElapsed;
	private TextView timeElapsedValue;
	private TextView timeElapsedCompare;
	private TextView averageHeartRate;
	private TextView averageHeartRateValue;
	private TextView averageHeartRateCompare;
	private TextView detailedFeedbackValue;
	private String[] titledList = {"Blame the teacher..", "You can do better",
			"Not bad! Try again", "Almost Aced it!", "Nice Job!", "Perfect!"};

	protected void onCreate(Bundle savedInstance){
		super.onCreate(savedInstance);
		setContentView(R.layout.train_summery);
		this.shortFeedback = findViewById(R.id.short_feedback);
		this.firstStar = findViewById(R.id.first_star);
		this.secondStar = findViewById(R.id.second_star);
		this.thirdStar = findViewById(R.id.third_star);
		this.fourthStar = findViewById(R.id.fourth_star);
		this.fifthStar = findViewById(R.id.fifth_star);
		this.smallerShortFeedback = findViewById(R.id.small_short_feedback);;
		this.fireIcon = findViewById(R.id.fire_field);
		this.fireValue = findViewById(R.id.fire_value);
		this.fireCompare = findViewById(R.id.fire_compare);
		this.accuracy = findViewById(R.id.accuracy);
		this.accuracyValue = findViewById(R.id.accuracy_value);
		this.accuracyCompare = findViewById(R.id.accuracy_compare);
		this.timeElapsed = findViewById(R.id.time_elapsed);
		this.timeElapsedValue = findViewById(R.id.time_elapsed_value);
		this.timeElapsedCompare = findViewById(R.id.time_elapsed_compare);
		this.averageHeartRate = findViewById(R.id.average_heart_rate);
		this.averageHeartRateValue = findViewById(R.id.average_heart_rate_value);
		this.averageHeartRateCompare = findViewById(R.id.average_heart_rate_compare);
		this.detailedFeedbackValue = findViewById(R.id.detailed_feedback_value);
		Button homeBtn = findViewById(R.id.statistics_home_btn);
		homeBtn.setOnClickListener(v -> this.switchToHomeActivity());
		ImageView backBtn = findViewById(R.id.back_arrow);
		backBtn.setOnClickListener(v -> this.switchToTrainer());
		ImageView closeWindow = findViewById(R.id.close_window);
		closeWindow.setOnClickListener(v -> this.switchToTrainer());
		this.set_default_elements();
		Bundle b = getIntent().getExtras();
		this.setWorkOutSummery(b);
	}

	private void setWorkOutSummery(Bundle b) {
		if (b == null) { return; }
		int[] resList = b.getIntArray("intArray");
		int repCount = resList[0];
		int goodRepCount = resList[1];
		int timeInWorkout = resList[2];
		this.fireValue.setText(String.valueOf(repCount));
		this.accuracyValue.setText(String.valueOf(goodRepCount));
		this.timeElapsedValue.setText(this.getTimeInString(timeInWorkout));
		this.shortFeedback.setText(this.titledList[goodRepCount]);
		this.smallerShortFeedback.setText("You did " + goodRepCount + " out of " + repCount);
	}

	public void set_default_elements(){
		this.shortFeedback.setText("Did not see");
		this.smallerShortFeedback.setText("Small feedback");
		this.fireIcon.setText("Repetitions:");
		this.fireValue.setText("0");
		this.fireCompare.setText("No history");
		this.accuracy.setText("Good form");
		this.accuracyValue.setText("0");
		this.accuracyCompare.setText("No history");
		this.timeElapsed.setText("Time");
		this.timeElapsedValue.setText("0");
		this.timeElapsedCompare.setText("No history");
		this.averageHeartRate.setText("Average rest time");
		this.averageHeartRateValue.setText("0");
		this.averageHeartRateCompare.setText("No history");
		this.detailedFeedbackValue.setText("No details");
	}

	private void switchToHomeActivity(){
		Intent homeActivity = new Intent(this, MainActivity.class);
		startActivity(homeActivity);
	}

	private void switchToTrainer() {
		Intent train = new Intent(this, TechniqueCorrectActivity.class);
		startActivity(train);
	}

	private String getTimeInString(int time) {
		int min = (int) Math.floor(time / 60);
		int sec = (int) (time % 60);
		String minutes = (min < 10) ? "0" + min : String.valueOf(min);
		String seconds = (sec < 10) ? "0" + sec : String.valueOf(sec);
		return minutes + ":" + seconds;
	}
}