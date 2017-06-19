package com.tryndamere.wechat;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.util.Utils;
import com.tryndamere.wechat.bean.ChatBean;
import com.tryndamere.wechat.bean.LoginBean;
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
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1){
                String s = msg.obj.toString();
                String substring = s.substring(5, s.length() - 1);
                textResult.setText(substring);
            }
        }
    };
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
            public void onMessageReceived(final List<EMMessage> messages) {
                //收到消息

                Log.d("EMMessage", "收到消息:" + messages.get(0).getBody().toString());
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        Message message = new Message();
                        message.what = 1;
                        message.obj = messages.get(0).getBody().toString();
                        handler.sendMessage(message);
                    }
                }.start();
               // textResult.setText(messages.get(0).getBody().toString());
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {
                //收到透传消息
                Log.d("EMMessage", "收到透传消息:" + messages.toString());
            }

            @Override
            public void onMessageRead(List<EMMessage> messages) {
                //收到已读回执
                Log.d("EMMessage", "收到已读回执:" + messages.toString());
            }

            @Override
            public void onMessageDelivered(List<EMMessage> message) {
                //收到已送达回执
                Log.d("EMMessage", "收到已送达回执:" + message.toString());
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
        regparam.put("user.nickname", "LZM");
        regparam.put("user.password", "123");
        regparam.put("user.gender", "男");
        regparam.put("user.area", "北京");
        regparam.put("user.phone", "15313095207");
        regparam.put("user.introduce", "我就是我不一样的烟火");

    }

    //注册
    public void getAsynRegist(View v) {


        OkHttpUtils.getInstance().getAsyn(HttpUrlUtils.REGIST_URL, regparam, UserBean.class, new OkHttpUtils.HttpCallBack<UserBean>() {
            @Override
            public void onSuccess(UserBean userBean) {
                myuserBean = userBean;
                Log.d("MainActivity", "getAsynRegist_userBean:" + userBean.toString());
                textResult.setText(userBean.toString());
                Toast.makeText(TestActivity.this, "成功", Toast.LENGTH_SHORT).show();
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

        //loginEasemob(editUsername.getText().toString().trim(),editPassword.getText().toString().trim());

        //登录的参数
        loginparam.put("user.phone", editUsername.getText().toString().trim());
        loginparam.put("user.password", editPassword.getText().toString().trim());

        OkHttpUtils.getInstance().getAsyn(HttpUrlUtils.LOGIN_URL, regparam, LoginBean.class, new OkHttpUtils.HttpCallBack<LoginBean>() {
            @Override
            public void onSuccess(LoginBean result) {
                loginEasemob(result);
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

    private void loginEasemob(LoginBean result) {

        EMClient.getInstance().login(result.getData().getPhone(), result.getData().getPassword(), new EMCallBack() {//回调
            @Override
            public void onSuccess() {
                EMClient.getInstance().groupManager().loadAllGroups();
                EMClient.getInstance().chatManager().loadAllConversations();
                Log.d("main", "登录聊天服务器成功！");
                //textResult.setText("登录聊天服务器成功!");
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String message) {
                Log.d("main", "登录聊天服务器失败！");
            }
        });
    }

    //聊天儿发送
    public void upLoadChat(View v) {
        fromFuWuQi();
        //fromHuanXin();
    }

    private void fromHuanXin() {
        //创建一条文本消息，content为消息文字内容，toChatUsername为对方用户或者群聊的id，后文皆是如此
        EMMessage message = EMMessage.createTxtSendMessage(editChat.getText().toString().trim(), "456");
        //如果是群聊，设置chattype，默认是单聊
        message.setChatType(EMMessage.ChatType.Chat);
        //发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
    }

    private void fromFuWuQi() {

        //聊天的参数
        chatnparam.put("chat.userId", 4);
        chatnparam.put("chat.userId", 2); //发送好友的ID
        chatnparam.put("chat.userId", editChat.getText().toString().trim());


        OkHttpUtils.getInstance().getAsyn(HttpUrlUtils.SEND_MESSAGE_URL, chatnparam, ChatBean.class, new OkHttpUtils.HttpCallBack<ChatBean>() {
            @Override
            public void onSuccess(ChatBean result) {

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
        addFriendparam.put("relationship.friendId", 2);//添加好友的ID
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
