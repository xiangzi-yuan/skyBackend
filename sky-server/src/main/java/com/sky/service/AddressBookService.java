package com.sky.service;

import com.sky.dto.addressbook.AddressBookCreateDTO;
import com.sky.dto.addressbook.AddressBookUpdateDTO;
import com.sky.vo.addressbook.AddressBookVO;

import java.util.List;

/**
 * 地址簿服务接口
 */
public interface AddressBookService {

    /**
     * 新增地址
     */
    void add(AddressBookCreateDTO dto);

    /**
     * 查询当前用户所有地址
     */
    List<AddressBookVO> list();

    /**
     * 查询默认地址
     */
    AddressBookVO getDefault();

    /**
     * 根据 ID 查询地址
     */
    AddressBookVO getById(Long id);

    /**
     * 修改地址
     */
    void update(AddressBookUpdateDTO dto);

    /**
     * 删除地址
     */
    void delete(Long id);

    /**
     * 设置默认地址
     */
    void setDefault(Long id);
}
