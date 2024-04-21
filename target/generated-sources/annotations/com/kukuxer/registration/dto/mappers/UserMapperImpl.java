package com.kukuxer.registration.dto.mappers;

import com.kukuxer.registration.domain.user.User;
import com.kukuxer.registration.dto.UserDTO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-04-18T20:04:46+0100",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 21.0.2 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(UserDTO dto) {
        if ( dto == null ) {
            return null;
        }

        User user = new User();

        user.setId( dto.getId() );
        user.setEmail( dto.getEmail() );
        user.setPassword( dto.getPassword() );
        user.setUsername( dto.getUsername() );

        return user;
    }

    @Override
    public List<User> toEntity(List<UserDTO> dto) {
        if ( dto == null ) {
            return null;
        }

        List<User> list = new ArrayList<User>( dto.size() );
        for ( UserDTO userDTO : dto ) {
            list.add( toEntity( userDTO ) );
        }

        return list;
    }

    @Override
    public UserDTO toDto(User entity) {
        if ( entity == null ) {
            return null;
        }

        UserDTO userDTO = new UserDTO();

        userDTO.setId( entity.getId() );
        userDTO.setUsername( entity.getUsername() );
        userDTO.setEmail( entity.getEmail() );
        userDTO.setPassword( entity.getPassword() );

        return userDTO;
    }

    @Override
    public List<UserDTO> toDto(List<User> entity) {
        if ( entity == null ) {
            return null;
        }

        List<UserDTO> list = new ArrayList<UserDTO>( entity.size() );
        for ( User user : entity ) {
            list.add( toDto( user ) );
        }

        return list;
    }
}
