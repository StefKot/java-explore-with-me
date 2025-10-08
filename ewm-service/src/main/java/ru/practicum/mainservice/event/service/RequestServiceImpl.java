package ru.practicum.mainservice.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.mainservice.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.mainservice.event.dto.ParticipationRequestDto;
import ru.practicum.mainservice.event.enums.EventState;
import ru.practicum.mainservice.event.enums.RequestStatus;
import ru.practicum.mainservice.event.enums.RequestStatusAction;
import ru.practicum.mainservice.event.mapper.RequestMapper;
import ru.practicum.mainservice.event.model.Event;
import ru.practicum.mainservice.event.model.Request;
import ru.practicum.mainservice.event.repository.RequestRepository;
import ru.practicum.mainservice.exception.ForbiddenException;
import ru.practicum.mainservice.exception.NotFoundException;
import ru.practicum.mainservice.user.model.User;
import ru.practicum.mainservice.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final UserService userService;
    private final EventService eventService;
    private final StatsService statsService;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getEventRequestsByRequester(Long userId) {
        log.info("Отображение списка запросов на участие в чужих мероприятиях от пользователя с ID {}", userId);

        userService.getUserById(userId);

        return toParticipationRequestsDto(requestRepository.findAllByRequesterId(userId));
    }

    @Override
    @Transactional
    public ParticipationRequestDto createEventRequest(Long userId, Long eventId) {
        log.info("Создание запроса на участие в мероприятии с ID {} пользователем с ID {}", eventId, userId);

        User user = userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);

        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ForbiddenException("Вы не можете создать запрос на собственное мероприятие");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ForbiddenException("Вы не можете создать запрос на неопубликованное событие");
        }

        Optional<Request> oldRequest = requestRepository.findByEventIdAndRequesterId(eventId, userId);

        if (oldRequest.isPresent()) {
            throw new ForbiddenException("Запрещено создавать повторный запрос");
        }

        checkIsNewLimitGreaterOld(
                statsService.getConfirmedRequests(Set.of(event)).getOrDefault(eventId, 0L) + 1,
                event.getParticipantLimit()
        );

        Request newRequest = Request.builder()
                .event(event)
                .requester(user)
                .created(LocalDateTime.now())
                .build();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            newRequest.setStatus(RequestStatus.CONFIRMED);
        } else {
            newRequest.setStatus(RequestStatus.PENDING);
        }

        return requestMapper.toParticipationRequestDto(requestRepository.save(newRequest));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelEventRequest(Long userId, Long requestId) {
        log.info("Отмена запроса с ID {} на участие в мероприятии пользователем с ID {}", requestId, userId);

        userService.getUserById(userId);

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка на участие с ID " + requestId + " не найдена."));

        checkUserIsOwner(request.getRequester().getId(), userId);

        request.setStatus(RequestStatus.CANCELED);

        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getEventRequestsByEventOwner(Long userId, Long eventId) {
        log.info("Отображение списка заявок на участие в мероприятии с ID {} " +
                "владельца с ID {}", eventId, userId);

        userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);

        checkUserIsOwner(event.getInitiator().getId(), userId);

        return toParticipationRequestsDto(requestRepository.findAllByEventId(eventId));
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult patchEventRequestsByEventOwner(
            Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("Обновление запросов на участие в мероприятии с ID {} владельца с ID {} и параметрами {}",
                eventId, userId, eventRequestStatusUpdateRequest);

        userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);

        checkUserIsOwner(event.getInitiator().getId(), userId);

        if (!event.getRequestModeration() ||
                event.getParticipantLimit() == 0 ||
                eventRequestStatusUpdateRequest.getRequestIds().isEmpty()) {
            return new EventRequestStatusUpdateResult(List.of(), List.of());
        }

        List<Request> confirmedList = new ArrayList<>();
        List<Request> rejectedList = new ArrayList<>();

        List<Request> requests = requestRepository.findAllByIdIn(eventRequestStatusUpdateRequest.getRequestIds());

        if (requests.size() != eventRequestStatusUpdateRequest.getRequestIds().size()) {
            throw new NotFoundException("Некоторые заявки на участие не найдены");
        }

        if (!requests.stream()
                .map(Request::getStatus)
                .allMatch(RequestStatus.PENDING::equals)) {
            throw new ForbiddenException("Изменять можно только находящиеся на рассмотрении заявки");
        }

        if (eventRequestStatusUpdateRequest.getStatus().equals(RequestStatusAction.REJECTED)) {
            rejectedList.addAll(changeStatusAndSave(requests, RequestStatus.REJECTED));
        } else {
            Long newConfirmedRequests = statsService.getConfirmedRequests(Set.of(event)).getOrDefault(eventId, 0L) +
                    eventRequestStatusUpdateRequest.getRequestIds().size();

            checkIsNewLimitGreaterOld(newConfirmedRequests, event.getParticipantLimit());

            confirmedList.addAll(changeStatusAndSave(requests, RequestStatus.CONFIRMED));

            if (newConfirmedRequests >= event.getParticipantLimit()) {
                rejectedList.addAll(changeStatusAndSave(
                        requestRepository.findAllByEventIdAndStatus(eventId, RequestStatus.PENDING),
                        RequestStatus.REJECTED)
                );
            }
        }

        return new EventRequestStatusUpdateResult(toParticipationRequestsDto(confirmedList),
                toParticipationRequestsDto(rejectedList));
    }

    private List<ParticipationRequestDto> toParticipationRequestsDto(List<Request> requests) {
        return requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    private List<Request> changeStatusAndSave(List<Request> requests, RequestStatus status) {
        requests.forEach(request -> request.setStatus(status));
        return requestRepository.saveAll(requests);
    }

    private void checkIsNewLimitGreaterOld(Long newLimit, Integer eventParticipantLimit) {
        if (eventParticipantLimit != 0 && (newLimit > eventParticipantLimit)) {
            throw new ForbiddenException(String.format("Достигнут лимит подтвержденных заявок на участие: %d",
                    eventParticipantLimit));
        }
    }

    private void checkUserIsOwner(Long id, Long userId) {
        if (!Objects.equals(id, userId)) {
            throw new ForbiddenException("Пользователь не является владельцем");
        }
    }
}