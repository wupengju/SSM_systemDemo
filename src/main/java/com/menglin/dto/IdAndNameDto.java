package com.menglin.dto;

import org.springframework.stereotype.Component;

@Component
public class IdAndNameDto {
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IdAndNameDto createNewIdAndNameDto(Long id, String name) {
        IdAndNameDto idAndNameDto = new IdAndNameDto();
        idAndNameDto.setId(id);
        idAndNameDto.setName(name);
        return idAndNameDto;
    }
}
