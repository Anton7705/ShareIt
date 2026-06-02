package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private long currentId = 1;
    private final Map<Long, User> userMap = new HashMap<>();

    @Override
    public Optional<User> get(Long id) {
        return Optional.ofNullable(userMap.get(id));
    }

    @Override
    public void save(User user) {
        if (user.getId() == null) {
            user.setId(currentId++);
        }
        userMap.put(user.getId(), user);
    }

    @Override
    public void delete(Long id) {
        userMap.remove(id);
    }

    public boolean containsEmail(String email) {
        return userMap.values().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

}
