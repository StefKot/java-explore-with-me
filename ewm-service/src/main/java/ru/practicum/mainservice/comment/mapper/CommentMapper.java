package ru.practicum.mainservice.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.mainservice.comment.dto.CommentDto;
import ru.practicum.mainservice.comment.model.Comment;
import ru.practicum.mainservice.user.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {

    @Mapping(target = "eventId", expression = "java(comment.getEvent().getId())")
    CommentDto toCommentDto(Comment comment);
}