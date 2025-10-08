package ru.practicum.mainservice.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.mainservice.compilation.dto.CompilationDto;
import ru.practicum.mainservice.compilation.dto.NewCompilationDto;
import ru.practicum.mainservice.compilation.model.Compilation;
import ru.practicum.mainservice.event.dto.EventShortDto;
import ru.practicum.mainservice.event.model.Event;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface CompilationMapper {
    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "events", expression = "java(events)")
    Compilation newDtoToCompilation(NewCompilationDto newCompilationDto, Set<Event> events);

    @Mapping(target = "events", expression = "java(eventsShortDto)")
    CompilationDto toCompilationDto(Compilation compilation, Set<EventShortDto> eventsShortDto);
}