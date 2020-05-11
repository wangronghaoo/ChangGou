package com.changgou.service;

import java.io.IOException;

public interface PageService {

    //创建静态页面
    void createHtml(String spuId) throws IOException;
}
