package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import com.sky.vo.ShoppingCartVO;
import com.sky.vo.setmeal.SetmealListVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Api("购物车相关接口")
@Slf4j
public class ShoppingCartController {

    @Autowired
    ShoppingCartService shoppingCartService;

    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCartVO>> list(){
        log.info("查看购物车");
        return Result.success(shoppingCartService.list());
    }

    @GetMapping("/add")
    @ApiOperation("新增商品到购物车")
    public Result<String> add(@RequestBody ShoppingCartDTO dto){
        log.info("新增商品到购物车");
        shoppingCartService.add(dto);
        return Result.success();
    }

}
