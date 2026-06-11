package org.example.domain.po;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String userName;
    private String password;
    private String nickName;
    private String phone;
    private String role;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}