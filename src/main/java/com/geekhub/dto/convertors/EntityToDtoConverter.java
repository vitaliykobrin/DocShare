package com.geekhub.dto.convertors;

import com.geekhub.dto.*;

import com.geekhub.entities.*;
import com.geekhub.entities.enums.EventStatus;
import com.geekhub.services.enams.FileType;
import com.geekhub.utils.DateTimeUtils;
import com.geekhub.utils.FileSharedLinkUtil;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class EntityToDtoConverter {

    public static DocumentOldVersionDto convert(DocumentOldVersion oldVersion) {
        DocumentOldVersionDto oldVersionDto = new DocumentOldVersionDto();
        oldVersionDto.setId(oldVersion.getId());
        oldVersionDto.setName(oldVersion.getUserDocument().getName());
        oldVersionDto.setChangedBy(oldVersion.getModifierName());
        oldVersionDto.setLastModifyTime(formatDate(oldVersion.getLastModifyTime()));
        oldVersionDto.setSize(oldVersion.getSize());
        return oldVersionDto;
    }

    public static UserFileDto convert(UserDocument document) {
        UserFileDto documentDto = new UserFileDto();
        documentDto.setId(document.getId());
        documentDto.setType(FileType.DOCUMENT);
        documentDto.setSize(document.getSize());
        documentDto.setLastModifyTime(formatDate(document.getLastModifyTime()));
        documentDto.setModifiedBy(document.getModifierName());
        documentDto.setModifiedById(document.getModifierId());
        documentDto.setName(document.getName());
        documentDto.setAccess(document.getDocumentAttribute().toString());
        return documentDto;
    }

    public static DocumentWithLinkDto convertWithLink(UserDocument document) {
        DocumentWithLinkDto documentDto = new DocumentWithLinkDto();
        documentDto.setId(document.getId());
        documentDto.setType(FileType.DOCUMENT);
        documentDto.setSize(document.getSize());
        documentDto.setLastModifyTime(formatDate(document.getLastModifyTime()));
        documentDto.setModifiedBy(document.getModifierName());
        documentDto.setModifiedById(document.getModifierId());
        documentDto.setName(document.getName());
        documentDto.setAccess(document.getDocumentAttribute().toString());
        return documentDto;
    }

    public static UserFileDto convert(UserDirectory directory) {
        UserFileDto directoryDto = new UserFileDto();
        directoryDto.setId(directory.getId());
        directoryDto.setType(FileType.DIRECTORY);
        directoryDto.setHashName(directory.getHashName());
        directoryDto.setName(directory.getName());
        directoryDto.setAccess(directory.getDocumentAttribute().toString());
        return directoryDto;
    }

    public static ExtendedUserDto extendedConvert(User user) {
        ExtendedUserDto userDto = new ExtendedUserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setLogin(user.getLogin());
        userDto.setEmail(user.getEmail());
        userDto.setCountry(user.getCountry());
        userDto.setState(user.getState());
        userDto.setCity(user.getCity());
        return userDto;
    }

    public static UserDto convert(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setLogin(user.getLogin());
        return userDto;
    }

    public static FriendGroupDto convert(FriendsGroup group) {
        FriendGroupDto groupDto = new FriendGroupDto();
        groupDto.setId(group.getId());
        groupDto.setName(group.getName());

        Set<UserDto> userDtos = group.getFriends().stream()
                .map(EntityToDtoConverter::convert).collect(Collectors.toSet());
        groupDto.setFriends(userDtos);
        return groupDto;
    }

    public static CommentDto convert(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setText(comment.getText());
        commentDto.setDate(formatDate(comment.getDate()));
        if (comment.getOwner() == null) {
            commentDto.setSenderName("Guest");
        } else {
            commentDto.setSenderName(comment.getOwner().getFullName());
        }
        return commentDto;
    }

    public static EventDto convert(Event event) {
        EventDto eventDto = new EventDto();
        eventDto.setText(event.getText());
        eventDto.setLinkText(event.getLinkText());
        eventDto.setLinkUrl(event.getLinkUrl());
        eventDto.setDate(formatDate(event.getDate()));
        eventDto.setStatus(event.getEventStatus() == EventStatus.UNREAD ? "New" : "");
        return eventDto;
    }

    public static RemovedFileDto convert(RemovedDocument document, String removerName) {
        RemovedFileDto removedFileDto = new RemovedFileDto();
        removedFileDto.setFileId(document.getUserDocument().getId());
        removedFileDto.setName(document.getUserDocument().getName());
        removedFileDto.setRemovalDate(formatDate(document.getRemovalDate()));
        removedFileDto.setRemoverName(removerName);
        removedFileDto.setType(FileType.DOCUMENT);
        return removedFileDto;
    }

    public static RemovedFileDto convert(RemovedDirectory directory, String removerName) {
        RemovedFileDto removedFileDto = new RemovedFileDto();
        removedFileDto.setFileId(directory.getUserDirectory().getId());
        removedFileDto.setName(directory.getUserDirectory().getName());
        removedFileDto.setRemovalDate(formatDate(directory.getRemovalDate()));
        removedFileDto.setRemoverName(removerName);
        removedFileDto.setType(FileType.DIRECTORY);
        return removedFileDto;
    }

    public static UserFileDto convert(UserToDocumentRelation relation) {
        UserFileDto fileDto = EntityToDtoConverter.convert(relation.getDocument());
        fileDto.setOwnerName(relation.getUser().getFullName());
        return fileDto;
    }

    public static UserFileDto convert(UserToDirectoryRelation relation) {
        UserFileDto fileDto = EntityToDtoConverter.convert(relation.getDirectory());
        fileDto.setOwnerName(relation.getUser().getFullName());
        return fileDto;
    }

    public static FileSharedLinkDto convert(FileSharedLink fileSharedLink) {
        FileSharedLinkDto linkDto = new FileSharedLinkDto();
        linkDto.setRelationType(fileSharedLink.getRelationType());
        linkDto.setUrl(FileSharedLinkUtil.generateURL(fileSharedLink));
        linkDto.setLastDate(DateTimeUtils.convertLocalDateTime(fileSharedLink.getLastDate()));
        linkDto.setClickNumber(fileSharedLink.getClickNumber());
        linkDto.setMaxClickNumber(fileSharedLink.getMaxClickNumber());
        return linkDto;
    }

    public static Map<UserDto, List<FriendGroupDto>> convertMap(Map<User, List<FriendsGroup>> friendsMap) {
        Map<UserDto, List<FriendGroupDto>> friendsDtoMap = new TreeMap<>();
        for (User user : friendsMap.keySet()) {
            List<FriendGroupDto> userGroupDtoList =
                    friendsMap.get(user).stream().map(EntityToDtoConverter::convert).collect(Collectors.toList());
            friendsDtoMap.put(EntityToDtoConverter.convert(user), userGroupDtoList);
        }
        return friendsDtoMap;
    }

    public static List<UserDto> convertToBaseUserDtos(List<User> users) {
        return users.stream().map(EntityToDtoConverter::convert).collect(Collectors.toList());
    }

    public static List<FriendGroupDto> convertToFriendGroupDtos(List<FriendsGroup> groups) {
        return groups.stream().map(EntityToDtoConverter::convert).collect(Collectors.toList());
    }

    public static List<DocumentOldVersionDto> convertToVersionDtos(UserDocument document, User user) {
        List<DocumentOldVersionDto> versions = document.getDocumentOldVersions().stream()
                .map(EntityToDtoConverter::convert)
                .sorted(DocumentOldVersionDto::compareTo)
                .collect(Collectors.toList());
        versions.stream()
                .filter(dto -> dto.getChangedBy().equals(user.getFullName()))
                .forEachOrdered(dto -> dto.setChangedBy("Me"));
        return versions;
    }

    private static String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM.dd.yy hh:mm:ss");
        return formatter.format(dateTime);
    }
}
