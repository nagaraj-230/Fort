package com.payoda.smartlock.request.model;

public enum RequestStatus {
    NOTASSIGNED("0"),
    APPROVED("1"),
    REJECTED("2"),
    WITHDRAW("3");
    RequestStatus(String id){
        this.id=id;
    }
    private String id;

    public String getId() {
        return id;
    }
}
