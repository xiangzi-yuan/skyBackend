package com.sky.controller.user;

import com.sky.dto.addressbook.AddressBookCreateDTO;
import com.sky.dto.addressbook.AddressBookUpdateDTO;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import com.sky.vo.addressbook.AddressBookVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * C端-地址簿接口
 */
@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "C端-地址簿接口")
@Slf4j
@RequiredArgsConstructor
public class AddressBookController {

    private final AddressBookService addressBookService;

    /**
     * 新增地址
     */
    @PostMapping
    @ApiOperation("新增地址")
    public Result<Void> add(@Valid @RequestBody AddressBookCreateDTO dto) {
        log.info("新增地址：{}", dto);
        addressBookService.add(dto);
        return Result.success();
    }

    /**
     * 查询当前登录用户的所有地址信息
     */
    @GetMapping("/list")
    @ApiOperation("查询所有地址")
    public Result<List<AddressBookVO>> list() {
        log.info("查询所有地址");
        return Result.success(addressBookService.list());
    }

    /**
     * 查询默认地址
     */
    @GetMapping("/default")
    @ApiOperation("查询默认地址")
    public Result<AddressBookVO> getDefault() {
        log.info("查询默认地址");
        return Result.success(addressBookService.getDefault());
    }

    /**
     * 根据ID查询地址
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询地址")
    public Result<AddressBookVO> getById(@PathVariable Long id) {
        log.info("根据ID查询地址：{}", id);
        return Result.success(addressBookService.getById(id));
    }

    /**
     * 修改地址
     */
    @PutMapping
    @ApiOperation("修改地址")
    public Result<Void> update(@Valid @RequestBody AddressBookUpdateDTO dto) {
        log.info("修改地址：{}", dto);
        addressBookService.update(dto);
        return Result.success();
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除地址")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除地址：{}", id);
        addressBookService.delete(id);
        return Result.success();
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result<Void> setDefault(@RequestBody AddressBookUpdateDTO dto) {
        log.info("设置默认地址：{}", dto.getId());
        addressBookService.setDefault(dto.getId());
        return Result.success();
    }
}
