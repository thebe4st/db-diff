package cn.cenzhongyuan.mysql.sync.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IndexType {
    PRIMARY(0,"PRIMARY KEY","PRIMARY"),
    INDEX(1,"INDEX","INDEX"),
    FOREIGN(2,"FOREIGN KEY","FOREIGN KEY");

    private final int code;
    private final String name;
    private final String prefix;
}
