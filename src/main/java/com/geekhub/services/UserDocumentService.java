package com.geekhub.services;

import com.geekhub.entities.DocumentOldVersion;
import com.geekhub.entities.FriendsGroup;
import com.geekhub.entities.User;
import com.geekhub.entities.UserDocument;
import com.geekhub.entities.enums.DocumentAttribute;
import java.util.Set;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserDocumentService extends EntityService<UserDocument, Long> {

    Set<UserDocument> getByIds(List<Long> docIds);

    List<UserDocument> getAllByOwnerId(Long ownerId);

    void moveToTrash(Long docId, Long removerId);

    void moveToTrash(Long[] docIds, Long removerId);

    void replace(Long docId, String destinationDirectoryHash);

    void replace(Long[] docIds, String destinationDirectoryHash);

    void copy(Long docId, String destinationDirectoryHash);

    void copy(Long[] docIds, String destinationDirectoryHash);

    Long recover(Long removedDocId);

    void recover(Long[] removedDocIds);

    UserDocument getByNameAndOwnerId(Long ownerId, String name);

    UserDocument getByFullNameAndOwner(User owner, String parentDirectoryHash, String name);

    UserDocument getDocumentWithComments(Long docId);

    UserDocument getDocumentWithOldVersions(Long docId);

    Set<User> getAllReadersAndEditors(Long docId);

    Set<DocumentOldVersion> getOldVersions(Long docId);

    UserDocument getWithOldVersions(Long docId);

    List<UserDocument> getActualByParentDirectoryHash(String parentDirectoryHash);

    List<UserDocument> getRemovedByParentDirectoryHash(String parentDirectoryHash);

    List<Object> getActualIdsByParentDirectoryHash(String parentDirectoryHash);

    Set<UserDocument> getAllCanRead(User reader);

    String getLocation(UserDocument document);

    Set<UserDocument> getAllByOwnerAndAttribute(User owner, DocumentAttribute attribute);

    Integer getCountByFriendsGroup(FriendsGroup friendsGroup);

    List<UserDocument> getAllByOwner(User owner);

    Set<UserDocument> searchByName(User owner, String name);
}
