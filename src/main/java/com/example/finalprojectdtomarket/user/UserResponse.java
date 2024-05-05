package com.example.finalprojectdtomarket.user;

import lombok.Data;

import java.sql.Date;

public class UserResponse {

    //로그인 용
    @Data
    public static class LoginDTO {
        private String username;
        //패스워드가 있어도 되나...
        private String password;
        private Boolean isCheck;

        public LoginDTO(User user, Boolean isCheck) {
            this.username = user.getUsername();
            this.password = user.getPassword();
            this.isCheck = isCheck;
        }
    }


    @Data
    public static class JoinDTO {
        private String password;
        private String username;
        private String phone;
        private String email;
        private Date birth;
        private Integer role;

        public JoinDTO(User user) {
            this.password = user.getPassword();
            this.username = user.getUsername();
            this.phone = user.getPhone();
            this.email = user.getEmail();
            this.birth = user.getBirth();
            this.role = user.getRole();
        }
    }
}
