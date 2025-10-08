package ru.practicum.mainservice.compilation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.compilation.dto.CompilationDto;
import ru.practicum.mainservice.compilation.dto.NewCompilationDto;
import ru.practicum.mainservice.compilation.dto.UpdateCompilationRequest;
import ru.practicum.mainservice.compilation.mapper.CompilationMapper;
import ru.practicum.mainservice.compilation.model.Compilation;
import ru.practicum.mainservice.compilation.repository.CompilationRepository;
import ru.practicum.mainservice.event.dto.EventShortDto;
import ru.practicum.mainservice.event.model.Event;
import ru.practicum.mainservice.event.service.EventService;
import ru.practicum.mainservice.exception.NotFoundException;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final EventService eventService;
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        log.info("Создание новой коллекции событий с параметрами {}", newCompilationDto);

        Set<Event> events = new HashSet<>();

        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = eventService.getEventsByIds(newCompilationDto.getEvents());
            checkSize(events, newCompilationDto.getEvents());
        }

        Compilation compilation = compilationRepository.save(compilationMapper.newDtoToCompilation(newCompilationDto, events));
        Set<EventShortDto> eventsShortDto = new HashSet<>(eventService.toEventsShortDto(compilation.getEvents()));

        return compilationMapper.toCompilationDto(compilation, eventsShortDto);
    }

    @Override
    @Transactional
    public CompilationDto patch(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        log.info("Обновление выборки событий с ID {} и новыми параметрами {}", compId, updateCompilationRequest);

        Compilation compilation = getCompilationById(compId);

        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }

        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }

        if (updateCompilationRequest.getEvents() != null) {
            Set<Event> events = eventService.getEventsByIds(updateCompilationRequest.getEvents());

            checkSize(events, updateCompilationRequest.getEvents());

            compilation.setEvents(events);
        }

        compilationRepository.save(compilation);
        Set<EventShortDto> eventsShortDto = new HashSet<>(eventService.toEventsShortDto(compilation.getEvents()));

        return compilationMapper.toCompilationDto(compilation, eventsShortDto);
    }

    @Override
    @Transactional
    public void deleteById(Long compId) {
        log.info("Удаление коллекции событий из ID {}", compId);

        getCompilationById(compId);

        compilationRepository.deleteById(compId);
    }

    @Override
    public List<CompilationDto> getAll(Boolean pinned, Pageable pageable) {
        log.info("Вывод всех коллекций событий с параметрами pinned = {}, pageable = {}", pinned, pageable);

        List<Compilation> compilations;

        if (pinned == null) {
            compilations = compilationRepository.findAll(pageable).toList();
        } else {
            compilations = compilationRepository.findAllByPinned(pinned, pageable);
        }

        Set<Event> uniqueEvents = new HashSet<>();
        compilations.forEach(compilation -> uniqueEvents.addAll(compilation.getEvents()));

        Map<Long, EventShortDto> eventsShortDto = new HashMap<>();
        eventService.toEventsShortDto(new HashSet<>(uniqueEvents))
                .forEach(event -> eventsShortDto.put(event.getId(), event));

        List<CompilationDto> result = new ArrayList<>();
        compilations.forEach(compilation -> {
            Set<EventShortDto> compEventsShortDto = new HashSet<>();
            compilation.getEvents()
                    .forEach(event -> compEventsShortDto.add(eventsShortDto.get(event.getId())));
            result.add(compilationMapper.toCompilationDto(compilation, compEventsShortDto));
        });

        return result;
    }

    @Override
    public CompilationDto getById(Long compId) {
        log.info("Вывод выборки событий с ID {}", compId);

        Compilation compilation = getCompilationById(compId);

        Set<EventShortDto> eventsShortDto = new HashSet<>(eventService.toEventsShortDto(compilation.getEvents()));

        return compilationMapper.toCompilationDto(compilation, eventsShortDto);
    }

    private Compilation getCompilationById(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Компиляции с таким идентификатором не существует"));
    }

    private void checkSize(Set<Event> events, List<Long> eventsIdToUpdate) {
        if (events.size() != eventsIdToUpdate.size()) {
            throw new NotFoundException("Некоторые события не найдены");
        }
    }
}