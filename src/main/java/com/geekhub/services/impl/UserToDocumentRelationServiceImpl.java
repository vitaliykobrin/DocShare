package com.geekhub.services.impl;

import com.geekhub.entities.User;
import com.geekhub.entities.UserDocument;
import com.geekhub.entities.UserToDocumentRelation;
import com.geekhub.entities.enums.FileRelationType;
import com.geekhub.repositories.UserToDocumentRelationRepository;
import com.geekhub.services.UserToDocumentRelationService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserToDocumentRelationServiceImpl implements UserToDocumentRelationService {

    @Inject
    private UserToDocumentRelationRepository repository;

    @Override
    public List<UserToDocumentRelation> getAll(String orderParameter) {
        return repository.getAll(orderParameter);
    }

    @Override
    public UserToDocumentRelation getById(Long id) {
        return repository.getById(id);
    }

    @Override
    public UserToDocumentRelation get(String propertyName, Object value) {
        return repository.get(propertyName, value);
    }

    @Override
    public Long save(UserToDocumentRelation entity) {
        return repository.save(entity);
    }

    @Override
    public void update(UserToDocumentRelation entity) {
        repository.update(entity);
    }

    @Override
    public void delete(UserToDocumentRelation entity) {
        repository.delete(entity);
    }

    @Override
    public void deleteById(Long entityId) {
        repository.deleteById(entityId);
    }

    @Override
    public List<UserToDocumentRelation> create(UserDocument document, List<User> users, FileRelationType relationType) {
        if (CollectionUtils.isEmpty(users)) {
            return new ArrayList<>();
        }
        return users.stream().map(u -> create(document, u, relationType)).collect(Collectors.toList());
    }

    @Override
    public UserToDocumentRelation create(UserDocument document, User user, FileRelationType relationType) {
        UserToDocumentRelation relation = new UserToDocumentRelation();
        relation.setDocument(document);
        relation.setUser(user);
        relation.setFileRelationType(relationType);
        save(relation);
        return relation;
    }

    @Override
    public void deleteAllBesidesOwnerByDocument(UserDocument document) {
        repository.deleteAllBesidesOwnerByDocument(document);
    }

    @Override
    public List<UserToDocumentRelation> getAllByDocument(UserDocument document) {
        return repository.getList("document", document);
    }

    @Override
    public List<User> getAllUsersByDocumentIdBesidesOwner(Long documentId) {
        return repository.getAllUsersByDocumentIdBesidesOwner(documentId);
    }

    @Override
    public User getDocumentOwner(UserDocument document) {
        return repository.getDocumentOwner(document);
    }

    @Override
    public List<User> getAllUsersByDocumentAndRelation(UserDocument document, FileRelationType relationType) {
        return repository.getAllUsersByDocumentAndRelation(document, relationType);
    }

    @Override
    public List<String> getAllDocumentHashNamesByOwner(User owner) {
        return repository.getAllDocumentHashNamesByOwner(owner);
    }

    @Override
    public Set<UserDocument> getAllAccessibleDocuments(User user) {
        return repository.getAllAccessibleDocuments(user).stream().collect(Collectors.toSet());
    }

    @Override
    public Set<UserToDocumentRelation> getAllAccessibleInRoot(User user, List<String> directoryHashes) {
        if (CollectionUtils.isEmpty(directoryHashes)) {
            return new HashSet<>();
        }
        return repository.getAllAccessibleDocumentsInRoot(user, directoryHashes).stream().collect(Collectors.toSet());
    }

    @Override
    public UserDocument getDocumentByFullNameAndOwner(String parentDirHash, String docName, User owner) {
        return repository.getDocumentByFullNameAndOwner(parentDirHash, docName, owner);
    }

    @Override
    public List<UserDocument> getAllDocumentsByFullNamesAndOwner(String parentDirHash, List<String> docNames, User owner) {
        if (CollectionUtils.isEmpty(docNames)) {
            return new ArrayList<>();
        }
        return repository.getAllDocumentsByFullNamesAndOwner(parentDirHash, docNames, owner);
    }

    @Override
    public UserToDocumentRelation getByDocumentAndUser(UserDocument document, User user) {
        return repository.getByDocumentAndUser(document, user);
    }

    @Override
    public Long getDocumentsCountByOwnerAndDocumentIds(User owner, Long[] documentIds) {
        List<Long> idList = Arrays.stream(documentIds).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            return 0L;
        }
        return repository.getCountByOwnerAndDocumentIds(owner, idList);
    }

    @Override
    public List<FileRelationType> getAllRelationsByDocumentsAndUser(List<UserDocument> documents, User user) {
        if (CollectionUtils.isEmpty(documents)) {
            return new ArrayList<>();
        }
        return repository.getAllRelationsByDocumentsAndUser(documents, user);
    }
}
