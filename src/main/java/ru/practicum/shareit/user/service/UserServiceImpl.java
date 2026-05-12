package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private UserStorage userStorage;

    @Override
    public UserDto findUser(Long id) {
        log.info("Запрос пользователя с id : {}", id);
        return UserMapper.toDto(getUser(id));
    }

    public void deleteUser(Long id) {
        log.info("Удаление пользователя с id : {}", id);
        getUser(id);
        userStorage.delete(id);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toModel(userDto);
        user.setId(null);
        checkEmail(user.getEmail());
        userStorage.save(user);
        log.info("Создан пользователь с id : {}", user.getId());
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto updateUser(UserDto updatedUserDto, Long id) {
        User updatedUser = UserMapper.toModel(updatedUserDto);
        log.info("Обновление пользователя с id : {}", id);
        User userToUpdate = getUser(id);

        String email = updatedUser.getEmail();
        if (email != null && !email.isBlank() && !email.equals(userToUpdate.getEmail())) {
            checkEmail(email);
            userToUpdate.setEmail(updatedUser.getEmail());
        }

        String name = updatedUser.getName();
        if (name != null && !name.isBlank()) {
            userToUpdate.setName(updatedUser.getName());
        }

        userStorage.save(userToUpdate);
        return UserMapper.toDto(userToUpdate);
    }

    private void checkEmail(String email) {
        if (userStorage.containsEmail(email)) {
            throw new IllegalArgumentException("Этот email уже используется");
        }
    }

    private User getUser(Long id) {
        return userStorage.get(id)
                .orElseThrow(() -> new NoSuchElementException("Отсутствует пользователь с id :" + id));
    }
}
