package com.geekhub.services.impl;

import com.geekhub.repositories.EventRepository;
import com.geekhub.entities.Event;
import com.geekhub.entities.User;
import com.geekhub.entities.enums.EventStatus;
import java.util.Collection;
import java.util.List;

import com.geekhub.services.EventService;
import com.geekhub.services.UserService;
import javax.inject.Inject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EventServiceImpl implements EventService {

    @Inject
    private EventRepository repository;

    @Inject
    private UserService userService;

    @Override
    public List<Event> getAll(String orderParameter) {
        return repository.getAll(orderParameter);
    }

    @Override
    public List<Event> getAllByRecipient(User recipient) {
        return repository.getList("recipient", recipient);
    }

    @Override
    public Event getById(Long id) {
        return repository.getById(id);
    }

    @Override
    public Event get(String propertyName, Object value) {
        return repository.get(propertyName, value);
    }

    @Override
    public Long save(Event entity) {
        return repository.save(entity);
    }

    @Override
    public void update(Event entity) {
        repository.update(entity);
    }

    @Override
    public void delete(Event entity) {
        repository.delete(entity);
    }

    @Override
    public void deleteById(Long entityId) {
        repository.deleteById(entityId);
    }

    @Override
    public Long getUnreadCount(Long recipientId) {
        return repository.getCount(recipientId, "eventStatus", EventStatus.UNREAD);
    }

    @Override
    public void setReadStatus(Collection<Event> events) {
        if (events != null) {
            events.forEach(e -> {
                e.setEventStatus(EventStatus.READ);
                update(e);
            });
        }
    }

    @Override
    public void save(List<Event> events) {
        events.forEach(this::save);
    }

    @Override
    public Event getByHashName(String eventHashName) {
        return repository.get("hashName", eventHashName);
    }

    @Override
    public void clearEvents(Long userId) {
        User user = userService.getById(userId);
        user.getEvents().clear();
        userService.update(user);
    }
}
