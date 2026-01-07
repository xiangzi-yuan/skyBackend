package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import com.sky.vo.ShoppingCartVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user/shoppingCart")
@Api("购物车相关接口")
@Slf4j
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCartVO>> list() {
        log.info("查看购物车");
        return Result.success(shoppingCartService.list());
    }

    @PostMapping("/add")
    @ApiOperation("新增商品到购物车")
    public Result<String> add(@RequestBody ShoppingCartDTO dto) {
        log.info("新增商品到购物车");
        shoppingCartService.add(dto);
        return Result.success();
    }

    @PostMapping("/sub")
    @ApiOperation("删除单个商品")
    public Result<Void> sub(@RequestBody ShoppingCartDTO dto) {
        log.info("删除单个商品");
        shoppingCartService.sub(dto);
        return Result.success();
    }

    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result<Void> clean() {
        log.info("清空购物车");
        shoppingCartService.clean();
        return Result.success();
    }

}
