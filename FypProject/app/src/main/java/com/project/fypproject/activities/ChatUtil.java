package com.project.fypproject.activities;

import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.fypproject.models.ChatModel;

public class ChatUtil {

    public static void passUserIntent(Intent intent, ChatModel model){
        intent.putExtra("lastName",model.getLastName());
        intent.putExtra("firstName",model.getFirstName());
        intent.putExtra("email",model.getEmail());
    }

    public static ChatModel getUserModelIntent(Intent intent){
        ChatModel chatModel = new ChatModel();
        chatModel.setLastName(intent.getStringExtra("lastName"));
        chatModel.setFirstName(intent.getStringExtra("firstName"));
        chatModel.setEmail(intent.getStringExtra("email"));
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
    public static String getChatroomId(String userEmail1,String userEmail2){
        if(userEmail1.hashCode()<userEmail2.hashCode()){
            return userEmail1+"_"+userEmail2;
        }else{
            return userEmail2+"_"+userEmail1;
        }
    }

}
