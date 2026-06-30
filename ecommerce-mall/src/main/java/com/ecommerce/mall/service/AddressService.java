package com.ecommerce.mall.service;

import com.ecommerce.mall.entity.Address;
import java.util.List;

public interface AddressService {
    List<Address> getAddressList(Long userId);
    Address getAddressById(Long id, Long userId);
    void addAddress(Address address);
    void updateAddress(Address address);
    void deleteAddress(Long id, Long userId);
    void setDefault(Long id, Long userId);
}