package org.example.domain.res;
import lombok.Data;

/**
 * @description 获取 Access token DTO 对象
 */
@Data
public class WeixinTokenRes {

    private String access_token;
    private int expires_in;
    private String errcode;
    private String errmsg;

}
