package com.geekhub.resources.utils;

import com.geekhub.entities.User;
import com.geekhub.entities.UserDirectory;
import com.geekhub.entities.UserDocument;
import com.geekhub.services.EntityService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FileControllersUtil {

    public static final String[] ACCESS_ATTRIBUTES = new String[] {"ALL", "PRIVATE", "PUBLIC", "FOR_FRIENDS"};

    public static <T, S extends EntityService<T, Long>> Set<T> createEntitySet(List<Long> ids, S service) {
        return ids.stream()
                .map(service::getById)
                .collect(Collectors.toSet());
    }

    public static boolean cannotReplaceDocuments(Set<UserDocument> documents, String destinationDirHash) {
        return documents.stream().anyMatch(d -> d.getParentDirectoryHash().equals(destinationDirHash));
    }

    public static boolean cannotReplaceDirectories(Set<UserDirectory> directories, String destinationDirHash, User user) {
        return directories.stream().anyMatch(d ->
                d.getParentDirectoryHash().equals(destinationDirHash)
                || (destinationDirHash.equals("root") && d.getParentDirectoryHash().equals(user.getLogin()))
                || d.getHashName().equals(destinationDirHash)
        );
    }

    public static boolean cannotCopyDirectories(Set<UserDirectory> directories, String destinationDirHash) {
        return directories.stream().anyMatch(d -> d.getHashName().equals(destinationDirHash));
    }
}
