package com.alikeremkol.ecommerce_backend.controller;

import com.alikeremkol.ecommerce_backend.dto.RoleCreateRequest;
import com.alikeremkol.ecommerce_backend.dto.RoleUpdateRequest;
import com.alikeremkol.ecommerce_backend.exception.AlreadyExistsException;
import com.alikeremkol.ecommerce_backend.exception.ResourceNotFoundException;
import com.alikeremkol.ecommerce_backend.model.Role;
import com.alikeremkol.ecommerce_backend.response.ApiResponse;
import com.alikeremkol.ecommerce_backend.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }


    // CRUD - C
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createRole(RoleCreateRequest request) {
        try {
            Role role =roleService.createRole(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("Role created successfully.",role));
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(e.getMessage(),null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(),null));
        }
    }

    // CRUD - R
    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllRoles() {
        try {
            List<Role> roles = roleService.getAllRoles();
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Roles found.",roles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(),null));
        }
    }

    @GetMapping("/role/id")
    public ResponseEntity<ApiResponse> getRoleById(@RequestParam Long roleId) {
        try {
            Role role = roleService.getRoleById(roleId);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Role found.",role));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(),null));
        }
    }

    @GetMapping("/role/name")
    public ResponseEntity<ApiResponse> getRoleByName(@RequestParam String name) {
        try {
            Role role = roleService.getRoleByName(name);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Role found.",role));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(),null));
        }
    }

    // CRUD - U
    @PutMapping("/role/update/{roleId}")
    public ResponseEntity<ApiResponse> updateRole(@RequestBody RoleUpdateRequest request, @PathVariable Long roleId) {
        try {
            Role role = roleService.updateRole(request, roleId);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse("Role updated successfully.",role));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(),null));
        }
    }

    // CRUD - D
    @DeleteMapping("/role/delete/{roleId}")
    public ResponseEntity<ApiResponse> deleteRoleById(@PathVariable Long roleId) {
        try {
            roleService.deleteRoleById(roleId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(),null));
        }
    }

    // Other functions



}