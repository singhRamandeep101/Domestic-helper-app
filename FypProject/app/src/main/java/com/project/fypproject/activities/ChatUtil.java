package com.project.fypproject.activities;

import android.content.Intent;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.fypproject.models.ChatModel;

import java.text.SimpleDateFormat;
import java.util.List;

public class ChatUtil {

    public static void passUserIntent(Intent intent, ChatModel model){
        intent.putExtra("lastName",model.getLastName());
        intent.putExtra("firstName",model.getFirstName());
        intent.putExtra("email",model.getEmail());
        intent.putExtra("userType",model.getUserType());
    }

    public static ChatModel getUserModelIntent(Intent intent){
        ChatModel chatModel = new ChatModel();
        chatModel.setLastName(intent.getStringExtra("lastName"));
        chatModel.setFirstName(intent.getStringExtra("firstName"));
        chatModel.setEmail(intent.getStringExtra("email"));
        chatModel.setUserType(intent.getStringExtra("userType"));
        return chatModel;
    }

    public static String currentUserEmail(){
        return FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }

    public static DocumentReference currentUserDetails(){
        return FirebaseFirestore.getInstance().collection("users").document(currentUserEmail());
    }

    public static CollectionReference allUserCollectionReference(){
        return FirebaseFirestore.getInstance().collection("users");
    }

    public static DocumentReference getChatroomReference(String chatroomId){
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId);
    }

    public static CollectionReference getChatroomMessageReference(String chatroomId){
        return getChatroomReference(chatroomId).collection("chats");
    }
    public static String getChatroomId(String userEmail1, String userEmail2) {
        String baseId;
        if (userEmail1.hashCode() < userEmail2.hashCode()) {
            return userEmail1 + "_" + userEmail2;
        } else {
            return userEmail2 + "_" + userEmail1;
        }
    }

    public static CollectionReference allChatroomCollectionReference(){
        return FirebaseFirestore.getInstance().collection("chatrooms");
    }

    public static DocumentReference getOtherUserFromChatroom(List<String>userEmail){
        if(userEmail.get(0).equals(ChatUtil.currentUserEmail())){
            return allUserCollectionReference().document(userEmail.get(1));
        }else{
            return allUserCollectionReference().document(userEmail.get(0));
        }
    }

    public static String timestampToString(Timestamp timestamp){
        return new SimpleDateFormat("HH:mm").format(timestamp.toDate());
    }

}
