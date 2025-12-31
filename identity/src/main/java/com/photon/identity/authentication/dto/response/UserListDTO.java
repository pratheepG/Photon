package com.photon.identity.authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class UserListDTO {
    private String userId;
    private String firstName;
    private String lastName;
    private String userName;
    private String phoneNumber;
    private Boolean isEnabled;
    private Date createdOn;
    private String roleName;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}