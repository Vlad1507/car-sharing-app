package com.app.carsharing.util;

import com.app.carsharing.dto.user.UserDto;
import com.app.carsharing.dto.user.UserLoginRequestDto;
import com.app.carsharing.dto.user.UserRegistrationRequestDto;
import com.app.carsharing.model.Role;
import com.app.carsharing.model.User;
import java.util.Set;

public class UserUtil {

    public static User getUserBeforeDbSave() {
        User user = new User();
        user.setEmail("alice@gmail.com");
        user.setFirstName("Alice");
        user.setLastName("Bloom");
        user.setPassword("noSilA12*-3");
        user.setRoles(Set.of(getRoles()));
        return user;
    }

    public static User getUserAfterDbSave() {
        User user = new User();
        user.setId(1L);
        user.setEmail("alice@gmail.com");
        user.setFirstName("Alice");
        user.setLastName("Bloom");
        user.setPassword("noSilA12*-3");
        user.setRoles(Set.of(getRoles()));
        return user;
    }

    public static User getExistedUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("alice@gmail.com");
        user.setFirstName("Alice");
        user.setLastName("Bloom");
        user.setPassword("noSilA12*-3");
        user.setRoles(Set.of(getRoles()));
        return user;
    }

    public static UserDto getUserDto() {
        return new UserDto(
                1L,
                "alice@gmail.com",
                "Alice",
                "Bloom",
                Set.of(getRoles().getRoleName().name())
        );
    }

    public static UserDto getManagerUserDto() {
        return new UserDto(
                1L,
                "alice@gmail.com",
                "Alice",
                "Bloom",
                Set.of(Role.RoleName.ROLE_MANAGER.name())
        );
    }

    public static User getUpdatedUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("alice@gmail.com");
        user.setFirstName("Alia");
        user.setLastName("Blast");
        user.setPassword("noSilA12*-3");
        user.setRoles(Set.of(getRoles()));
        return user;
    }

    public static User getExistedUserWithNewRole() {
        Role role = new Role();
        role.setId(2L);
        role.setRoleName(Role.RoleName.ROLE_MANAGER);
        User user = new User();
        user.setId(1L);
        user.setEmail("alice@gmail.com");
        user.setFirstName("Alice");
        user.setLastName("Bloom");
        user.setPassword("noSilA12*-3");
        user.setRoles(Set.of(role));
        return user;
    }

    public static Role getRoles() {
        Role role = new Role();
        role.setId(1L);
        role.setRoleName(Role.RoleName.ROLE_CUSTOMER);
        return role;
    }

    public static UserRegistrationRequestDto getUserRegistrationRequestDto() {
        return new UserRegistrationRequestDto(
                "alice@gmail.com",
                "Alice",
                "Bloom",
                "noSilA12*-3",
                "noSilA12*-3"
        );
    }

    public static UserRegistrationRequestDto getRequestDtoInvalidPassword() {
        return new UserRegistrationRequestDto(
                "alice@gmail.com",
                "Alice",
                "Bloom",
                "InvalidPassword",
                "InvalidPassword"
        );
    }

    public static UserRegistrationRequestDto getRequestDtoWithInvalidEmail() {
        return new UserRegistrationRequestDto(
                "invalidEmail",
                "Alice",
                "Bloom",
                "noSilA12*-3",
                "noSilA12*-3");
    }

    public static UserLoginRequestDto getLoginRequestDto() {
        return new UserLoginRequestDto("alice@gmail.com", "noSilA12*-3");
    }

    public static UserLoginRequestDto getNoExistLoginRequestDto() {
        return new UserLoginRequestDto("john@gmail.com", "JonoSilA12*-3");
    }

    public static UserDto getUpdatedUserDto() {
        return new UserDto(
                1L,
                "alice@gmail.com",
                "Alex",
                "Stone",
                Set.of(getRoles().getRoleName().name())
        );
    }
}
