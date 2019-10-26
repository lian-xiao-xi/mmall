package com.mmall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

public class FTPUtil {
    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);
    private final String ip = PropertiesUtil.getProperty("ftp.server.ip");
    private final String user = PropertiesUtil.getProperty("ftp.user");
    private final String pwd = PropertiesUtil.getProperty("ftp.pass");
    private final Integer port = Integer.valueOf(Objects.requireNonNull(PropertiesUtil.getProperty("ftp.server.port")));
    private FTPClient ftpClient;

    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil();
        logger.info("开始连接ftp服务器");
        boolean result = ftpUtil.uploadFile("img",fileList);
        logger.info("开始连接ftp服务器,结束上传,上传结果:");
        return result;
    }

    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException {
        boolean uploaded = true;
        FileInputStream fis = null;
        // 连接FTP服务器
        boolean isConnect = this.connectServer(this.ip, this.port, this.user, this.pwd);
        if(isConnect) {
            try {
                this.ftpClient.changeWorkingDirectory(remotePath);
                this.ftpClient.setBufferSize(1024);
                this.ftpClient.setControlEncoding(StandardCharsets.UTF_8.name());
                this.ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                for(File file:fileList) {
                    fis = new FileInputStream(file);
                    this.ftpClient.storeFile(file.getName(), fis);
                }
            } catch (IOException e) {
                logger.error("上传文件异常",e);
                uploaded = false;
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    fis.close();
                }
                if(this.ftpClient.isConnected()) {
                    this.ftpClient.logout();
                    this.ftpClient.disconnect();
                }
            }
        }
        return uploaded;
    }

    private boolean connectServer(String ip, Integer port, String user, String pwd) {
        this.ftpClient = new FTPClient();
        boolean isSuccess = false;
        try {
            ftpClient.connect(ip, port);
//            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user, pwd);
        } catch (IOException e) {
            logger.error("连接FTP服务器异常", e);
            e.printStackTrace();
        }
        return isSuccess;
    }

}
