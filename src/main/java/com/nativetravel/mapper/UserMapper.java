package com.nativetravel.mapper;

import com.nativetravel.dto.UserDto;
import com.nativetravel.entity.Role;
import com.nativetravel.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct маппер для преобразования сущности User в UserDto.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRolesToStrings")
    UserDto toDto(User user);

    @Named("mapRolesToStrings")
    default Set<String> mapRolesToStrings(Set<Role> roles) {
        if (roles == null) {
            return Collections.emptySet();
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}
