package com.choice.cloud.versioncontrol.controller;

import com.choice.cloud.versioncontrol.config.VersionConfiguration;
import com.choice.cloud.versioncontrol.util.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 版本控制
 * @author  jty
 * @since JDK8
 */
@Slf4j
@Controller
@RequestMapping("/upload")
public class UploadVersionController {
    @Autowired
    private OssUtil ossUtil;
    @Autowired
    private VersionConfiguration versionConfiguration;

    @PostMapping(value = "/version")
    public String uploadVersion(@RequestParam("file") MultipartFile file, Model modelMap) {
        if (file.isEmpty()) {
            modelMap.addAttribute("message", "The file is empty!");
            return "/uploadStatus";
        }
        // 获取文件名
        String fileName = file.getOriginalFilename();
        // 获取文件的后缀名
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        // 文件上传路径
        String filePath = "d:/uploadVersion";
        // 解决中文问题,liunx 下中文路径,图片显示问题
        //fileName = UUID.randomUUID() + suffixName;
        File dest = new File(filePath + fileName);
        log.info("file路径={}",filePath + fileName);
        // 检测是否存在目录
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
         file.transferTo(dest);
         String md5 = DigestUtils.md5DigestAsHex(new FileInputStream(filePath+fileName));
         log.info("上传文件成功，md5={}",md5);
         String url = ossUtil.upload(dest,versionConfiguration.getBucketPrefix(), md5);
         log.info("OSS服务器上传成功，url={}",url);
         modelMap.addAttribute("message", "success");
        } catch (IllegalStateException e) {
        e.printStackTrace();
        } catch (IOException e) {
        e.printStackTrace();
        }
        return "/uploadStatus";
    }
}
