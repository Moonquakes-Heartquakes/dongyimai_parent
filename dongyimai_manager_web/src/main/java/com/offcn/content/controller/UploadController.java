package com.offcn.content.controller;

import com.offcn.entity.Result;
import com.offcn.utils.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Auther: ysp
 * @Date: 2020/9/27 09:45
 * @Description: 上传文件
 */
@RestController
@RequestMapping("/upload")
public class UploadController {

    //加载属性文件中的值
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @RequestMapping("/uploadFile")
    public Result uploadFile(MultipartFile file) {
        //1.读取文件的全路径    xxx.jpg
        String fileName = file.getOriginalFilename();
        //2.截取文件的扩展名    xxx.jpg ---》jpg
        String extName = fileName.substring(fileName.lastIndexOf(".") + 1);

        try {
            //3.使用工具类完成文件上传
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            String path = fastDFSClient.uploadFile(file.getBytes(), extName);
            //4.拼接上传文件的路径
            String url = FILE_SERVER_URL + path;
            return new Result(true, url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }

    }


}
