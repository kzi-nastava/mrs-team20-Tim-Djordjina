package com.example.team20_tim_djordjina.model;

/** Body for reporting an inconsistent route (US#2.6.2) */
public class InconsistencyRequest {
    private String text;

    public InconsistencyRequest(String text) {
        this.text = text;
    }
}
