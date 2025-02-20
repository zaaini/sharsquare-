package com.example.ss;

public class Feedback {
    private String name;
    private String email;
    private String feedback;

    public Feedback() {
        // Default constructor required for calls to DataSnapshot.getValue(Feedback.class)
    }

    public Feedback(String name, String email, String feedback) {
        this.name = name;
        this.email = email;
        this.feedback = feedback;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getFeedback() {
        return feedback;
    }
}
