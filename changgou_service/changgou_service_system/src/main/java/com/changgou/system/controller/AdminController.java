package com.changgou.system.controller;
import com.changgou.entity.PageResult;
import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.system.pojo.Admin;
import com.changgou.system.service.AdminService;
import com.github.pagehelper.Page;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/admin")
public class AdminController {


    @Autowired
    private AdminService adminService;

    /**
     * 查询全部数据
     * @return
     */
    @GetMapping
    public Result findAll(){
        List<Admin> adminList = adminService.findAll();
        return new Result(true, StatusCode.OK,"查询成功",adminList) ;
    }

    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id){
        Admin admin = adminService.findById(id);
        return new Result(true,StatusCode.OK,"查询成功",admin);
    }


    /***
     * 新增数据
     * @param admin
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Admin admin){
        adminService.add(admin);
        return new Result(true,StatusCode.OK,"添加成功");
    }


    /***
     * 修改数据
     * @param admin
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody Admin admin,@PathVariable Integer id){
        admin.setId(id);
        adminService.update(admin);
        return new Result(true,StatusCode.OK,"修改成功");
    }


    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable Integer id){
        adminService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search" )
    public Result findList(@RequestParam Map searchMap){
        List<Admin> list = adminService.findList(searchMap);
        return new Result(true,StatusCode.OK,"\n" +
                "My Workspace\n" +
                "Sign In\n" +
                "SYNC OFF\n查询成功",list);
    }


    /***
     * 分页搜索实现
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result findPage(@RequestParam Map searchMap, @PathVariable  int page, @PathVariable  int size){
        Page<Admin> pageList = adminService.findPage(searchMap, page, size);
        PageResult pageResult=new PageResult(pageList.getTotal(),pageList.getResult());
        return new Result(true,StatusCode.OK,"查询成功",pageResult);
    }


    /**
     * 管理员登录
     * 1. 用户进入网关开始登陆，网关过滤器进行判断，如果是登录，则路由到后台管理微服务进
     * 行登录
     * 2. 用户登录成功，后台管理微服务签发JWT TOKEN信息返回给用户
     * 3. 用户再次进入网关开始访问，网关过滤器接收用户携带的TOKEN
     * 4. 网关过滤器解析TOKEN ，判断是否有权限，如果有，则放行，如果没有则返回未认证错误
     */

    @PostMapping("/login")
    public Result login(@RequestBody Admin admin){
        //根据用户名输入的用户名查找是否存在
        Boolean result = adminService.findByLoginName(admin);

        byte[] encodeKey = Base64.getDecoder().decode("wangronghao");
        SecretKey secretKey = new SecretKeySpec(encodeKey,0,encodeKey.length,"AES");
        long timeMillis = System.currentTimeMillis();
        Date date = new Date(timeMillis);
        //过期时间默认为4小时
        Long time = timeMillis + 14400000L;
        Date timeOut = new Date(time);
        if (result){
            JwtBuilder token = Jwts.builder().setId(UUID.randomUUID().toString())  //id
                    .setIssuer("admin")    //发行人,签发者
                    .setIssuedAt(date)      //发布时期
                    .setExpiration(timeOut)            //过期时间
                    .signWith(SignatureAlgorithm.HS256,secretKey)
                    .setSubject(admin.getLoginName());    //主题
            Map<String,String> map = new HashMap<>();  //object报错
            map.put("username",admin.getLoginName());
            map.put("token",token.compact());
            return new Result(true,StatusCode.OK,"登录成功",map);
        }
        return new Result(false,StatusCode.LOGINERROR,"用户名或者密码错误");

    }


}
