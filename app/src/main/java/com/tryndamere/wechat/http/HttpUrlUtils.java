package com.tryndamere.wechat.http;

/**
 * Created by Administrator on 2017/6/16 0016.
 */

public class HttpUrlUtils {

    //主机地址
    public static String HOST_URL = "http://169.254.118.53:8080/MyoneInterface/";

    // user.nickname=12121231
    // &user.password=456&user.gender=%E7%94%B7
    // &user.area=1212
    // &user.phone=123
    // &user.introduce=1212

    //1 注册地址(get)
    public static String REGIST_URL = HOST_URL + "userAction_add.action?";


    //user.phone=123132
    //&user.password=1231123

    //2 登录地址(get)
    public static String LOGIN_URL = HOST_URL + "userAction_login.action?";


    //relationship.friendId=3
    //&relationship.groupName=a

    //3 添加好友地址(get)
    public static String ADD_FRIEND_URL = HOST_URL + "userAction_addFriends.action?";


    //chat.userId=1
    //&chat.touserId=2
    //&chat.message=你好

    //4 发送聊天消息(get)
    public static String SEND_MESSAGE_URL = HOST_URL + "userAction_chatMessage.action?";


    // chat.userId=1&
    // chat.touserId=2&
    // pageIndex=0&
    // pageSize=2

    //5 查看聊天记录(get)
    public static String SEARCH_CHAT_LOG_URL = HOST_URL + "userAction_selectChat.action?";


    // pageIndex=0
    // &pageSize=2

    //6 查询附近的人(get)
    public static String FIND_NEAR_USER_URL = HOST_URL + "userAction_selectAllUserAndFriend.action?";


    // userAction_selectUserById.action?
    // user.userId=2

    //7 查询用户详细信息(get)
    public static String SEARCH_USER_INFO_URL = HOST_URL + "userAction_selectUserById.action?";


    // 必须必须必须必须存cookie 否则无法上传!!!!!!!!!!!!!!!!
    // user.file    (必填，文件)  file类型
    //8 上传头像(post)
    public static String UPLOAD_PROFILE_URL = HOST_URL + "userAction_uploadImage.action";


    // user.phone=123&
    // user.userId=1&
    // user.gender=男&
    // user.area=145&
    // user.nickname=545&
    // user.introduce=121

    //9 修改用户基本信息(get)
    public static String UPDATE_USER_INFO_URL = HOST_URL + "userAction_updateUser.action?";


    // 必须必须必须必须存cookie 否则无法上传!!!!!!!!!!!!!!!!
    // user.file    (必填，文件)  file类型

    //10 修改头像(post)
    public static String UPDATE__PROFILE_URL = HOST_URL + "userAction_updateuploadImage.action";


    // newPassword=666

    //11 修改密码(get)
    public static String UPDATE_PASSWORD_URL = HOST_URL + "userAction_updatePassword.action?";

    //album.albumName （必填，相册名） String
    //user.file    (必填，文件)  file类型
    // 必须必须必须必须存cookie 否则无法上传!!!!!!!!!!!!!!!!

    //12 上传图片到相册(post)
    public static String UPLOAD_PHOTOES_URL = HOST_URL + "userAction_uploadPhotoAlbum.action";
}