package com.github.liuanxin.page;

import com.github.liuanxin.page.model.PageBounds;
import com.github.liuanxin.page.model.PageList;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

/**
 * @author https://github.com/liuanxin
 */
public class UserTest extends BaseDao {

    @Test
    public void user() {
        List<User> userList = userPage(2, 20);
        Assert.assertEquals(20, userList.size());
        // 如果能转换成功说明是走了分页插件的
        Assert.assertTrue(userList instanceof PageList);
        // 如果数值正确说明是走了 select count 查询的
        Assert.assertEquals(200, ((PageList) userList).getTotal());

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
}
