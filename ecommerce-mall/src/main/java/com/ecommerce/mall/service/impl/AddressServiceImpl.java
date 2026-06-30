package com.ecommerce.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ecommerce.common.exception.BusinessException;
import com.ecommerce.common.result.ResultCode;
import com.ecommerce.mall.entity.Address;
import com.ecommerce.mall.mapper.AddressMapper;
import com.ecommerce.mall.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    @Override
    public List<Address> getAddressList(Long userId) {
        return addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreateTime));
    }

    @Override
    public Address getAddressById(Long id, Long userId) {
        Address address = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getId, id)
                .eq(Address::getUserId, userId));
        if (address == null) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }
        return address;
    }

    @Override
    @Transactional
    public void addAddress(Address address) {
        Long count = addressMapper.selectCount(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, address.getUserId()));
        if (count >= 20) {
            throw new BusinessException(ResultCode.ADDRESS_LIMIT);
        }
        // 如果是第一个地址，自动设为默认
        if (count == 0) {
            address.setIsDefault(1);
        }
        // 如果新增地址设为默认，取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, address.getUserId())
                    .set(Address::getIsDefault, 0));
        }
        addressMapper.insert(address);
    }

    @Override
    @Transactional
    public void updateAddress(Address address) {
        Address exist = addressMapper.selectById(address.getId());
        if (exist == null || !exist.getUserId().equals(address.getUserId())) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }
        // 如果编辑后设为默认，取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, address.getUserId())
                    .set(Address::getIsDefault, 0));
        }
        addressMapper.updateById(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long id, Long userId) {
        Address address = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getId, id).eq(Address::getUserId, userId));
        if (address == null) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }
        addressMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void setDefault(Long id, Long userId) {
        Address address = getAddressById(id, userId);
        // 取消其他默认
        addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0));
        // 设置当前为默认
        address.setIsDefault(1);
        addressMapper.updateById(address);
    }
}