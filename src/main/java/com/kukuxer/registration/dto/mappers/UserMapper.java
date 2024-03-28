package com.kukuxer.registration.dto.mappers;



import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.dto.UserDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper extends Mappable<User, UserDTO> {
}
