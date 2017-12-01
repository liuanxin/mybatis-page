package com.github.liuanxin.page;

import com.github.liuanxin.page.model.PageBounds;
import com.github.liuanxin.page.model.PageList;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author https://github.com/liuanxin
 */
public class UserTest extends BaseDao {

    @Test
    public void user() {
        List<User> userList = userPage(1, 10);
        Assert.assertEquals(10, userList.size());
        Assert.assertTrue(userList instanceof PageList);

        userList = userPage(2, 20);
        Assert.assertEquals(5, userList.size());
        Assert.assertEquals(25, ((PageList) userList).getTotal());


        userList = userPageMap("", "", 1, 10);
        Assert.assertEquals(10, userList.size());
        Assert.assertTrue(userList instanceof PageList);

        userList = userPageMap("", "", 2, 20);
        Assert.assertEquals(5, userList.size());
        Assert.assertEquals(25, ((PageList) userList).getTotal());


        userList = userPageMap("0", "a", 2, 2);
        System.out.println(((PageList) userList).getTotal());
        for (User user : userList) {
            System.out.println(user);
        }
    }

    private List<User> userPage(int page, int size) {
        PageBounds pageBounds = new PageBounds(page, size);

        SqlSession session = null;
        try {
            session = getSqlSession();
            return session.selectList("com.github.liuanxin.page.findUser", new HashMap(), pageBounds);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    private List<User> userPageMap(String userName, String password, int page, int size) {
        PageBounds pageBounds = new PageBounds(page, size);
        Map<String, String> param = new HashMap<String, String>();
        if (userName != null && !"".equals(userName)) {
            param.put("userName", "%" + userName + "%");
        }
        if (password != null && !"".equals(password)) {
            param.put("password", "%" + password + "%");
        }

        SqlSession session = null;
        try {
            session = getSqlSession();
            return session.selectList("com.github.liuanxin.page.findUserByMap", param, pageBounds);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
