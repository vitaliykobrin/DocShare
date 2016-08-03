package com.geekhub.dto;

import com.geekhub.entities.enums.FileRelationType;
import com.geekhub.services.enams.FileType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class FileSharedLinkDto {

    @Getter @Setter
    private long fileId;

    @Getter @Setter
    private String url;

    @Getter @Setter
    private FileType fileType;

    @Getter @Setter
    private FileRelationType relationType;

    @Getter @Setter
    private LocalDateTime lastDate;

    @Getter @Setter
    private int maxClickNumber;

    @Getter @Setter
    private int clickNumber;

    @Getter @Setter
    private String token;
}
