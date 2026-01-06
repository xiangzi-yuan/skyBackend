package com.sky.converter.addressbook;

import com.sky.entity.AddressBook;
import com.sky.vo.addressbook.AddressBookVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 地址簿读转换器 - 负责读操作转换
 * 
 * <p>
 * 包含：Entity -> VO 的转换
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressBookReadConvert {

    /**
     * Entity -> VO
     */
    AddressBookVO toVO(AddressBook addressBook);

    /**
     * Entity List -> VO List
     */
    List<AddressBookVO> toVOList(List<AddressBook> addressBookList);
}
