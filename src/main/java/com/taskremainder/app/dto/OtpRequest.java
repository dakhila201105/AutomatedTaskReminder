package com.taskremainder.app.dto;

public class OtpRequest {
    private String otp;
    private String email;
    private String getEmail(){ return email;}
    private void setEmail(String email){this.email=email;}
    public String getOtp() {return otp;}
    public void setOtp(String otp) {this.otp=otp;}
}
