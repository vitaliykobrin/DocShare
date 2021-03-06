package com.geekhub.services;

import com.geekhub.entities.FriendGroupToDocumentRelation;
import com.geekhub.entities.FriendsGroup;
import com.geekhub.entities.User;
import com.geekhub.entities.UserDocument;
import com.geekhub.entities.enums.FileRelationType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FriendGroupToDocumentRelationService extends EntityService<FriendGroupToDocumentRelation, Long> {

    List<FriendGroupToDocumentRelation> create(UserDocument document, List<FriendsGroup> groups,
                                               FileRelationType relationType);

    FriendGroupToDocumentRelation create(UserDocument document, FriendsGroup group, FileRelationType relationType);

    void deleteAllByDocument(UserDocument document);

    List<FriendsGroup> getAllGroupsByDocumentId(Long documentId);

    List<FriendsGroup> getAllGroupsByDocumentIdAndRelation(UserDocument document, FileRelationType relationType);

    List<User> getAllGroupMembersByDocumentId(Long documentId);

    List<FileRelationType> getAllRelationsByDocumentIdAndUser(Long documentId, User user);

    List<FriendGroupToDocumentRelation> getAllByDocument(UserDocument document);

    Long getCountByFriendGroup(FriendsGroup group);
}
