package com.menglin.dto;

import com.google.common.base.Converter;
import com.menglin.entity.Admin;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class AdminDto extends UserDto {
    public Admin convertToAdmin() {
        AdminDtoToAdminConverter adminDtoToAdminConverter = new AdminDtoToAdminConverter();
        return adminDtoToAdminConverter.convert(this);
    }

    public AdminDto convertFor(Admin admin) {
        AdminDtoToAdminConverter adminDtoToAdminConverter = new AdminDtoToAdminConverter();
        return adminDtoToAdminConverter.reverse().convert(admin);
    }

    private static class AdminDtoToAdminConverter extends Converter<AdminDto, Admin> {

        @Override
        protected Admin doForward(AdminDto adminDto) {
            Admin admin = new Admin();
            BeanUtils.copyProperties(adminDto, admin);
            return admin;
        }

        @Override
        protected AdminDto doBackward(Admin admin) {
            AdminDto adminDto = new AdminDto();
            BeanUtils.copyProperties(admin, adminDto);
            return adminDto;
        }
    }
}
