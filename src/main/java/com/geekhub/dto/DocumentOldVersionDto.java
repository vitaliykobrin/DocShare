package com.geekhub.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@EqualsAndHashCode
public class DocumentOldVersionDto implements Comparable<DocumentOldVersionDto> {

    @Getter @Setter
    private long id;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String changedBy;

    @Getter @Setter
    private String lastModifyTime;

    @Getter @Setter
    private String size;

    @Override
    public int compareTo(DocumentOldVersionDto o) {
        return o.getLastModifyTime().compareTo(this.getLastModifyTime());
    }
}
