package com.zzzi.springframework.beans.factory.bean;

/**
 * @author zzzi
 * @date 2023/10/31 20:31
 * 需要实例化的bean对象，由于属性时单独填充的，所以不需要有参构造
 */
public class UserService {
    private int id;
    private UserDao userDao;

    public void query() {
        System.out.println("查询用户信息：" + userDao.query(id));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

}
