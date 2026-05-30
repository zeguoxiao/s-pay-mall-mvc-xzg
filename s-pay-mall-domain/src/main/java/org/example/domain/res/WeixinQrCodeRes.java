package org.example.domain.res;

import lombok.Data;

/**
 * @description 获取微信登录二维码响应对象
 */
@Data
public class WeixinQrCodeRes {

    private String ticket;
    private Long expire_seconds;// 二维码有效时间
    private String url;// 二维码图片的URL

}
