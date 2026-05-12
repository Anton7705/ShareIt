package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

public interface UserService {
    UserDto findUser(Long id);

    void deleteUser(Long id);

    UserDto createUser(UserDto user);

    UserDto updateUser(UserDto updatedUser, Long id);

}
