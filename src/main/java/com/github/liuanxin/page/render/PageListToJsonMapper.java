package com.github.liuanxin.page.render;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.liuanxin.page.model.PageList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** 将 PageList 渲染成 json, 使用 Jackson 序列化 */
public class PageListToJsonMapper extends ObjectMapper {

    /**
     * 将数据返回时, 只输出 总条数 和 当前页的数据.
     *
     * 前端根据 page limit(此两个值由他们传入) 和 总条数 三个值, 输出相关的 1 2 3 及置灰相关的按钮等
     */
    public PageListToJsonMapper() {
        super();

        registerModule(new SimpleModule().addSerializer(PageList.class, new JsonSerializer<PageList>() {
            @Override
            @SuppressWarnings("unchecked")
            public void serialize(PageList value, JsonGenerator gen, SerializerProvider sp) throws IOException {
                Map<String, Object> returnMap = new HashMap<String, Object>();
                returnMap.put("items", new ArrayList(value));
                returnMap.put("total", value.getTotal());

                gen.writeObject(returnMap);
            }
        }));
    }
}
