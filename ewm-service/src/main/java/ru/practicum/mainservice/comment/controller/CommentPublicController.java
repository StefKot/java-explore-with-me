package ru.practicum.mainservice.comment.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.MainCommonUtils;
import ru.practicum.mainservice.comment.dto.CommentDto;
import ru.practicum.mainservice.comment.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
@Validated
public class CommentPublicController {
    private final CommentService commentService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getCommentsByPublic(
            @RequestParam Long eventId,
            @RequestParam(defaultValue = MainCommonUtils.PAGE_DEFAULT_FROM) @PositiveOrZero Integer from,
            @RequestParam(defaultValue = MainCommonUtils.PAGE_DEFAULT_SIZE) @Positive Integer size) {
        return commentService.getCommentsByPublic(eventId, PageRequest.of(from / size, size));
    }

    @GetMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto getCommentByPublic(@PathVariable Long commentId) {
        return commentService.getCommentByPublic(commentId);
    }
}