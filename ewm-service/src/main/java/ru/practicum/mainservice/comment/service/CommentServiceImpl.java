package ru.practicum.mainservice.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.comment.dto.CommentDto;
import ru.practicum.mainservice.comment.dto.NewCommentDto;
import ru.practicum.mainservice.comment.mapper.CommentMapper;
import ru.practicum.mainservice.comment.model.Comment;
import ru.practicum.mainservice.comment.repository.CommentRepository;
import ru.practicum.mainservice.event.enums.EventState;
import ru.practicum.mainservice.event.model.Event;
import ru.practicum.mainservice.event.service.EventService;
import ru.practicum.mainservice.exception.ForbiddenException;
import ru.practicum.mainservice.exception.NotFoundException;
import ru.practicum.mainservice.exception.TimeLessTwoHourException;
import ru.practicum.mainservice.user.model.User;
import ru.practicum.mainservice.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final UserService userService;
    private final EventService eventService;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    public List<CommentDto> getCommentsByAdmin(Pageable pageable) {
        log.info("Вывод всех комментариев с разбивкой на страницы {}", pageable);

        return toCommentsDto(commentRepository.findAll(pageable).toList());
    }

    @Override
    @Transactional
    public void deleteByAdmin(Long commentId) {
        log.info("Удаление комментария с ID {}", commentId);
        commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Комментариев с таким ID нет."));

        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getCommentsByPrivate(Long userId, Long eventId, Pageable pageable) {
        log.info("Вывод всех комментариев пользователей с ID {} к событию с ID {} и пагинацией {}",
                userId, eventId, pageable);

        userService.getUserById(userId);

        List<Comment> comments;
        if (eventId != null) {
            eventService.getPublicEventById(eventId);

            comments = commentRepository.findAllByAuthorIdAndEventId(userId, eventId);
        } else {
            comments = commentRepository.findAllByAuthorId(userId);
        }
        return toCommentsDto(comments);
    }

    @Override
    @Transactional
    public CommentDto createByPrivate(Long userId, Long eventId, NewCommentDto newCommentDto) {
        log.info("Создание комментария к событию ID {} пользователем с ID {} и параметрами {}",
                eventId, userId, newCommentDto);

        User user = userService.getUserById(userId);
        Event event = eventService.getPublicEventById(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ForbiddenException("Вы можете создавать комментарии только к опубликованным событиям.");
        }

        Comment comment = Comment.builder()
                .text(newCommentDto.getText())
                .author(user)
                .event(event)
                .createdOn(LocalDateTime.now())
                .build();

        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto patchByPrivate(Long userId, Long commentId, NewCommentDto newCommentDto) {
        log.info("Обновление комментария с ID {} пользователем с ID {} и параметрами {}", commentId, userId, newCommentDto);

        userService.getUserById(userId);
        if (LocalDateTime.now().isBefore(getCommentById(commentId).getCreatedOn().plusHours(2))) {
            Comment commentFromRepository = getCommentById(commentId);
            checkUserIsOwner(userId, commentFromRepository.getAuthor().getId());
            commentFromRepository.setText(newCommentDto.getText());
            commentFromRepository.setEditedOn(LocalDateTime.now());

            return commentMapper.toCommentDto(commentRepository.save(commentFromRepository));
        } else {
            throw new TimeLessTwoHourException("Прошло более двух часов с момента создания комментария.");
        }
    }

    @Override
    @Transactional
    public void deleteByPrivate(Long userId, Long commentId) {
        log.info("Удаление комментария с ID {} пользователем с ID {}", commentId, userId);

        userService.getUserById(userId);

        checkUserIsOwner(userId, getCommentById(commentId).getAuthor().getId());

        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getCommentsByPublic(Long eventId, Pageable pageable) {
        log.info("Вывод всех комментариев к событию с ID {} и пагинацией {}", eventId, pageable);

        eventService.getPublicEventById(eventId);

        return toCommentsDto(commentRepository.findAllByEventId(eventId, pageable));
    }

    @Override
    public CommentDto getCommentByPublic(Long commentId) {
        log.info("Вывод комментария с ID {}", commentId);

        return commentMapper.toCommentDto(getCommentById(commentId));
    }

    private List<CommentDto> toCommentsDto(List<Comment> comments) {
        return comments.stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментариев с этим ID нет."));
    }

    private void checkUserIsOwner(Long id, Long userId) {
        if (!Objects.equals(id, userId)) {
            throw new ForbiddenException("Пользователь не является владельцем.");
        }
    }
}