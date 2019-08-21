package com.github.liuanxin.page.render;

import com.github.liuanxin.page.model.PageList;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 将 PageList 渲染成 model, 放入 spring mvc 的渲染上下文中, 使用拦截器实现
 *
 * @author https://github.com/liuanxin
 */
public class PageListToPageInterceptor implements HandlerInterceptor {

    private static final String SUFFIX = "Total";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        return true;
    }

    /** 如果在当前上下文中已经放了对象的属性, 则判断有没有 PageList, 有就显式的将总条数也放入上下文 */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            Map<String, Object> newModel = new HashMap<>();
            for (Map.Entry<String, Object> item : modelAndView.getModel().entrySet()) {
                Object value = item.getValue();
                if (value instanceof PageList) {
                    newModel.put(item.getKey() + SUFFIX, ((PageList) value).getTotal());
                }
            }
            if (!newModel.isEmpty()) {
                modelAndView.addAllObjects(newModel);
            }
        }
        // 在 model 和 request 中都放
        Enumeration enumeration = request.getAttributeNames();
        while (enumeration.hasMoreElements()) {
            Object element = enumeration.nextElement();
            if (element instanceof String) {
                String name = (String) element;
                Object attribute = request.getAttribute(name);
                if (attribute instanceof PageList) {
                    request.setAttribute(name + SUFFIX, ((PageList) attribute).getTotal());
                }
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
    }
}
