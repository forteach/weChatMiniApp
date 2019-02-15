package com.forteach.wechat.mini.app.web.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Auther: zhangyy
 * @Email: zhang10092009@hotmail.com
 * @Date: 19-1-8 15:29
 * @Version: 1.0
 * @Description:
 */
@Data
@ApiModel(value = "绑定学生用户登录微信信息")
public class BindingUserInfoReq {

    @ApiModelProperty(value = "微信openId", name = "openId")
    private String openId;

    @ApiModelProperty(value = "用户名", name = "userName")
    private String userName;

    @ApiModelProperty(value = "身份证号码", name = "idCardNo")
    private String idCardNo;
}
