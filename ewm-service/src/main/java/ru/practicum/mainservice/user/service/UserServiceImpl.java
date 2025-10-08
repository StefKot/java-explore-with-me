package ru.practicum.mainservice.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.exception.NotFoundException;
import ru.practicum.mainservice.user.dto.NewUserRequest;
import ru.practicum.mainservice.user.dto.UserDto;
import ru.practicum.mainservice.user.mapper.UserMapper;
import ru.practicum.mainservice.user.model.User;
import ru.practicum.mainservice.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(NewUserRequest newUserRequest) {
        log.info("Добавление пользователя {}", newUserRequest);
        User user = userMapper.toUser(newUserRequest);
        User savedUser = userRepository.save(user);
        return userMapper.toUserDto(savedUser);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Pageable pageable) {
        log.info("Изъятие пользователей с ID {} и пагинацией {}", ids, pageable);

        if (ids == null || ids.isEmpty()) {
            return userRepository.findAll(pageable).stream()
                    .map(userMapper::toUserDto)
                    .collect(Collectors.toList());
        } else {
            return userRepository.findAllByIdIn(ids, pageable).stream()
                    .map(userMapper::toUserDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("Удаление пользователя с ID {}", id);
        findUserById(id);
        userRepository.deleteById(id);
    }

    @Override
    public User getUserById(Long id) {
        log.info("Вывод пользователя с ID {}", id);
        return findUserById(id);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));
    }
}