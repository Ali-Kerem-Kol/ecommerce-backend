package com.alikeremkol.ecommerce_backend.service;

import com.alikeremkol.ecommerce_backend.dto.UserDto;
import com.alikeremkol.ecommerce_backend.exception.ResourceNotFoundException;
import com.alikeremkol.ecommerce_backend.model.Role;
import com.alikeremkol.ecommerce_backend.model.User;
import com.alikeremkol.ecommerce_backend.repository.RoleRepository;
import com.alikeremkol.ecommerce_backend.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
    }


    // CRUD - C
    public User addUserRole(Long userId, Long roleIdToAdd) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Role roleToAdd = roleRepository.findById(roleIdToAdd)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));

        if (!user.getRoles().contains(roleToAdd)) {
            user.getRoles().add(roleToAdd);
        }

        return userRepository.save(user);
    }

    // CRUD - R

    public User getUserById(Long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    // CRUD - U


    // CRUD - D
    public User removeUserRole(Long userId, Long roleIdToRemove) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Role roleToRemove = roleRepository.findById(roleIdToRemove)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found."));

        user.getRoles().remove(roleToRemove);

        return userRepository.save(user);
    }

    public void deleteUser(Long userId) throws AccessDeniedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRoles().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Cannot delete an admin account.");
        }

        userRepository.delete(user);
    }

    // Other functions

    public User disableUser(Long userId) throws AccessDeniedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (user.getRoles().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Cannot deactivate an admin account.");
        }

        user.setEnabled(false);
        return userRepository.save(user);
    }

    public User enableUser(Long userId) throws AccessDeniedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (user.getRoles().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Cannot activate an admin account.");
        }

        user.setEnabled(true);
        return userRepository.save(user);
    }

    public UserDto convertUserToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }


}
