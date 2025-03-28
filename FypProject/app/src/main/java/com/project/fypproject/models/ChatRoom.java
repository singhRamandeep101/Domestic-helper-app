package com.project.fypproject.models;


import com.google.firebase.Timestamp;

import java.util.List;

public class ChatRoom {
    String chatroomId;
    List<String>userEmails;
    Timestamp lastMessageTimestamp;
    String lastMessageSenderId;
    String lastMessage;
    String employeeEmail;
    String employerEmail;
    String agentEmail;

    public ChatRoom() {
    }

    public ChatRoom(String chatroomId, List<String> userEmails, Timestamp lastMessageTimestamp, String lastMessageSenderId,String employeeEmail,String agentEmail,String employerEmail) {
        this.chatroomId = chatroomId;
        this.userEmails = userEmails;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.lastMessageSenderId = lastMessageSenderId;
        this.employeeEmail = employeeEmail;
        this.agentEmail = agentEmail;
        this.employerEmail = employerEmail;
    }

    public String getChatroomId() {
        return chatroomId;
    }

    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
    }

    public List<String> getUserEmails() {
        return userEmails;
    }

    public void setUserEmails(List<String> userEmails) {
        this.userEmails = userEmails;
    }

    public Timestamp getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(Timestamp lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public String getEmployerEmail() {
        return employerEmail;
    }

    public void setEmployerEmail(String employerEmail) {
        this.employerEmail = employerEmail;
    }

    public String getAgentEmail() {
        return agentEmail;
    }

    public void setAgentEmail(String agentEmail) {
        this.agentEmail = agentEmail;
    }
}
