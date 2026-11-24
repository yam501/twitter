package com.yashin.userservice.controller;

import com.yashin.userservice.dto.AssignRoleRequest;
import com.yashin.userservice.dto.CreateUserRequest;
import com.yashin.userservice.dto.UpdateUserRequest;
import com.yashin.userservice.dto.UserResponse;
import com.yashin.userservice.service.UserService;
import com.yashin.userservice.util.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 4.1. Регистрация — без токена
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody CreateUserRequest request) {
        return userService.register(request);
    }

    // 4.3. Получение пользователя — публичный
    @GetMapping("/users/{userId}")
    public UserResponse getUser(@PathVariable Long userId) {
        return userService.getById(userId);
    }

    // GET /users?ids=1,2,3 — батч-выборка для соседних сервисов (лаба 5)
    @GetMapping("/users")
    public List<UserResponse> getUsers(@RequestParam List<Long> ids) {
        return userService.getByIds(ids);
    }

    // 4.4. Изменение пользователя — только владелец профиля или администратор
    @PatchMapping("/users/{userId}")
    public UserResponse updateUser(@PathVariable Long userId,
                                   @Valid @RequestBody UpdateUserRequest request,
                                   HttpServletRequest httpRequest) {
        return userService.update(userId, request, AuthContext.userId(httpRequest), AuthContext.isAdmin(httpRequest));
    }

    // выдача роли — только администратор
    @PostMapping("/users/{userId}/roles")
    public UserResponse assignRole(@PathVariable Long userId,
                                   @Valid @RequestBody AssignRoleRequest request,
                                   HttpServletRequest httpRequest) {
        return userService.assignRole(userId, request.roleName(), AuthContext.isAdmin(httpRequest));
    }

    @PutMapping("/users/{userId}/roles/{roleName}")
    public UserResponse putRole(@PathVariable Long userId, @PathVariable String roleName,
                                HttpServletRequest httpRequest) {
        return userService.putRole(userId, roleName, AuthContext.isAdmin(httpRequest));
    }

    // снятие роли — только администратор
    @DeleteMapping("/users/{userId}/roles/{roleName}")
    public UserResponse revokeRole(@PathVariable Long userId,
                                   @PathVariable String roleName,
                                   HttpServletRequest httpRequest) {
        return userService.revokeRole(userId, roleName, AuthContext.isAdmin(httpRequest));
    }
}
