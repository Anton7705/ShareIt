package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private UserStorage userStorage;

    @Override
    public User findUser(Long id) {
        return userStorage.get(id)
                .orElseThrow(() -> new NoSuchElementException("Отсутствует пользователь с id :" + id));
    }

    public void deleteUser(Long id) {
        userStorage.delete(id);
    }

    @Override
    public User createUser(User user) {
        checkEmail(user);
        userStorage.save(user);
        return user;
    }

    @Override
    public User updateUser(User updatedUser, Long id) {
        User userToUpdate = userStorage.get(id)
                .orElseThrow(() -> new NoSuchElementException("Отсутствует пользователь с id :" + id));

        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equals(userToUpdate.getEmail())) {
            checkEmail(updatedUser);
            userToUpdate.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getName() != null) {
            userToUpdate.setName(updatedUser.getName());
        }

        userStorage.save(userToUpdate);
        return userToUpdate;
    }

    private void checkEmail(User user) {
        if (userStorage.containsEmail(user.getEmail())) {
            throw new IllegalArgumentException("Этот email уже используется");
        }
    }
}
