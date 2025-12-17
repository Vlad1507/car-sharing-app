package com.app.carsharing.repository.role;

import com.app.carsharing.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(Role.RoleName roleName);
}
