package com.yashin.userservice.service;

import com.yashin.userservice.dto.CreateUserRequest;
import com.yashin.userservice.dto.LoginRequest;
import com.yashin.userservice.dto.LoginResponse;
import com.yashin.userservice.dto.UpdateUserRequest;
import com.yashin.userservice.dto.UserResponse;
import com.yashin.userservice.entity.Role;
import com.yashin.userservice.entity.User;
import com.yashin.userservice.exception.ConflictException;
import com.yashin.userservice.exception.ForbiddenException;
import com.yashin.userservice.exception.InvalidCredentialsException;
import com.yashin.userservice.exception.NotFoundException;
import com.yashin.userservice.repository.RoleRepository;
import com.yashin.userservice.repository.UserRepository;
import com.yashin.userservice.util.JwtUtil;
import com.yashin.userservice.util.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // 4.1. Регистрация: новому пользователю выдаётся роль subscriber
    @Transactional
    public UserResponse register(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email().strip().toLowerCase(Locale.ROOT))) {
            throw new ConflictException(
                    "Пользователь с таким email уже зарегистрирован",
                    Map.of("email", request.email().strip().toLowerCase(Locale.ROOT)));
        }
        if (request.phoneNumber() != null && userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new ConflictException(
                    "Пользователь с таким телефоном уже зарегистрирован",
                    Map.of("phoneNumber", request.phoneNumber()));
        }

        Role subscriber = getRole("subscriber");
        User user = new User();
        user.setFirstname(request.firstname());
        user.setLastname(request.lastname());
        user.setEmail(request.email().strip().toLowerCase(Locale.ROOT));
        user.setPhoneNumber(request.phoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.getRoles().add(subscriber);

        return UserMapper.toResponse(userRepository.save(user));
    }

    // 4.2. Вход: ответ одинаковый и для несуществующего email, и для неверного пароля
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().strip().toLowerCase(Locale.ROOT)).orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Неверный email или пароль");
        }
        List<String> roles = user.getRoles().stream().map(Role::getName).sorted().toList();
        return new LoginResponse(user.getId(), jwtUtil.generate(user.getId(), roles), jwtUtil.expiresAt());
    }

    // 4.3. Получение пользователя
    @Transactional(readOnly = true)
    public UserResponse getById(Long userId) {
        return UserMapper.toResponse(getUserOrThrow(userId));
    }

    // GET /users?ids=1,2,3 — батч-выборка по id, внутренний вызов соседних сервисов (лаба 5)
    @Transactional(readOnly = true)
    public List<UserResponse> getByIds(List<Long> ids) {
        return userRepository.findAllById(ids).stream()
                .sorted(Comparator.comparing(User::getId))
                .map(UserMapper::toResponse)
                .toList();
    }

    // 4.4. Изменение пользователя: только сам пользователь или администратор
    @Transactional
    public UserResponse update(Long targetUserId, UpdateUserRequest request, Long callerId, boolean callerIsAdmin) {
        if (!callerIsAdmin && !callerId.equals(targetUserId)) {
            throw new ForbiddenException("Можно изменять только свой профиль");
        }
        User user = getUserOrThrow(targetUserId);

        // телефон уникален: менять можно только на свободный
        if (request.phoneNumber() != null && !request.phoneNumber().equals(user.getPhoneNumber())) {
            userRepository.findByPhoneNumber(request.phoneNumber())
                    .filter(other -> !other.getId().equals(user.getId()))
                    .ifPresent(other -> {
                        throw new ConflictException(
                                "Телефон уже занят другим пользователем",
                                Map.of("phoneNumber", request.phoneNumber()));
                    });
        }
        if (request.firstname() != null) {
            user.setFirstname(request.firstname());
        }
        if (request.lastname() != null) {
            user.setLastname(request.lastname());
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber());
        }
        userRepository.flush();
        return UserMapper.toResponse(user);
    }

    // выдача роли — только администратор
    @Transactional
    public UserResponse assignRole(Long targetUserId, String roleName, boolean callerIsAdmin) {
        requireAdmin(callerIsAdmin);
        User user = getUserOrThrow(targetUserId);
        Role role = getRoleOrThrow(roleName);
        if (user.getRoles().contains(role)) {
            throw new ConflictException(
                    "У пользователя уже есть эта роль",
                    Map.of("roleName", roleName));
        }
        user.getRoles().add(role);
        return UserMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse putRole(Long targetUserId, String roleName, boolean callerIsAdmin) {
        requireAdmin(callerIsAdmin);
        User user = getUserOrThrow(targetUserId);
        user.getRoles().add(getRoleOrThrow(roleName));
        return UserMapper.toResponse(userRepository.save(user));
    }

    // снятие роли — только администратор
    @Transactional
    public UserResponse revokeRole(Long targetUserId, String roleName, boolean callerIsAdmin) {
        requireAdmin(callerIsAdmin);
        User user = getUserOrThrow(targetUserId);
        Role role = getRoleOrThrow(roleName);
        if (!user.getRoles().contains(role)) {
            throw new NotFoundException(
                    "У пользователя нет этой роли",
                    Map.of("roleName", roleName));
        }
        if (user.getRoles().size() == 1) {
            throw new ConflictException("Нельзя снять последнюю роль", Map.of("userId", targetUserId));
        }
        user.getRoles().remove(role);
        return UserMapper.toResponse(userRepository.save(user));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь не найден",
                        Map.of("userId", userId)));
    }

    private Role getRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new NotFoundException(
                        "Роль не найдена",
                        Map.of("roleName", roleName)));
    }

    private Role getRoleOrThrow(String roleName) {
        return getRole(roleName);
    }

    private void requireAdmin(boolean callerIsAdmin) {
        if (!callerIsAdmin) {
            throw new ForbiddenException("Требуется роль administrator");
        }
    }
}
