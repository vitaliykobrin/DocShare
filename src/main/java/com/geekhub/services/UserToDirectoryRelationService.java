package com.geekhub.services;

import com.geekhub.entities.User;
import com.geekhub.entities.UserDirectory;
import com.geekhub.entities.UserToDirectoryRelation;
import com.geekhub.entities.enums.FileRelationType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface UserToDirectoryRelationService extends EntityService<UserToDirectoryRelation, Long> {

    List<UserToDirectoryRelation> create(UserDirectory directory, List<User> users, FileRelationType relationType);

    UserToDirectoryRelation create(UserDirectory directory, User user, FileRelationType relationType);

    void deleteAllBesidesOwnerByDirectory(UserDirectory directory);

    List<UserToDirectoryRelation> getAllByDirectory(UserDirectory directory);

    Set<UserDirectory> getAllAccessibleDirectories(User user);

    Set<UserToDirectoryRelation> getAllAccessibleInRoot(User user, List<String> directoryHashes);

    List<User> getAllUsersByDirectoryIdAndRelation(UserDirectory directory, FileRelationType relationType);

    UserToDirectoryRelation getByDirectoryAndUser(UserDirectory directory, User user);

    List<FileRelationType> getAllRelationsByDirectoriesAndUser(List<UserDirectory> directories, User user);

    Long getDirectoriesCountByOwnerAndDirectoryIds(User owner, Long[] directoryIds);

    User getDirectoryOwner(UserDirectory directory);
}
