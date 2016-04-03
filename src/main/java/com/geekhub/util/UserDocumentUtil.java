package com.geekhub.util;

import com.geekhub.entity.User;
import com.geekhub.entity.UserDocument;
import com.geekhub.enums.DocumentAttribute;
import com.geekhub.enums.DocumentStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class UserDocumentUtil {

    private static String[] bytes = {"byte", "KB", "MB", "GB"};

    public static Map<String, List<UserDocument>> prepareDocumentsListMap(List<UserDocument> allDocuments) {
        List<UserDocument> privateDocuments = new ArrayList<>();
        List<UserDocument> publicDocuments = new ArrayList<>();
        List<UserDocument> forFriendsDocuments = new ArrayList<>();
        allDocuments.forEach(doc -> {
            if (doc.getDocumentAttribute() == DocumentAttribute.PRIVATE) {
                privateDocuments.add(doc);
            }
            if (doc.getDocumentAttribute() == DocumentAttribute.PUBLIC) {
                publicDocuments.add(doc);
            }
            if (doc.getDocumentAttribute() == DocumentAttribute.FOR_FRIENDS) {
                forFriendsDocuments.add(doc);
            }
        });
        Map<String, List<UserDocument>> userDocumentsListMap = new HashMap<>();
        userDocumentsListMap.put("allDocumentsTable", allDocuments);
        userDocumentsListMap.put("privateDocumentsTable", privateDocuments);
        userDocumentsListMap.put("publicDocumentsTable", publicDocuments);
        userDocumentsListMap.put("forFriendsDocumentsTable", forFriendsDocuments);
        return userDocumentsListMap;
    }

    public static UserDocument createUserDocument(MultipartFile multipartFile, String description, User user)
            throws IOException {
        UserDocument document = new UserDocument();
        document.setName(multipartFile.getOriginalFilename());
        document.setDescription(description);
        document.setLastModifyTime(Calendar.getInstance().getTime());
        document.setType(multipartFile.getContentType());
        document.setSize(calculateSize(multipartFile.getSize()));
        document.setContent(multipartFile.getBytes());
        document.setOwner(user);
        document.setDocumentAttribute(DocumentAttribute.PRIVATE);
        document.setDocumentStatus(DocumentStatus.ACTUAL);
        return document;
    }

    public static UserDocument updateUserDocument(UserDocument document, MultipartFile multipartFile, String description)
            throws IOException {
        document.setDescription(description);
        document.setLastModifyTime(Calendar.getInstance().getTime());
        document.setSize(calculateSize(multipartFile.getSize()));
        document.setContent(multipartFile.getBytes());
        document.setDocumentStatus(DocumentStatus.ACTUAL);
        return document;
    }

    private static String calculateSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[] {"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}