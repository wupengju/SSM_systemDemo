package com.menglin.dto;

import com.google.common.base.Converter;
import com.menglin.entity.Role;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class RoleDto extends BaseTableDto {
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Role convertToRole() {
        RoleDto.RoleDtoToRoleConverter roleDtoToRoleConverter = new RoleDto.RoleDtoToRoleConverter();
        return roleDtoToRoleConverter.convert(this);
    }

    public RoleDto convertFor(Role role) {
        RoleDto.RoleDtoToRoleConverter roleDtoToRoleConverter = new RoleDto.RoleDtoToRoleConverter();
        return roleDtoToRoleConverter.reverse().convert(role);
    }

    private static class RoleDtoToRoleConverter extends Converter<RoleDto, Role> {

        @Override
        protected Role doForward(RoleDto roleDto) {
            Role role = new Role();
            BeanUtils.copyProperties(roleDto, role);
            return role;
        }

        @Override
        protected RoleDto doBackward(Role role) {
            RoleDto roleDto = new RoleDto();
            BeanUtils.copyProperties(role, roleDto);
            return roleDto;
        }
    }
}
