package ru.practicum.mainservice.event.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.practicum.mainservice.MainCommonUtils;
import ru.practicum.mainservice.category.model.Category;
import ru.practicum.mainservice.event.enums.EventState;
import ru.practicum.mainservice.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events", schema = "public")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = MainCommonUtils.MAX_LENGTH_TITLE)
    String title;

    @Column(nullable = false, length = MainCommonUtils.MAX_LENGTH_ANNOTATION)
    String annotation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    Category category;

    @Column(nullable = false, length = MainCommonUtils.MAX_LENGTH_DESCRIPTION)
    String description;

    @Column(nullable = false)
    Boolean paid;

    @Column(nullable = false)
    Integer participantLimit;

    @Column(nullable = false)
    LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    Location location;

    @Column(nullable = false)
    LocalDateTime createdOn;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    EventState state;

    LocalDateTime publishedOn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    User initiator;

    @Column(nullable = false)
    Boolean requestModeration;

    @Builder.Default
    @Column(nullable = false)
    private Long views = 0L;
}