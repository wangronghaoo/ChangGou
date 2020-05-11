package com.changgou.file.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.file.pojo.FastDFSFile;
import com.changgou.file.util.FastDFSClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/file")
public class FileUploadController {


    /**
     * 上传文件
     * @param file
     * @return
     * @throws
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file){
        //StorageClient storageClient = getStorageClient("D:\\Develop\\Application\\changgou_parent\\changgou_service\\changgou_service_file\\src\\main\\resources\\fastdfs_client.conf");
        //上传文件请求头headers Content-Type  multipart/form-data

        try {
            if (file == null){
                throw new RuntimeException("文件不存在");
            }
            //获取文件名称
            String originalFilename = file.getOriginalFilename();
            if (StringUtils.isEmpty(originalFilename)){
                throw new RuntimeException("文件不存在");
            }

            //获取文件扩展名   abc.jpg
            //获取最后一个点所在位置
            int lastIndexOf = originalFilename.lastIndexOf(".");
            //对点之后的进行切割,这里+1是因为把点带上  不带则为.jpg
            String extName = originalFilename.substring(lastIndexOf + 1);

            //获取文件内容
            byte[] content = file.getBytes();
            FastDFSFile fastDFSFile = new FastDFSFile(originalFilename,content,extName);
            String[] upload = FastDFSClient.upload(fastDFSFile);

            //组名,存储路径
            String url = FastDFSClient.getTrackerUrl() + upload[0] +"/" +  upload[1];
            //封装返回值
            return new Result(true, StatusCode.OK,"上传文件成功",url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false, StatusCode.ERROR,"上传文件失败");

    }


    /**
     * 下载文件
     */

    @PostMapping("/download")
    public Result download(MultipartFile file ,String groupName,String remote_Name ){
        try {
            String originalFilename = file.getOriginalFilename();
            //下载文件存储路径
            StorageClient storageClient = getStorageClient();
            byte[] bytes = storageClient.download_file(groupName, remote_Name);
            FileOutputStream fos = new FileOutputStream(new File("D:\\Develop\\下载文件\\" + originalFilename));
            IOUtils.write(bytes,fos);
            fos.close();
            return new Result(true,StatusCode.OK,"文件下载成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false,StatusCode.ERROR,"文件下载失败");

    }


    /**
     * 删除文件
     */
    @PostMapping("/delete")
    public Result delete(String groupName,String remote_Name){
        try {
            StorageClient storageClient = getStorageClient();
            storageClient.delete_file(groupName,remote_Name);
            return new Result(true,StatusCode.OK,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(false,StatusCode.ERROR,"删除失败");
    }



    /**
     * 公共初始方法
     */

    private StorageClient getStorageClient() throws IOException, MyException {
        ClientGlobal.init("D:\\Develop\\Application\\changgou_parent\\changgou_service\\changgou_service_file\\src\\main\\resources\\fastdfs_client.conf");
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
        trackerServer.close();
        storageServer.close();
        return new StorageClient(trackerServer,storageServer);
    }
}
