package com.app.carsharing.service.user;

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
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Role.RoleName CUSTOMER = Role.RoleName.ROLE_CUSTOMER;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public UserDto register(UserRegistrationRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RegistrationException("User with email: " + request.email()
                    + " is already exist");
        }
        User user = userMapper.toModel(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(getRoleUserFromDb()));
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDto updateRole(Long userId, UpdateRoleRequestDto updateRoleRequestDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User with id: " + userId + " not found")
        );
        Role.RoleName requestedRoleName;
        try {
            requestedRoleName = Role.RoleName.valueOf(updateRoleRequestDto.role());
        } catch (IllegalArgumentException e) {
            throw new RoleUpdateException("Invalid role name requested: "
                    + updateRoleRequestDto.role());
        }
        Role role = roleRepository
                .findByRoleName(requestedRoleName)
                .orElseThrow(
                        () -> new EntityNotFoundException("Role with name: "
                                + updateRoleRequestDto.role() + " not found")
                );  
        Set<Role> updatedRoles = new HashSet<>(Set.of(role));
        user.setRoles(updatedRoles);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDto getUser(Authentication authentication) {
        String principal = authentication.getName();
        User user = getUserFromPrincipal(principal);
        return userMapper.toDto(user);
    }

    @Override
    public UserDto updateUser(Authentication authentication,
                              UpdateUserRequestDto updateRequestDto) {
        String principal = authentication.getName();
        User user = getUserFromPrincipal(principal);
        userMapper.updateUserFromDto(updateRequestDto, user);
        return userMapper.toDto(userRepository.save(user));
    }

    private User getUserFromPrincipal(String principal) {
        return userRepository.findByEmail(principal).orElseThrow(
                () -> new EntityNotFoundException("User with email: " + principal + " not found")
        );
    }

    private Role getRoleUserFromDb() {
        return roleRepository.findByRoleName(CUSTOMER)
                .orElseThrow(
                        () -> new EntityNotFoundException("Can't find role by name: " + CUSTOMER)
                );
    }
}
