package ru.practicum.mainservice.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.mainservice.comment.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByAuthorId(Long userId);

    List<Comment> findAllByAuthorIdAndEventId(Long userId, Long eventId);

    List<Comment> findAllByEventId(Long eventId, Pageable pageable);

    @Query("SELECT com.event.id, COUNT(com) " +
            "FROM Comment com " +
            "WHERE com.event.id IN :eventsId " +
            "GROUP BY com.event.id")
    List<Object[]> findCommentsCountByEventIds(@Param("eventsId") List<Long> eventsId);

    Long countByEventId(Long eventId);
}