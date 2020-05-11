package com.changgou;

import org.apache.commons.io.IOUtils;
import org.csource.fastdfs.*;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


public class FileTest {


    /**
     * 上传文件
     * @throws Exception
     */
    @Test
    public void uploadTest() throws Exception{
        //初始化,加载配置文件
        ClientGlobal.init("D:\\Develop\\Application\\changgou_parent\\changgou_service\\changgou_service_file\\src\\main\\resources\\fastdfs_client.conf");


        /*
        * Tracker server 作用是负载均衡和调度，通过 Tracker server 在文件上传时可以根据一
            些策略找到Storage server 提供文件上传服务*/
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
        StorageClient storageClient = new StorageClient(trackerServer,storageServer);
        //数组:组名和随机生成的md5
        String[] file1 = storageClient.upload_file("C:\\Users\\18198\\Desktop\\QQ图片20191230132957.jpg", "jpg", null);
        for (String s : file1) {
            System.out.println(s);  //group1
            //M00/00/00/wKjIgF4Jpc-AOpS0AALEw49iJDA299.jpg
        }
        trackerServer.close();
        storageServer.close();
    }


    /**
     * 下载文件
     */
    @Test
    public void downloadTest() throws Exception{

        ClientGlobal.init("D:\\Develop\\Application\\changgou_parent\\changgou_service\\changgou_service_file\\src\\main\\resources\\fastdfs_client.conf");
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
        StorageClient1 storageClient1 = new StorageClient1(trackerServer,storageServer);
        byte[] file = storageClient1.download_file("group1", "M00/00/00/wKjIgF4Jpc-AOpS0AALEw49iJDA299.jpg");
        FileOutputStream fos = new FileOutputStream(new File("D:\\Develop\\下载文件\\20.jpg"));
        IOUtils.write(file,fos);
        trackerServer.close();
        storageServer.close();
    }

    /**
     * 删除文件
     */

    @Test
    public void deleteTest() throws Exception{
        //创建global客户端
        ClientGlobal.init("D:\\Develop\\Application\\changgou_parent\\changgou_service\\changgou_service_file\\src\\main\\resources\\fastdfs_client.conf");
        //创建trackerclient
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
        StorageClient storageClient = new StorageClient(trackerServer,storageServer);
        int i = storageClient.delete_file("group1", "M00/00/00/wKjIgF4Jpc-AOpS0AALEw49iJDA299.jpg");
        System.out.println(i);
        storageServer.close();
        trackerServer.close();

    }
}
