package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public UserDto findUser(Long id) {
        log.info("Запрос пользователя с id : {}", id);
        return UserMapper.toDto(getUser(id));
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        log.info("Удаление пользователя с id : {}", id);
        getUser(id);
        userRepository.deleteById(id);
    }

    @Transactional
    @Override
    public UserDto createUser(UserDto userDto) {
        User user = UserMapper.toModel(userDto);
        user.setId(null);
        checkEmail(user.getEmail());
        userRepository.save(user);
        log.info("Создан пользователь с id : {}", user.getId());
        return UserMapper.toDto(user);
    }

    @Transactional
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

        userRepository.save(userToUpdate);
        return UserMapper.toDto(userToUpdate);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Отсутствует пользователь с id :" + id));
    }

    private void checkEmail(String email) {
        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    throw new IllegalArgumentException("Этот email уже используется");
                });
    }

}
