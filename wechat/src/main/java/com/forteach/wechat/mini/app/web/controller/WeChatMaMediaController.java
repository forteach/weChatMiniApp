package com.forteach.wechat.mini.app.web.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.constant.WxMaConstants;
import com.forteach.wechat.mini.app.common.WebResult;
import com.forteach.wechat.mini.app.config.WeChatMiniAppConfig;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author zhangyy
 * @Email: zhang10092009@hotmail.com
 * @Date: 19-1-10 15:33
 * @Version: 1.0
 * @Description:
 */
@Api(value = "用户操作文件相关接口", description = "微信调用操作文件相关接口", tags = {"操作文件"})
@RestController
@RequestMapping("/media")
public class WeChatMaMediaController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @ApiOperation(value = "微信上传文件")
    @PostMapping("/upload")
    public WebResult uploadMedia(HttpServletRequest request) throws WxErrorException {
        final WxMaService wxService = WeChatMiniAppConfig.getMaService();

        CommonsMultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());

        if (!resolver.isMultipart(request)) {
            return WebResult.okResult(Lists.newArrayList());
        }

        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        Iterator<String> it = multiRequest.getFileNames();
        List<String> result = Lists.newArrayList();
        while (it.hasNext()) {
            try {
                MultipartFile file = multiRequest.getFile(it.next());
                File newFile = new File(Files.createTempDir(), file.getOriginalFilename());
                this.logger.info("filePath is ：" + newFile.toString());
                file.transferTo(newFile);
                WxMediaUploadResult uploadResult = wxService.getMediaService().uploadMedia(WxMaConstants.KefuMsgType.IMAGE, newFile);
                this.logger.info("media_id ： " + uploadResult.getMediaId());
                result.add(uploadResult.getMediaId());
            } catch (IOException e) {
                this.logger.error(e.getMessage(), e);
            }
        }
        return WebResult.okResult(result);
    }

    /**
     * 下载临时素材
     */
    @ApiOperation(value = "下载微信文件")
    @ApiImplicitParam(value = "文件对应的mediaId", name = "mediaId", required = true, dataType = "string", paramType = "query")
    @GetMapping("/download/{mediaId}")
    public File getMedia(@PathVariable("mediaId") String mediaId) throws WxErrorException {
        final WxMaService wxService = WeChatMiniAppConfig.getMaService();
        return wxService.getMediaService().getMedia(mediaId);
    }
}
