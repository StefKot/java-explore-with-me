package ru.practicum.mainservice.compilation.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.practicum.mainservice.MainCommonUtils;
import ru.practicum.mainservice.event.model.Event;

import java.util.Set;

@Entity
@Table(name = "compilations", schema = "public")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = MainCommonUtils.MAX_LENGTH_TITLE, unique = true)
    String title;

    @Column(nullable = false)
    Boolean pinned;

    @ManyToMany
    @JoinTable(name = "compilations_events",
            joinColumns = @JoinColumn(name = "compilation_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "event_id", referencedColumnName = "id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    Set<Event> events;
}