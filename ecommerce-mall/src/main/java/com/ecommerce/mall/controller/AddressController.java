package com.ecommerce.mall.controller;

import com.ecommerce.common.result.Result;
import com.ecommerce.mall.entity.Address;
import com.ecommerce.mall.service.AddressService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/list")
    public Result<List<Address>> getAddressList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(addressService.getAddressList(userId));
    }

    @GetMapping("/{id}")
    public Result<Address> getAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(addressService.getAddressById(id, userId));
    }

    @PostMapping
    public Result<Void> addAddress(@RequestBody Address address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        address.setUserId(userId);
        addressService.addAddress(address);
        return Result.success();
    }

    @PutMapping
    public Result<Void> updateAddress(@RequestBody Address address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        address.setUserId(userId);
        addressService.updateAddress(address);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        addressService.deleteAddress(id, userId);
        return Result.success();
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        addressService.setDefault(id, userId);
        return Result.success();
    }
}