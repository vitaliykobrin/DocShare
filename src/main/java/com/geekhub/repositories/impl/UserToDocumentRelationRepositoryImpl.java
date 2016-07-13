package com.geekhub.repositories.impl;

import com.geekhub.entities.UserDocument;
import com.geekhub.entities.UserToDocumentRelation;
import com.geekhub.entities.enums.FileRelationType;
import com.geekhub.repositories.UserToDocumentRelationRepository;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;

@Repository
@SuppressWarnings("unchecked")
public class UserToDocumentRelationRepositoryImpl implements UserToDocumentRelationRepository {

    @Inject
    private SessionFactory sessionFactory;

    private Class<UserToDocumentRelation> clazz = UserToDocumentRelation.class;

    @Override
    public List<UserToDocumentRelation> getAll(String orderParameter) {
        return sessionFactory.getCurrentSession()
                .createCriteria(clazz)
                .addOrder(Order.asc(orderParameter))
                .list();
    }

    @Override
    public UserToDocumentRelation getById(Long id) {
        return (UserToDocumentRelation) sessionFactory.getCurrentSession().get(clazz, id);
    }

    @Override
    public UserToDocumentRelation get(String propertyName, Object value) {
        return (UserToDocumentRelation) sessionFactory.getCurrentSession()
                .createCriteria(clazz)
                .add(Restrictions.eq(propertyName, value))
                .uniqueResult();
    }

    @Override
    public Long save(UserToDocumentRelation entity) {
        return (Long) sessionFactory.getCurrentSession().save(entity);
    }

    @Override
    public void update(UserToDocumentRelation entity) {
        sessionFactory.getCurrentSession().update(entity);
    }

    @Override
    public void saveOrUpdate(UserToDocumentRelation entity) {
        sessionFactory.getCurrentSession().saveOrUpdate(entity);
    }

    @Override
    public void delete(UserToDocumentRelation entity) {
        sessionFactory.getCurrentSession().delete(entity);
    }

    @Override
    public void deleteById(Long entityId) {
        UserToDocumentRelation userDocument = getById(entityId);
        sessionFactory.getCurrentSession().delete(userDocument);
    }

    @Override
    public List<UserToDocumentRelation> getList(String propertyName, Object value) {
        return sessionFactory.getCurrentSession()
                .createCriteria(clazz)
                .add(Restrictions.eq(propertyName, value))
                .list();
    }

    @Override
    public void deleteByDocumentBesidesOwner(UserDocument document) {
        sessionFactory.getCurrentSession()
                .createQuery("DELETE UserToDocumentRelation r WHERE r.document = :document AND r.fileRelationType != :relation")
                .setParameter("document", document)
                .setParameter("relation", FileRelationType.OWNER)
                .executeUpdate();
    }
}
