package com.geekhub.controllers;

import com.geekhub.dto.CommentDto;
import com.geekhub.entities.Comment;
import com.geekhub.entities.DocumentOldVersion;
import com.geekhub.entities.RemovedDirectory;
import com.geekhub.entities.RemovedDocument;
import com.geekhub.entities.User;
import com.geekhub.entities.UserDirectory;
import com.geekhub.entities.UserDocument;
import com.geekhub.entities.enums.DocumentAttribute;
import com.geekhub.dto.UserFileDto;
import com.geekhub.dto.DocumentOldVersionDto;
import com.geekhub.dto.SharedDto;
import com.geekhub.security.UserDirectoryAccessProvider;
import com.geekhub.security.UserDocumentAccessProvider;
import com.geekhub.services.CommentService;
import com.geekhub.services.DocumentOldVersionService;
import com.geekhub.services.EntityService;
import com.geekhub.services.EventService;
import com.geekhub.services.FriendsGroupService;
import com.geekhub.services.RemovedDirectoryService;
import com.geekhub.services.RemovedDocumentService;
import com.geekhub.services.UserDirectoryService;
import com.geekhub.services.UserDocumentService;
import com.geekhub.services.UserService;
import com.geekhub.utils.CommentUtil;
import com.geekhub.utils.DocumentVersionUtil;
import com.geekhub.dto.convertors.EntityToDtoConverter;
import com.geekhub.utils.EventUtil;
import com.geekhub.utils.UserFileUtil;
import com.geekhub.validators.FileValidator;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/document")
public class DocumentController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDocumentService userDocumentService;

    @Autowired
    private UserDirectoryService userDirectoryService;

    @Autowired
    private FileValidator fileValidator;

    @Autowired
    private CommentService commentService;

    @Autowired
    private RemovedDocumentService removedDocumentService;

    @Autowired
    private RemovedDirectoryService removedDirectoryService;

    @Autowired
    private FriendsGroupService friendsGroupService;

    @Autowired
    private DocumentOldVersionService documentOldVersionService;

    @Autowired
    private UserDirectoryAccessProvider directoryAccessProvider;

    @Autowired
    private UserDocumentAccessProvider documentAccessProvider;

    @Autowired
    private EventService eventService;

    private User getUserFromSession(HttpSession session) {
        return userService.getById((Long) session.getAttribute("userId"));
    }

    @InitBinder("multipartFile")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(fileValidator);
    }

    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public ModelAndView upload(HttpSession session) {
        ModelAndView model = new ModelAndView("home");
        User user = getUserFromSession(session);
        model.addObject("tableNames", new String[] {"ALL", "PRIVATE", "PUBLIC", "FOR_FRIENDS"});
        model.addObject("friendsGroups", userService.getAllFriendsGroups(user.getId()));
        model.addObject("friends", userService.getAllFriends(user.getId()));
        model.addObject("userLogin", user.getLogin());
        return model;
    }

    @RequestMapping(value = "/download-{docId}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void downloadDocument(@PathVariable Long docId, HttpSession session, HttpServletResponse response)
            throws IOException {

        User user = getUserFromSession(session);
        UserDocument document = userDocumentService.getById(docId);

        if (documentAccessProvider.canRead(document, user)) {
            File file = UserFileUtil.createFile(document.getHashName());
            response.setContentType(document.getType());
            response.setContentLength((int) file.length());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + document.getName() + "\"");

            FileCopyUtils.copy(Files.newInputStream(file.toPath()), response.getOutputStream());
        }
    }

    @RequestMapping(value = "/move-to-trash", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void moveDocumentToTrash(@RequestParam(value = "docIds[]", required = false) Long[] docIds,
                                    @RequestParam(value = "dirIds[]", required = false) Long[] dirIds,
                                    HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        User user = getUserFromSession(session);
        if (docIds != null) {
            Set<UserDocument> documents = userDocumentService.getByIds(Arrays.asList(docIds));
            if (documentAccessProvider.canRemove(documents, user)) {
                userDocumentService.moveToTrash(docIds, userId);
                documents.forEach(doc -> {
                    String eventTxt = "Document " + doc.getName() + " has been removed by " + user.toString();
                    eventService.save(EventUtil.createEvents(
                            userDocumentService.getAllReadersAndEditors(doc.getId()), eventTxt, user
                    ));
                });
            }
        }
        if (dirIds != null) {
            Set<UserDirectory> directories = userDirectoryService.getByIds(Arrays.asList(dirIds));
            if (directoryAccessProvider.canRemove(directories, user)) {
                userDirectoryService.moveToTrash(dirIds, userId);
                directories.forEach(dir -> {
                    String eventText = "Directory " + dir.getName() + " has been removed by " + user.toString();
                    eventService.save(EventUtil.createEvents(
                            userDirectoryService.getAllReaders(dir.getId()), eventText, user
                    ));
                });
            }
        }
    }

    @RequestMapping(value = "/recover", method = RequestMethod.GET)
    public ModelAndView recoverDocument(HttpSession session) {
        ModelAndView model = new ModelAndView("recover");

        Long ownerId = (Long) session.getAttribute("userId");
        List<RemovedDocument> documents = removedDocumentService.getAllByOwnerId(ownerId);
        List<RemovedDirectory> directories = removedDirectoryService.getAllByOwnerId(ownerId);

        model.addObject("documents", documents);
        model.addObject("directories", directories);
        return model;
    }

    @RequestMapping(value = "/recover-doc-{remDocId}", method = RequestMethod.POST)
    public ModelAndView recoverDocument(@PathVariable Long remDocId, HttpSession session) {
        User user = getUserFromSession(session);
        if (documentAccessProvider.canRecover(remDocId, user)) {
            Long docId = userDocumentService.recover(remDocId);

            String docName = userDocumentService.getById(docId).getName();
            String eventText = "Document " + docName + " has been recovered by " + user.toString();
            String eventLinkText = "Browse";
            String eventLinkUrl = "/document/browse-" + docId;
            eventService.save(EventUtil.createEvents(
                    userDocumentService.getAllReadersAndEditors(docId), eventText, eventLinkText, eventLinkUrl, user
            ));
            return new ModelAndView("redirect:/document/upload");
        }
        return null;
    }

    @RequestMapping(value = "/recover-dir-{remDirId}", method = RequestMethod.POST)
    public ModelAndView recoverDirectory(@PathVariable Long remDirId, HttpSession session) {
        User user = getUserFromSession(session);
        if (directoryAccessProvider.canRecover(remDirId, user)) {
            Long dirId = userDirectoryService.recover(remDirId);

            String dirName = userDirectoryService.getById(dirId).getName();
            String eventText = "Directory " + dirName + " has been removed by " + user.toString();
            eventService.save(EventUtil.createEvents(userDirectoryService.getAllReaders(dirId), eventText, user));
            return new ModelAndView("redirect:/document/upload");
        }
        return null;
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ModelAndView uploadDocument(@RequestParam("files[]") MultipartFile[] files,
                                       String description,
                                       HttpSession session) throws IOException {

        String parentDirectoryHash = (String) session.getAttribute("parentDirectoryHash");
        User user = getUserFromSession(session);
        if (files.length > 0) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    saveOrUpdateDocument(file, parentDirectoryHash, description, user);
                }
            }
        }
        return new ModelAndView("redirect:/document/upload");
    }

    @RequestMapping(value = "/browse-{docId}", method = RequestMethod.GET)
    public ModelAndView browseDocument(@PathVariable Long docId, HttpSession session) {
        ModelAndView model = new ModelAndView();
        User user = getUserFromSession(session);
        UserDocument document = userDocumentService.getById(docId);
        if (documentAccessProvider.canRead(document, user)) {
            model.setViewName("document");
            model.addObject("doc", EntityToDtoConverter.convert(document));
            model.addObject("location", userDocumentService.getLocation(document));
            return model;
        }
        return null;
    }

    @RequestMapping(value = "/get-comments", method = RequestMethod.GET)
    public Set<CommentDto> getComments(Long docId, HttpSession session) {
        User user = getUserFromSession(session);
        UserDocument document = userDocumentService.getDocumentWithComments(docId);
        if (documentAccessProvider.canRead(document, user)) {
            Set<CommentDto> comments = new TreeSet<>();
            document.getComments().forEach(c -> comments.add(EntityToDtoConverter.convert(c)));
            return comments;
        }
        return null;
    }

    @RequestMapping(value = "/add-comment", method = RequestMethod.POST)
    public CommentDto addComment(String text, Long docId, HttpSession session) {
        User user = getUserFromSession(session);
        UserDocument document = userDocumentService.getById(docId);
        if (documentAccessProvider.canRead(document, user)) {
            if (!text.isEmpty()) {
                Comment comment = CommentUtil.createComment(text, user, document);
                commentService.save(comment);
                return EntityToDtoConverter.convert(comment);
            }
        }
        return null;
    }

    @RequestMapping("/clear-comments")
    @ResponseStatus(HttpStatus.OK)
    public void clearComments(Long docId, HttpSession session) {
        User user = getUserFromSession(session);
        UserDocument document = userDocumentService.getDocumentWithComments(docId);
        if (documentAccessProvider.isOwner(document, user)) {
            document.getComments().clear();
            userDocumentService.update(document);
        }
    }

    @RequestMapping("/make-directory")
    public UserFileDto makeDir(String dirName, HttpSession session) {
        User owner = getUserFromSession(session);
        String parentDirectoryHash = (String) session.getAttribute("parentDirectoryHash");
        UserDirectory directory = makeDirectory(owner, parentDirectoryHash, dirName);
        return EntityToDtoConverter.convert(directory);
    }

    @RequestMapping("/get_document")
    public UserFileDto getUserDocument(Long docId, HttpSession session) {
        User user = getUserFromSession(session);
        UserDocument document = userDocumentService.getById(docId);
        if (documentAccessProvider.isOwner(document, user)) {
            return EntityToDtoConverter.convert(document);
        }
        return null;
    }

    @RequestMapping("/get_directory")
    public UserFileDto getUserDirectory(Long dirId, HttpSession session) {
        User user = getUserFromSession(session);
        UserDirectory directory = userDirectoryService.getById(dirId);
        if (directoryAccessProvider.isOwner(directory, user)) {
            return EntityToDtoConverter.convert(directory);
        }
        return null;
    }

    @RequestMapping(value = "/share_document", method = RequestMethod.POST)
    public UserFileDto shareUserDocument(@RequestBody SharedDto shared, HttpSession session) {
        User user = getUserFromSession(session);
        UserDocument document = userDocumentService.getById(shared.getDocId());
        Set<User> readersAndEditors = userDocumentService.getAllReadersAndEditors(document.getId());

        if (documentAccessProvider.isOwner(document, user)) {
            document.setDocumentAttribute(DocumentAttribute.valueOf(shared.getAccess()));
            document.setReaders(createEntitySet(shared.getReaders(), userService));
            document.setReadersGroups(createEntitySet(shared.getReadersGroups(), friendsGroupService));
            document.setEditors(createEntitySet(shared.getEditors(), userService));
            document.setEditorsGroups(createEntitySet(shared.getEditorsGroups(), friendsGroupService));
            userDocumentService.update(document);

            Set<User> newReadersAndEditorsSet = userDocumentService.getAllReadersAndEditors(document.getId());
            newReadersAndEditorsSet.removeAll(readersAndEditors);
            String eventText = "User " + user.toString() + " has shared document " + document.getName();
            String eventLinkUrl = "/document/browse-" + document.getId();
            String eventLinkText = "Browse";
            eventService.save(EventUtil.createEvents(
                    newReadersAndEditorsSet, eventText, eventLinkText, eventLinkUrl, user
            ));

            newReadersAndEditorsSet = userDocumentService.getAllReadersAndEditors(document.getId());
            readersAndEditors.removeAll(newReadersAndEditorsSet);
            eventText = "User " + user.toString() + " has prohibited access to document " + document.getName();
            eventService.save(EventUtil.createEvents(readersAndEditors, eventText, user));
            return EntityToDtoConverter.convert(document);
        }
        return null;
    }

    @RequestMapping(value = "/share_directory", method = RequestMethod.POST)
    public UserFileDto shareUserDirectory(@RequestBody SharedDto shared, HttpSession session) {
        User user = getUserFromSession(session);
        UserDirectory directory = userDirectoryService.getById(shared.getDocId());
        Set<User> readers = userDirectoryService.getAllReaders(directory.getId());

        if (directoryAccessProvider.isOwner(directory, user)) {
            directory.setDocumentAttribute(DocumentAttribute.valueOf(shared.getAccess()));
            directory.setReaders(createEntitySet(shared.getReaders(), userService));
            directory.setReadersGroups(createEntitySet(shared.getReadersGroups(), friendsGroupService));
            userDirectoryService.update(directory);

            Set<User> newReaderSet = userDocumentService.getAllReadersAndEditors(directory.getId());
            newReaderSet.removeAll(readers);
            String eventText = "User " + user.toString() + " has shared directory " + directory.getName();
            eventService.save(EventUtil.createEvents(newReaderSet, eventText, user));

            newReaderSet = userDocumentService.getAllReadersAndEditors(directory.getId());
            readers.removeAll(newReaderSet);
            eventText = "User " + user.toString() + " has prohibited access to directory " + directory.getName();
            eventService.save(EventUtil.createEvents(readers, eventText, user));
            return EntityToDtoConverter.convert(directory);
        }
        return null;
    }

    private <T, S extends EntityService<T, Long>> Set<T> createEntitySet(long[] ids, S service) {
        Set<T> entitySet = new HashSet<>();
        Arrays.stream(ids).forEach(id -> entitySet.add(service.getById(id)));
        return entitySet;
    }

    @RequestMapping("/history-{docId}")
    public ModelAndView showHistory(@PathVariable Long docId, HttpSession session) {
        ModelAndView model = new ModelAndView("history");
        User user = getUserFromSession(session);
        UserDocument document = userDocumentService.getWithOldVersions(docId);
        if (documentAccessProvider.isOwner(document, user)) {
            List<DocumentOldVersionDto> versions = new ArrayList<>();
            document.getDocumentOldVersions().forEach(v -> versions.add(EntityToDtoConverter.convert(v)));
            model.addObject("versions", versions);
            return model;
        }
        return null;
    }

    @RequestMapping("/accessible-documents")
    public ModelAndView getAccessibleDocuments(HttpSession session) {
        User user = getUserFromSession(session);
        Set<UserDocument> documents = userDocumentService.getAllCanRead(user);
        Set<UserFileDto> documentDtos = new TreeSet<>();
        documents.forEach(d -> documentDtos.add(EntityToDtoConverter.convert(d)));
        ModelAndView model = new ModelAndView("friendsDocuments");
        model.addObject("documents", documentDtos);
        return model;
    }

    @RequestMapping("/get-directory-content-{dirHashName}")
    public Set<UserFileDto> getDirectoryContent(@PathVariable String dirHashName, HttpSession session) {
        User user = getUserFromSession(session);
        if (dirHashName.equals("root")) {
            dirHashName = user.getLogin();
            return getDirectoryContent(dirHashName);
        } else {
            UserDirectory directory = userDirectoryService.getByHashName(dirHashName);
            if (directoryAccessProvider.canRead(directory, user) || dirHashName.equals(user.getLogin())) {
                return getDirectoryContent(dirHashName);
            }
        }
        return null;
    }

    @RequestMapping("get-parent-directory-content-{dirHashName}")
    public Set<UserFileDto> getParentDirectoryContent(@PathVariable String dirHashName, HttpSession session) {
        User user = getUserFromSession(session);
        UserDirectory currentDirectory = userDirectoryService.getByHashName(dirHashName);
        if (directoryAccessProvider.canRead(currentDirectory, user)) {
            return getDirectoryContent(currentDirectory.getParentDirectoryHash());
        }
        return null;
    }

    private Set<UserFileDto> getDirectoryContent(String directoryHashName) {
        List<UserDocument> documents;
        List<UserDirectory> directories;
        documents = userDocumentService.getAllByParentDirectoryHash(directoryHashName);
        directories = userDirectoryService.getAllByParentDirectoryHash(directoryHashName);

        Set<UserFileDto> dtoList = new TreeSet<>();
        if (documents != null) {
            documents.forEach(d -> {
                if (d.getOwner() != null) {
                    dtoList.add(EntityToDtoConverter.convert(d));
                }
            });
        }
        if (directories != null) {
            directories.forEach(d -> {
                if (d.getOwner() != null) {
                    dtoList.add(EntityToDtoConverter.convert(d));
                }
            });
        }
        return dtoList;
    }

    private void saveOrUpdateDocument(MultipartFile multipartFile,
                                      String parentDirectoryHash,
                                      String description,
                                      User user) throws IOException {

        String docName = multipartFile.getOriginalFilename();
        UserDocument document = userDocumentService.getByFullNameAndOwner(user, parentDirectoryHash, docName);

        if (document == null) {
            RemovedDocument removedDocument =
                    removedDocumentService.getByFullNameAndOwner(user, parentDirectoryHash, docName);
            if (removedDocument == null) {
                document = UserFileUtil.createUserDocument(multipartFile, parentDirectoryHash, description, user);
                multipartFile.transferTo(UserFileUtil.createFile(document.getHashName()));
                userDocumentService.save(document);
            } else {
                Long docId = userDocumentService.recover(removedDocument.getId());
                document = userDocumentService.getDocumentWithOldVersions(docId);
                updateDocument(document, user, description, multipartFile);
            }
        } else if (documentAccessProvider.canEdit(document, user)) {
            document = userDocumentService.getDocumentWithOldVersions(document.getId());
            updateDocument(document, user, description, multipartFile);
        }
    }

    private void updateDocument(UserDocument document, User user, String description, MultipartFile multipartFile)
            throws IOException {
        DocumentOldVersion oldVersion = DocumentVersionUtil.saveOldVersion(document, "Changed by " + user.toString());
        document.getDocumentOldVersions().add(oldVersion);
        userDocumentService.update(UserFileUtil.updateUserDocument(document, multipartFile, description));

        String eventText = "Document " + document.getName() + " has been updated by " + user.toString();
        String eventLinkUrl = "/document/browse-" + document.getId();
        String eventLinkText = "Browse";
        eventService.save(EventUtil.createEvents(userDocumentService.getAllReadersAndEditors(document.getId()),
                eventText, eventLinkText, eventLinkUrl, user));
    }

    private UserDirectory makeDirectory(User owner, String parentDirectoryHash, String dirName) {
        UserDirectory directory = userDirectoryService.getByFullNameAndOwnerId(owner, parentDirectoryHash, dirName);

        if (directory == null) {
            directory = UserFileUtil.createUserDirectory(owner, parentDirectoryHash, dirName);
            long dirId = userDirectoryService.save(directory);
            directory.setId(dirId);
        }
        return directory;
    }
}