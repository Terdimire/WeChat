package com.tryndamere.wechat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.tryndamere.wechat.bean.UserBean;
import com.tryndamere.wechat.http.HttpUrlUtils;
import com.tryndamere.wechat.http.OkHttpUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/6/16 0016.
 */

public class TestActivity extends Activity {

    private UserBean myuserBean = new UserBean();
    private String url1 = "http://169.254.118.53:8080/MyoneInterface/userAction_add.action?user.nickname=12121231&user.password=456&user.gender=%E7%94%B7&user.area=1212&user.phone=141161113&user.introduce=1212";
    //注册参数
    private Map<String, Object> regparam = new HashMap<>();

    //登录参数
    private Map<String, Object> loginparam = new HashMap<>();


    //聊天的参数
    private Map<String, Object> chatnparam = new HashMap<>();


    //添加好友的参数
    private Map<String, Object> addFriendparam = new HashMap<>();


    private EditText editChat;
    private EditText editUsername;
    private EditText editPassword;

    private TextView textResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView(this);
        initParams();
        initLisintener();
    }

    private void initLisintener() {
        EMMessageListener msgListener = new EMMessageListener() {

            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                editChat.setText(messages.get(0).toString());
                //收到消息
                Log.d("EMMessage", "收到消息:" + messages.get(0).toString());
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {
                //收到透传消息
                Log.d("EMMessage", "收到透传消息:" + messages.get(0).toString());
            }

            @Override
            public void onMessageRead(List<EMMessage> messages) {
                //收到已读回执
                Log.d("EMMessage", "收到已读回执:" + messages.get(0).toString());
            }

            @Override
            public void onMessageDelivered(List<EMMessage> message) {
                //收到已送达回执
                Log.d("EMMessage", "收到已送达回执:" + message.get(0).toString());
            }

            @Override
            public void onMessageChanged(EMMessage message, Object change) {
                //消息状态变动
                Log.d("EMMessage", "消息状态变动:" + message.toString());
            }
        };
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
    }

    private void initView(TestActivity testActivity) {
        editChat = (EditText) testActivity.findViewById(R.id.edit_chat);
        editUsername = (EditText) testActivity.findViewById(R.id.edit_phone);
        editPassword = (EditText) testActivity.findViewById(R.id.edit_password);
        textResult = (TextView) testActivity.findViewById(R.id.text_result);
    }

    private void initParams() {


        //注册的参数
        regparam.put("user.nickname", "Tryndamere");
        regparam.put("user.password", "123");
        regparam.put("user.gender", Utils.toUTF("男"));
        regparam.put("user.area", Utils.toUTF("北京"));
        regparam.put("user.phone", "lzm");
        regparam.put("user.introduce", Utils.toUTF("我就是我不一样的烟火"));

    }

    //注册
    public void getAsynRegist(View v) {


        OkHttpUtils.getInstance().getAsyn(HttpUrlUtils.REGIST_URL, regparam, UserBean.class, new OkHttpUtils.HttpCallBack<UserBean>() {
            @Override
            public void onSuccess(UserBean userBean) {
                myuserBean = userBean;
                Log.d("MainActivity", "getAsynRegist_userBean:" + userBean.toString());
                textResult.setText(userBean.toString());
            }

            @Override
            public void onFailure(IOException e) {
                e.printStackTrace();
                Log.d("MainActivity", "e:" + e.toString());

            }
        });
    }

    //登录
    public void getAsynLogin(View v) {

        //登录的参数
        loginparam.put("user.phone", editUsername.getText().toString().trim());
        loginparam.put("user.password", editPassword.getText().toString().trim());

        OkHttpUtils.getInstance().getAsyn(HttpUrlUtils.LOGIN_URL, regparam, null, new OkHttpUtils.HttpCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d("MainActivity", "getAsynLogin_result:" + result.toString());
                textResult.setText(result.toString());

            }

            @Override
            public void onFailure(IOException e) {
                e.printStackTrace();
                Log.d("MainActivity", "e:" + e.toString());
            }
        });
    }

    //上传头像
    public void upLoadAsyn(View v) {
        File file = Environment.getExternalStorageDirectory();
        final File iv = new File(file, "ac.png");
        Log.d("MainActivity", "1111");

        OkHttpUtils.getInstance().postUpLoadAsyn("http://169.254.118.53:8080/MyoneInterface/userAction_uploadImage.action", iv, "user.file", new OkHttpUtils.HttpCallBack<String>() {
            @Override
            public void onSuccess(String s) {
                Log.d("MainActivity", s);
                textResult.setText(s.toString());
            }

            @Override
            public void onFailure(IOException e) {
                Log.d("MainActivity", "e:" + e);
            }
        });
    }

    //聊天儿发送
    public void upLoadChat(View v) {

        //聊天的参数
        chatnparam.put("chat.userId", 0);
        chatnparam.put("chat.userId", 1); //发送好友的ID
        chatnparam.put("chat.userId", Utils.toUTF(editChat.getText().toString().trim()));


        OkHttpUtils.getInstance().getAsyn(HttpUrlUtils.SEND_MESSAGE_URL, chatnparam, null, new OkHttpUtils.HttpCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d("MainActivity", "upLoadChat_result:" + result.toString());
                textResult.setText(result.toString());
            }

            @Override
            public void onFailure(IOException e) {
                e.printStackTrace();
                Log.d("MainActivity", "e:" + e.toString());
            }
        });
    }

    //添加好友
    public void addFriend(View v) {

        //添加好友的参数
        addFriendparam.put("relationship.friendId", "");//添加好友的ID
        addFriendparam.put("relationship.groupName", "a");

        OkHttpUtils.getInstance().getAsyn(HttpUrlUtils.ADD_FRIEND_URL, regparam, null, new OkHttpUtils.HttpCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                Log.d("MainActivity", "addFriend_result:" + result.toString());
                textResult.setText(result.toString());

            }

            @Override
            public void onFailure(IOException e) {
                e.printStackTrace();
                Log.d("MainActivity", "e:" + e.toString());
            }
        });
    }

}
