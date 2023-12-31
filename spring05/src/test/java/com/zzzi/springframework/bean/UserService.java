package com.zzzi.springframework.bean;

public class UserService {

    private String uId;

    private UserDao userDao;

    public String query() {
        return userDao.query(uId);
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}
