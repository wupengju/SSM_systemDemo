package com.menglin.dto;

import com.google.common.base.Converter;
import com.menglin.entity.Permission;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class PermissionDto extends BaseTableDto {
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Permission convertToPermission() {
        PermissionDto.PermissionDtoToPermissionConverter permissionDtoToPermissionConverter = new PermissionDto.PermissionDtoToPermissionConverter();
        return permissionDtoToPermissionConverter.convert(this);
    }

    public PermissionDto convertFor(Permission permission) {
        PermissionDto.PermissionDtoToPermissionConverter permissionDtoToPermissionConverter = new PermissionDto.PermissionDtoToPermissionConverter();
        return permissionDtoToPermissionConverter.reverse().convert(permission);
    }

    private static class PermissionDtoToPermissionConverter extends Converter<PermissionDto, Permission> {

        @Override
        protected Permission doForward(PermissionDto permissionDto) {
            Permission permission = new Permission();
            BeanUtils.copyProperties(permissionDto, permission);
            return permission;
        }

        @Override
        protected PermissionDto doBackward(Permission permission) {
            PermissionDto permissionDto = new PermissionDto();
            BeanUtils.copyProperties(permission, permissionDto);
            return permissionDto;
        }
    }
}
