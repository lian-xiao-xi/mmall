package com.mmall.service.impl;

import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileService implements IFileService {
    private final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Override
    public String upload(MultipartFile file, String path) {
        String filename = file.getOriginalFilename();
        String fileExtensionName = filename.substring(filename.lastIndexOf(".") + 1);
        String uploadFileName = UUID.randomUUID().toString() + "." + fileExtensionName;
        logger.info("开始上传文件,上传文件的文件名:{},上传的路径:{},新文件名:{}",filename,path,uploadFileName);

        // 文件上传目录
        File fileDir = new File(path);
        if(!fileDir.exists()) {
            fileDir.setWritable(true);
            // 创建此目录
            fileDir.mkdir();
        }
        File targetFile = new File(fileDir, uploadFileName);
        try {
            // 文件上传成功了
            file.transferTo(targetFile);
            ArrayList<File> fileList = new ArrayList<>(Collections.singletonList(targetFile));
            //上传到ftp服务器是否成功
            boolean isUploadSuccess = FTPUtil.uploadFile(fileList);
            // 删除文件
            targetFile.delete();
        } catch (IOException e) {
            logger.error("文件上传异常", e);
            e.printStackTrace();
            return null;
        }
        return targetFile.getName();
    }
}
