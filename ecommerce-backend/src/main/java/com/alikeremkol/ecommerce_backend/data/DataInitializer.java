package com.alikeremkol.ecommerce_backend.data;

import com.alikeremkol.ecommerce_backend.model.Role;
import com.alikeremkol.ecommerce_backend.model.User;
import com.alikeremkol.ecommerce_backend.repository.RoleRepository;
import com.alikeremkol.ecommerce_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        createRolesIfNotExist();
        createDefaultAdminIfNotExists();
    }

    private void createRolesIfNotExist() {
        createRoleIfNotExist("ROLE_USER");
        createRoleIfNotExist("ROLE_ADMIN");
    }

    private void createRoleIfNotExist(String roleName) {
        Optional<Role> optionalRole = roleRepository.findByName(roleName);
        if (optionalRole.isEmpty()) {
            Role role = new Role();
            role.setAuthority(roleName);
            roleRepository.save(role);
            System.out.println(roleName + " created successfully.");
        } else {
            System.out.println(roleName + " already exists.");
        }
    }

    private void createDefaultAdminIfNotExists() {
        Optional<Role> optionalAdminRole = roleRepository.findByName("ROLE_ADMIN");
        String defaultEmail = "admin@email.com";

        if (optionalAdminRole.isPresent()) {
            Role adminRole = optionalAdminRole.get();
            if (userRepository.existsByEmail(defaultEmail)) {
                System.out.println("Admin user already exists.");
                return;
            }

            User user = new User();
            user.setUsername("admin");
            user.setEmail(defaultEmail);
            user.setPassword(passwordEncoder.encode("password"));
            user.setEnabled(true);
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            user.setRoles(roles);
            userRepository.save(user);
            System.out.println("Default admin user created successfully.");
        } else {
            System.out.println("ROLE_ADMIN does not exist, please create the role first.");
        }
    }

}

