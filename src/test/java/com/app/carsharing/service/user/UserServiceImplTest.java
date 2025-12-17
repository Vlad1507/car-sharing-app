package com.app.carsharing.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.app.carsharing.dto.user.UpdateRoleRequestDto;
import com.app.carsharing.dto.user.UpdateUserRequestDto;
import com.app.carsharing.dto.user.UserDto;
import com.app.carsharing.dto.user.UserRegistrationRequestDto;
import com.app.carsharing.exception.EntityNotFoundException;
import com.app.carsharing.exception.RegistrationException;
import com.app.carsharing.exception.RoleUpdateException;
import com.app.carsharing.mapper.UserMapper;
import com.app.carsharing.model.Role;
import com.app.carsharing.model.User;
import com.app.carsharing.repository.role.RoleRepository;
import com.app.carsharing.repository.user.UserRepository;
import com.app.carsharing.util.UserUtil;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class UserServiceImplTest {
    private static final String PASSWORD_ENCODER_STUB = "FIXED_MOCKED_HASH_FOR_TESTING";

    @MockitoBean
    private final UserRepository userRepository;
    @MockitoBean
    private final UserMapper userMapper;
    @MockitoBean
    private final RoleRepository roleRepository;
    @MockitoBean
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Autowired
    public UserServiceImplTest(UserService userService,
                               UserMapper userMapper,
                               UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Test
    @DisplayName("Should register a new user")
    void register_successful_shouldRegisterUser() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto(
                "alice@gmail.com",
                "Alice",
                "Bloom",
                "noSilA12*-3",
                "noSilA12*-3"
        );
        User userBeforeDbSave = UserUtil.getUserBeforeDbSave();
        User userAfterDbSave = UserUtil.getUserAfterDbSave();
        Role role = UserUtil.getRoles();
        UserDto expected = new UserDto(
                userAfterDbSave.getId(),
                userAfterDbSave.getEmail(),
                userAfterDbSave.getFirstName(),
                userAfterDbSave.getLastName(),
                Set.of(role.getRoleName().name())
        );

        when(userRepository.existsByEmail(userBeforeDbSave.getEmail())).thenReturn(false);
        when(userMapper.toModel(requestDto)).thenReturn(userBeforeDbSave);
        when(passwordEncoder.encode(requestDto.password())).thenReturn(PASSWORD_ENCODER_STUB);
        when(roleRepository.findByRoleName(role.getRoleName())).thenReturn(Optional.of(role));
        when(userRepository.save(userBeforeDbSave)).thenReturn(userAfterDbSave);
        when(userMapper.toDto(userAfterDbSave)).thenReturn(expected);

        UserDto actual = userService.register(requestDto);
        assertNull(userBeforeDbSave.getId());
        assertNotNull(userBeforeDbSave);
        assertEquals(expected, actual);
        verify(passwordEncoder).encode(requestDto.password());
        verify(roleRepository).findByRoleName(Role.RoleName.ROLE_CUSTOMER);
        verify(userRepository).save(userBeforeDbSave);
    }

    @Test
    @DisplayName("An error should occur due to an attempt "
            + "to register an already registered email.")
    void register_duplicateEmail_shouldThrowException() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto(
                "alice@gmail.com",
                "Alice",
                "Bloom",
                "noSilA12*-3",
                "noSilA12*-3"
        );
        String expectedMessage = "User with email: " + requestDto.email()
                + " is already exist";

        when(userRepository.existsByEmail(any())).thenReturn(true);

        RegistrationException actual = assertThrows(RegistrationException.class,
                () -> userService.register(requestDto));
        assertEquals(expectedMessage, actual.getMessage());
    }

    @Test
    @DisplayName("Should change the role of an existing user")
    void updateRole_validRole_shouldUpdateRole() {
        User existedUser = UserUtil.getExistedUser();
        Role role = new Role();
        role.setRoleName(Role.RoleName.ROLE_MANAGER);
        User userWithNewRole = UserUtil.getExistedUserWithNewRole();
        UpdateRoleRequestDto updateRoleRequestDto = new UpdateRoleRequestDto(
                Role.RoleName.ROLE_MANAGER.name()
        );
        when(userRepository.findById(existedUser.getId())).thenReturn(Optional.of(existedUser));
        when(roleRepository.findByRoleName(Role.RoleName.ROLE_MANAGER))
                .thenReturn(Optional.of(role));
        when(userRepository.save(existedUser)).thenReturn(userWithNewRole);
        UserDto expected = new UserDto(
                existedUser.getId(),
                existedUser.getEmail(),
                existedUser.getFirstName(),
                existedUser.getLastName(),
                Set.of(Role.RoleName.ROLE_MANAGER.name())
        );
        when(userMapper.toDto(userWithNewRole)).thenReturn(expected);

        UserDto actual = userService.updateRole(existedUser.getId(), updateRoleRequestDto);
        assertTrue(userWithNewRole.getRoles().stream()
                .anyMatch(r -> r.getRoleName() == Role.RoleName.ROLE_MANAGER));
        assertFalse(userWithNewRole.getRoles().stream()
                .anyMatch(r -> r.getRoleName() == Role.RoleName.ROLE_CUSTOMER));
        assertEquals(expected, actual);
        verify(roleRepository).findByRoleName(Role.RoleName.ROLE_MANAGER);
        verify(userRepository).save(existedUser);
    }

    @Test
    @DisplayName("Should throw an error due to an invalid role name inserted")
    void updateRole_invalidRole_shouldThrowException() {
        UpdateRoleRequestDto updateRoleRequestDto = new UpdateRoleRequestDto("ROLE_FAKE");
        User existedUser = UserUtil.getExistedUser();
        String expected = "Invalid role name requested: " + updateRoleRequestDto.role();

        when(userRepository.findById(existedUser.getId())).thenReturn(Optional.of(existedUser));

        RoleUpdateException actual = assertThrows(RoleUpdateException.class,
                () -> userService.updateRole(existedUser.getId(), updateRoleRequestDto));
        verify(userRepository).findById(existedUser.getId());
        assertEquals(expected, actual.getMessage());
        verifyNoInteractions(roleRepository);
    }

    @Test
    @DisplayName("Should throw an error due to a valid role name is missed from the database")
    void updateRole_roleNotFoundInDB_shouldThrowException() {
        UpdateRoleRequestDto updateRoleRequestDto =
                new UpdateRoleRequestDto(Role.RoleName.ROLE_MANAGER.name());
        User existedUser = UserUtil.getExistedUser();

        when(userRepository.findById(existedUser.getId())).thenReturn(Optional.of(existedUser));
        when(roleRepository.findByRoleName(Role.RoleName.ROLE_MANAGER))
                .thenReturn(Optional.empty());

        String expected = "Role with name: " + updateRoleRequestDto.role() + " not found";
        EntityNotFoundException actual = assertThrows(EntityNotFoundException.class,
                () -> userService.updateRole(existedUser.getId(), updateRoleRequestDto));
        assertEquals(expected, actual.getMessage());
        verify(userRepository).findById(existedUser.getId());
        verify(roleRepository).findByRoleName(Role.RoleName.ROLE_MANAGER);
        verify(userRepository, never()).save(existedUser);
    }

    @Test
    @DisplayName("Should throw an error due to an invalid user id")
    void updateRole_userNotFound_shouldThrowException() {
        Long userId = 100L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        String expected = "User with id: " + userId + " not found";
        EntityNotFoundException actual =
                assertThrows(EntityNotFoundException.class,
                        () -> userService.updateRole(userId,
                                new UpdateRoleRequestDto(Role.RoleName.ROLE_CUSTOMER.name())));
        assertEquals(expected, actual.getMessage());
        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(roleRepository);
    }

    @Test
    @DisplayName("Should update user data")
    void updateUser_validUserId_shouldUpdateUser() {
        Authentication mockAuth = mock(Authentication.class);
        UpdateUserRequestDto updateUserRequestDto = new UpdateUserRequestDto(
                "Alia",
                "Blast"
        );
        User existedUser = UserUtil.getExistedUser();
        User updatedUser = UserUtil.getUpdatedUser();
        UserDto expected = new UserDto(
                updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getFirstName(),
                updateUserRequestDto.lastName(),
                Set.of(Role.RoleName.ROLE_CUSTOMER.name())
        );
        when(mockAuth.getName()).thenReturn(existedUser.getEmail());
        when(userRepository.findByEmail(existedUser.getEmail()))
                .thenReturn(Optional.of(existedUser));
        doNothing().when(userMapper).updateUserFromDto(updateUserRequestDto, existedUser);
        when(userRepository.save(existedUser)).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(expected);

        UserDto actual = userService.updateUser(mockAuth, updateUserRequestDto);
        assertEquals(expected, actual);
        verify(userRepository).findByEmail(updatedUser.getEmail());
    }

    @Test
    @DisplayName("Should throw an error due to an invalid user id")
    void updateUser_invalidUserId_shouldThrowException() {
        Authentication mockAuth = mock(Authentication.class);
        UpdateUserRequestDto updateUserRequestDto = new UpdateUserRequestDto(
                "Alia",
                "Blast"
        );
        User existedUser = UserUtil.getExistedUser();
        when(mockAuth.getName()).thenReturn(existedUser.getEmail());
        String expected = "User with email: " + existedUser.getEmail() + " not found";
        when(userRepository.findByEmail(existedUser.getEmail())).thenReturn(Optional.empty());

        EntityNotFoundException actual = assertThrows(EntityNotFoundException.class,
                () -> userService.updateUser(mockAuth, updateUserRequestDto));
        verify(userRepository).findByEmail(existedUser.getEmail());
        assertEquals(expected, actual.getMessage());
    }
}
