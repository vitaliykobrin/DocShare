package com.geekhub.utils;

import com.geekhub.entities.RemovedDirectory;
import com.geekhub.entities.RemovedDocument;
import com.geekhub.entities.User;
import com.geekhub.entities.UserDirectory;
import com.geekhub.entities.UserDocument;
import com.geekhub.entities.enums.DocumentAttribute;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.DecimalFormat;

public class UserFileUtil {

    public static final String ROOT_LOCATION = "C:\\spring_docs\\";
    public static final String SYSTEM_EXTENSION = ".ud";
    public static final Pattern DIRECTORY_NAME_PATTERN = Pattern.compile("^[\\w,\\s,\\.\\-/)/(]+$");
    public static final Pattern DOCUMENT_NAME_PATTERN = Pattern.compile("^[\\w,\\s,\\.\\-/)/(]+\\.[A-Za-z0-9]{1,5}");

    private final static List<String> IMAGE_TYPES = Arrays.asList(
            "image/bmp", "image/png", "image/jpeg", "image/gif"
    );

    private final static List<String> FORBIDDEN_FILE_TYPES = Arrays.asList(
            "application/octet-stream", "application/x-msdownload"
    );

    private static final List<String> SIZE_UNITS = Arrays.asList(
            "B", "kB", "MB", "GB", "TB"
    );

    public static boolean isValidFileUploading(MultipartFile... files) {
        return files.length > 0
                && files.length <= 10
                && Arrays.stream(files).noneMatch(MultipartFile::isEmpty)
                && Arrays.stream(files).map(MultipartFile::getContentType).noneMatch(FORBIDDEN_FILE_TYPES::contains)
                && Arrays.stream(files).map(MultipartFile::getOriginalFilename).allMatch(UserFileUtil::validateDocumentName);
    }

    public static UserDocument createUserDocument(MultipartFile file,
                                                  UserDirectory directory,
                                                  User user) throws IOException {

        UserDocument document = new UserDocument();
        document.setNameWithExtension(file.getOriginalFilename());
        document.setParentDirectoryHash(directory == null ? user.getLogin() : directory.getHashName());
        document.setLastModifyTime(LocalDateTime.now());
        document.setType(file.getContentType());
        document.setSize(convertDocumentSize(file.getSize()));
        document.setModifierName(user.getFullName());
        document.setModifierId(user.getId());

        String parentDirectoryHash = directory == null ? user.getLogin() : directory.getHashName();
        document.setHashName(generateHashName(file.getOriginalFilename(), parentDirectoryHash));
        document.setDocumentAttribute(directory == null ? DocumentAttribute.PRIVATE : directory.getDocumentAttribute());
        return document;
    }

    public static UserDocument updateUserDocument(UserDocument document,
                                                  MultipartFile file,
                                                  User user) throws IOException {

        String hashName = UserFileUtil.generateHashName(document.getName(), document.getParentDirectoryHash());
        document.setLastModifyTime(LocalDateTime.now());
        document.setModifierName(user.getFullName());
        document.setModifierId(user.getId());
        document.setSize(convertDocumentSize(file.getSize()));
        document.setHashName(hashName);
        file.transferTo(UserFileUtil.createFile(hashName));
        return document;
    }

