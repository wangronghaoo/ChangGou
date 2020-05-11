package com.changgou.user.feign;

import com.changgou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;


@FeignClient(name = "user")
public interface AddressFeign {

    @GetMapping("/address/findAddress")
    Result<Map> findAddress();
}
