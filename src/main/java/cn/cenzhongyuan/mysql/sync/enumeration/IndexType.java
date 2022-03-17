package cn.cenzhongyuan.mysql.sync.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IndexType {
    PRIMARY(0,"PRIMARY","PRIMARY"),
    INDEX(1,"INDEX","INDEX"),
    FOREIGN(2,"FOREIGN KEY","FOREIGN KEY");

    private final int code;
    private final String type;
    private final String prefix;
}
