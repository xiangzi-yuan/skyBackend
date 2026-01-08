package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.converter.addressbook.AddressBookReadConvert;
import com.sky.converter.addressbook.AddressBookWriteConvert;
import com.sky.dto.addressbook.AddressBookCreateDTO;
import com.sky.dto.addressbook.AddressBookUpdateDTO;
import com.sky.entity.AddressBook;
import com.sky.exception.AddressBookBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import com.sky.vo.addressbook.AddressBookVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * 地址簿服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AddressBookServiceImpl implements AddressBookService {

    private final AddressBookMapper addressBookMapper;

    private final AddressBookWriteConvert writeConvert;

    private final AddressBookReadConvert readConvert;

    @Override
    public void add(AddressBookCreateDTO dto) {
        Long userId = BaseContext.getCurrentId();

        AddressBook addressBook = writeConvert.fromCreateDTO(dto);
        addressBook.setUserId(userId);
        addressBook.setIsDefault(0); // 新增默认不设为默认地址

        addressBookMapper.insert(addressBook);
        log.info("新增地址成功，用户ID：{}，地址ID：{}", userId, addressBook.getId());
    }

    @Override
    public List<AddressBookVO> list() {
        Long userId = BaseContext.getCurrentId();
        List<AddressBook> addressBookList = addressBookMapper.listByUserId(userId);
        return readConvert.toVOList(addressBookList);
    }

    @Override
    public AddressBookVO getDefault() {
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = addressBookMapper.getDefaultByUserId(userId);
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.DEFAULT_ADDRESS_NOT_FOUND);
        }
        return readConvert.toVO(addressBook);
    }

    @Override
    public AddressBookVO getById(Long id) {
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = addressBookMapper.getByIdAndUserId(id, userId);
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_NOT_FOUND);
        }
        return readConvert.toVO(addressBook);
    }

    @Override
    public void update(AddressBookUpdateDTO dto) {
        Long userId = BaseContext.getCurrentId();

        // 先查询确认地址存在且属于当前用户
        AddressBook addressBook = addressBookMapper.getByIdAndUserId(dto.getId(), userId);
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_NOT_FOUND);
        }

        // 合并更新
        writeConvert.mergeUpdate(dto, addressBook);
        addressBook.setUserId(userId); // 确保 userId 用于 SQL 条件

        int rows = addressBookMapper.updateByIdAndUserId(addressBook);
        if (rows == 0) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_NOT_FOUND);
        }
        log.info("更新地址成功，地址ID：{}", dto.getId());
    }

    @Override
    public void delete(Long id) {
        Long userId = BaseContext.getCurrentId();
        int rows = addressBookMapper.deleteByIdAndUserId(id, userId);
        if (rows == 0) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_NOT_FOUND);
        }
        log.info("删除地址成功，地址ID：{}", id);
    }

    @Override
    @Transactional
    public void setDefault(Long id) {
        Long userId = BaseContext.getCurrentId();

        // 1. 清除当前用户所有默认地址
        addressBookMapper.clearDefaultByUserId(userId);

        // 2. 设置指定地址为默认
        int rows = addressBookMapper.setDefaultByIdAndUserId(id, userId);
        if (rows == 0) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_NOT_FOUND);
        }
        log.info("设置默认地址成功，地址ID：{}", id);
    }
}
