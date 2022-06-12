package com.example.javatrainner;

import android.widget.TextView;

import org.w3c.dom.Text;

public class TechniqueCorrectUI {

    private final TextView counter;
    private final TextView successPercentage;
    private final TextView userFeedback;


    TechniqueCorrectUI(TextView counter, TextView successPer, TextView userFeedback){
        this.counter = counter;
        this.successPercentage = successPer;
        this.userFeedback = userFeedback;
    }

    public void setCounter(String count){
        this.counter.setText(count);
    }

    public void setSuccessPercentage(String successPer){
        this.successPercentage.setText(successPer);
    }

    public void setUserFeedback(String userFeedback){
        this.userFeedback.setText(userFeedback);
    }
}
