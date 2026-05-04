package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.Optional;

public interface UserStorage {
    Optional<User> get(Long id);

    void save(User user);

    void delete(Long id);

    boolean containsEmail(String email);
}