    private static String convertDocumentSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        int digitGroup = getDigitGroup(size);
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
        return decimalFormat.format(getSize(size, digitGroup)) + " " + SIZE_UNITS.get(digitGroup);
    }

    private static int getDigitGroup(long size) {
        return (int) (Math.log10(size) / Math.log10(1024));
    }

    private static double getSize(long size, int digitGroup) {
        return size / Math.pow(1024, digitGroup);
    }

    public static RemovedDocument wrapUserDocumentInRemoved(UserDocument document, Long removerId) {
        RemovedDocument removedDocument = new RemovedDocument();
//        removedDocument.setOwner(document.getOwner());
        removedDocument.setRemoverId(removerId);
        removedDocument.setRemovalDate(LocalDateTime.now());
        removedDocument.setUserDocument(document);
        return removedDocument;
    }

    public static RemovedDirectory wrapUserDirectoryInRemoved(UserDirectory directory, Long removerId) {
        RemovedDirectory removedDirectory = new RemovedDirectory();
//        removedDirectory.setOwner(directory.getOwner());
        removedDirectory.setRemoverId(removerId);
        removedDirectory.setRemovalDate(LocalDateTime.now());
        removedDirectory.setUserDirectory(directory);
        return removedDirectory;
    }

    public static String generateHashName(String name, String parentDirectoryHash) {
        return DigestUtils.sha256Hex(name + parentDirectoryHash + LocalDateTime.now());
    }

    public static File createFile(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            return new File(ROOT_LOCATION + fileName + SYSTEM_EXTENSION);
        }
        return null;
    }

    public static UserDirectory createUserDirectory(User owner, String parentDirHash, String dirName) {
        UserDirectory directory = new UserDirectory();
        directory.setName(dirName);
        directory.setParentDirectoryHash(parentDirHash.equals("root") ? owner.getLogin() : parentDirHash);
        directory.setHashName(generateHashName(dirName, parentDirHash));
        directory.setDocumentAttribute(DocumentAttribute.PRIVATE);
        return directory;
    }

    public static Map<String, Object> createPropertiesMap(User owner, String parentDirectoryHash, String name) {
        Map<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.put("user", owner);
        propertiesMap.put("parentDirectoryHash", parentDirectoryHash);
        propertiesMap.put("name", name);
        return propertiesMap;
    }

    public static void copyFile(String originalFileName, String copiedFileName) {
        try {
            File originalFile = new File(ROOT_LOCATION + originalFileName + SYSTEM_EXTENSION);
            File copiedFile = new File(ROOT_LOCATION + copiedFileName + SYSTEM_EXTENSION);
            FileCopyUtils.copy(originalFile, copiedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static UserDocument copyDocument(UserDocument original) {
        String[] ignoredProperties = new String[] {
                "id", "hashName", "comments", "documentOldVersions"
        };
        UserDocument copy = new UserDocument();
        BeanUtils.copyProperties(original, copy, ignoredProperties);
        copy.setHashName(UserFileUtil.generateHashName(original.getName(), original.getParentDirectoryHash()));
        return copy;
    }

    public static UserDirectory copyDirectory(UserDirectory original) {
        String[] ignoredProperties = new String[] {
                "id", "hashName"
        };
        UserDirectory copy = new UserDirectory();
        BeanUtils.copyProperties(original, copy, ignoredProperties);
        copy.setHashName(UserFileUtil.generateHashName(original.getName(), original.getParentDirectoryHash()));
        return copy;
    }

    public static void removeUserFiles(List<String> filesNames) {
        filesNames.forEach(f -> new File(ROOT_LOCATION + f + SYSTEM_EXTENSION).delete());
    }

    public static boolean validateDocumentNameWithoutExtension(String documentName) {
        return DIRECTORY_NAME_PATTERN.matcher(documentName).matches();
    }

    public static boolean validateDocumentName(String documentName) {
        return DOCUMENT_NAME_PATTERN.matcher(documentName).matches();
    }

    public static boolean validateDirectoryName(String directoryName) {
        return DIRECTORY_NAME_PATTERN.matcher(directoryName).matches();
    }

    public static void createRootDir() {
        File file = new File("C:\\spring_docs");
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public static <T> int countFileNameIndex(List<String> similarFileNames, T file) {
        List<Integer> indexes = new ArrayList<>();
        Pattern namePattern = createPatternForName(file);
        for (String name : similarFileNames) {
            Matcher match = namePattern.matcher(name);
            while (match.find()) {
                int groupCount = match.groupCount();
                String group = match.group(groupCount);
                indexes.add(Integer.valueOf(group));
            }
        }
        for (int i = 1; i <= indexes.size(); i++) {
            if (!indexes.contains(i)) {
                return i;
            }
        }
        return indexes.size() + 1;
    }

    public static <T> Pattern createPatternForName(T file) {
        if (file instanceof UserDirectory) {
            UserDirectory directory = (UserDirectory) file;
            String regex = directory.getName().replace("(", "\\(").replace(")", "\\)") + " \\((\\d+)\\)";
            return Pattern.compile(regex);
        }
        if (file instanceof UserDocument) {
            UserDocument document = (UserDocument) file;
            String regex = document.getNameWithoutExtension().replace("(", "\\(").replace(")", "\\)") + " \\((\\d+)\\)" + document.getExtension();
            return Pattern.compile(regex);
        }
        return null;
    }

    public static String createNamesPattern(List<String> documentNames) {
        StringBuilder pattern = new StringBuilder("'");
        Iterator<String> iterator = documentNames.iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();
            pattern.append(name).append("|").append(name).append(".");
            if (iterator.hasNext()) {
                pattern.append("|");
            }
        }
        pattern.append("'");
        String patternInString = pattern.toString().replace("(", "\\\\(");
        patternInString = patternInString.replace(")", "\\\\)");
        return patternInString;
    }
}
