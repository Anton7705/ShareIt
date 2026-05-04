package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

public interface UserService {
    User findUser(Long id);

    void deleteUser(Long id);

    User createUser(User user);

    User updateUser(User updatedUser, Long id);

}
