package com.tryndamere.wechat.bean;

/**
 * Created by Administrator on 2017/6/16 0016.
 */

public class LoginBean {

    /**
     * result_message : success
     * result_code : 200
     * data : {"createtime":1497604048000,"phone":"123","area":"北京","lasttime":1497604196266,"nickname":"Tryndamere","userId":2,"introduce":"我就是我不一样的烟火","password":"123"}
     */

    private String result_message;
    private int result_code;
    private DataBean data;

    public String getResult_message() {
        return result_message;
    }

    public void setResult_message(String result_message) {
        this.result_message = result_message;
    }

    public int getResult_code() {
        return result_code;
    }

    public void setResult_code(int result_code) {
        this.result_code = result_code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * createtime : 1497604048000
         * phone : 123
         * area : 北京
         * lasttime : 1497604196266
         * nickname : Tryndamere
         * userId : 2
         * introduce : 我就是我不一样的烟火
         * password : 123
         */

        private long createtime;
        private String phone;
        private String area;
        private long lasttime;
        private String nickname;
        private int userId;
        private String introduce;
        private String password;

        public long getCreatetime() {
            return createtime;
        }

        public void setCreatetime(long createtime) {
            this.createtime = createtime;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getArea() {
            return area;
        }

        public void setArea(String area) {
            this.area = area;
        }

        public long getLasttime() {
            return lasttime;
        }

        public void setLasttime(long lasttime) {
            this.lasttime = lasttime;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public String getIntroduce() {
            return introduce;
        }

        public void setIntroduce(String introduce) {
            this.introduce = introduce;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
