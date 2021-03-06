package com.geekhub.repositories.impl;

import com.geekhub.entities.User;
import com.geekhub.entities.UserDirectory;
import com.geekhub.entities.UserToDirectoryRelation;
import com.geekhub.entities.enums.FileRelationType;
import com.geekhub.repositories.UserToDirectoryRelationRepository;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;

@Repository
@SuppressWarnings("unchecked")
public class UserToDirectoryRelationRepositoryImpl implements UserToDirectoryRelationRepository {

    @Inject
    private SessionFactory sessionFactory;

    private Class<UserToDirectoryRelation> clazz = UserToDirectoryRelation.class;

    @Override
    public List<UserToDirectoryRelation> getAll(String orderParameter) {
        return sessionFactory.getCurrentSession()
                .createCriteria(clazz)
                .addOrder(Order.asc(orderParameter))
                .list();
    }

    @Override
    public UserToDirectoryRelation getById(Long id) {
        return (UserToDirectoryRelation) sessionFactory.getCurrentSession().get(clazz, id);
    }

    @Override
    public UserToDirectoryRelation get(String propertyName, Object value) {
        return (UserToDirectoryRelation) sessionFactory.getCurrentSession()
                .createCriteria(clazz)
                .add(Restrictions.eq(propertyName, value))
                .uniqueResult();
    }

    @Override
    public Long save(UserToDirectoryRelation entity) {
        return (Long) sessionFactory.getCurrentSession().save(entity);
    }

    @Override
    public void update(UserToDirectoryRelation entity) {
        sessionFactory.getCurrentSession().update(entity);
    }

    @Override
    public void saveOrUpdate(UserToDirectoryRelation entity) {
        sessionFactory.getCurrentSession().saveOrUpdate(entity);
    }

    @Override
    public void delete(UserToDirectoryRelation entity) {
        sessionFactory.getCurrentSession().delete(entity);
    }

    @Override
    public void deleteById(Long entityId) {
        UserToDirectoryRelation userDocument = getById(entityId);
        sessionFactory.getCurrentSession().delete(userDocument);
    }

    @Override
    public List<UserToDirectoryRelation> getList(String propertyName, Object value) {
        return sessionFactory.getCurrentSession()
                .createCriteria(clazz)
                .add(Restrictions.eq(propertyName, value))
                .list();
    }

    @Override
    public void deleteAllBesidesOwnerByDirectory(UserDirectory directory) {
        sessionFactory.getCurrentSession()
                .createQuery("DELETE UserToDirectoryRelation r " +
                             "WHERE r.directory = :directory AND r.fileRelationType != :relation")
                .setParameter("directory", directory)
                .setParameter("relation", FileRelationType.OWN)
                .executeUpdate();
    }

    @Override
    public List<UserDirectory> getAllAccessibleDirectories(User user) {
        return sessionFactory.getCurrentSession()
                .createCriteria(clazz)
                .add(Restrictions.eq("user", user))
                .add(Restrictions.ne("fileRelationType", FileRelationType.OWN))
                .setProjection(Projections.property("directory"))
                .list();
    }

    @Override
    public List<User> getAllUsersByDirectoryIdAndRelation(UserDirectory directory, FileRelationType relationType) {
        return sessionFactory.getCurrentSession()
                .createCriteria(clazz)
                .add(Restrictions.eq("directory", directory))
                .add(Restrictions.eq("fileRelationType", relationType))
                .setProjection(Projections.property("user"))
                .list();
    }

    @Override
    public UserToDirectoryRelation getByDirectoryAndUser(UserDirectory directory, User user) {
        return (UserToDirectoryRelation) sessionFactory.getCurrentSession()
                .createQuery("FROM UserToDirectoryRelation rel WHERE rel.directory = :dir AND rel.user = :user")
                .setParameter("dir", directory)
                .setParameter("user", user)
                .uniqueResult();
    }

    @Override
    public Long getDirectoriesCountByOwnerAndDirectoryIds(User owner, List<Long> idList) {
        return (Long) sessionFactory.getCurrentSession()
                .createCriteria(clazz, "rel")
                .createAlias("rel.directory", "dir")
                .add(Restrictions.eq("user", owner))
                .add(Restrictions.eq("fileRelationType", FileRelationType.OWN))
                .add(Restrictions.in("dir.id", idList))
                .setProjection(Projections.rowCount())
                .uniqueResult();
    }

    @Override
    public User getDirectoryOwner(UserDirectory directory) {
        return (User) sessionFactory.getCurrentSession()
                .createCriteria(clazz)
                .add(Restrictions.eq("directory", directory))
                .add(Restrictions.eq("fileRelationType", FileRelationType.OWN))
                .setProjection(Projections.property("user"))
                .uniqueResult();
    }

    @Override
    public List<FileRelationType> getAllRelationsByDirectoriesAndUser(List<UserDirectory> directories, User user) {
        return sessionFactory.getCurrentSession()
                .createQuery("SELECT rel.fileRelationType FROM UserToDirectoryRelation rel " +
                             "WHERE rel.directory IN :dirs AND rel.user = :user")
                .setParameter("user", user)
                .setParameterList("dirs", directories)
                .list();
    }

    @Override
    public List<UserToDirectoryRelation> getAllAccessibleInRoot(User user, List<String> directoryHashes) {
        return sessionFactory.getCurrentSession()
                .createCriteria(clazz, "rel")
                .createAlias("rel.directory", "dir")
                .add(Restrictions.eq("user", user))
                .add(Restrictions.ne("fileRelationType", FileRelationType.OWN))
                .add(Restrictions.not(Restrictions.in("dir.parentDirectoryHash", directoryHashes)))
                .list();
    }
}
