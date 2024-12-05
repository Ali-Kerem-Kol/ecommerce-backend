package com.alikeremkol.ecommerce_backend.service;

import com.alikeremkol.ecommerce_backend.dto.RoleCreateRequest;
import com.alikeremkol.ecommerce_backend.dto.RoleUpdateRequest;
import com.alikeremkol.ecommerce_backend.exception.AlreadyExistsException;
import com.alikeremkol.ecommerce_backend.exception.ResourceNotFoundException;
import com.alikeremkol.ecommerce_backend.model.Role;
import com.alikeremkol.ecommerce_backend.model.User;
import com.alikeremkol.ecommerce_backend.repository.RoleRepository;
import com.alikeremkol.ecommerce_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleService(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    // CRUD - C
    public Role createRole(RoleCreateRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new AlreadyExistsException("Role: " + request.getName() + " is already exists.");
        }
        Role newRole = new Role(request.getName());
        return roleRepository.save(newRole);
    }

    // CRUD - R
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) {
        return roleRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));
    }

    public Role getRoleByName(String name) {
        return roleRepository
                .findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));
    }

    // CRUD - U
    public Role updateRole(RoleUpdateRequest request, Long roleId) {
        return roleRepository
                .findById(roleId)
                .map(existingRole -> updateExistingRole(existingRole,request))
                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));
    }
    private Role updateExistingRole(Role existingRole, RoleUpdateRequest request) {
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new RuntimeException("Role name cannot be null or empty.");
        }
        existingRole.setAuthority(request.getName());
        return roleRepository.save(existingRole);
    }

    // CRUD - D
    public void deleteRoleById(Long id) {
        Role role = roleRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));

        if (role.getAuthority().equals("ROLE_USER") || role.getAuthority().equals("ROLE_ADMIN")) {
            throw new IllegalArgumentException("Cannot delete default roles: ROLE_USER or ROLE_ADMIN");
        }

        List<User> usersWithRole = userRepository.findByRolesContains(role);
        for (User user : usersWithRole) {
            user.getRoles().remove(role);
            userRepository.save(user);
        }

        roleRepository.delete(role);
    }


    // Other functions



}
