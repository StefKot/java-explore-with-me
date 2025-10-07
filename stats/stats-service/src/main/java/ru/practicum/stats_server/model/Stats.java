package ru.practicum.stats_server.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "stats", schema = "public")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "app_name", nullable = false)
    String app;

    @Column(nullable = false)
    String uri;

    @Column(name = "user_ip", nullable = false, length = 15)
    String ip;

    @Column(name = "created", nullable = false)
    LocalDateTime timestamp;
}