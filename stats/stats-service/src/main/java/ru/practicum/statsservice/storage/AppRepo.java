package ru.practicum.statsservice.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.statsservice.model.App;

import java.util.Optional;

public interface AppRepo extends JpaRepository<App, Long> {
    Optional<App> findByName(String name);
}