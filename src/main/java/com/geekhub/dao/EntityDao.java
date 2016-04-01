package com.geekhub.dao;

import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

public interface EntityDao<T, PK extends Serializable> {

    List<T> getAll(String orderParameter);

    T getById(PK id);

    T get(String propertyName, Object value);

    PK save(T entity);

    void update(T entity);

    void saveOrUpdate(T entity);

    void delete(T entity);

    void delete(PK entityId);
}