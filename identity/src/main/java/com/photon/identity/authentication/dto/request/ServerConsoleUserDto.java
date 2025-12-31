package com.photon.identity.authentication.dto.request;

import com.photon.identity.authentication.entity.Address;
import com.photon.identity.commons.entity.CdnAssetInfo;
import com.photon.identity.commons.enums.Gender;
import com.photon.identity.commons.enums.ServerConsoleRole;
import com.photon.utils.PhotonUtils;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Date;
import java.util.Map;
import java.util.Set;

@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerConsoleUserDto {
    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private String phoneNumber;
    private String countryCode;
    private String email;
    private Gender sex;
    private String dob;
    @NotNull(message = "tenantId can't be null")
    @NotEmpty(message = "tenantId can't be empty")
    private String tenantId;
    private Set<Address> address;
    private CdnAssetInfo profilePic;
    private ServerConsoleRole serverConsoleRole;
    private Map<String, Object> additionalAttributes;

    public Date getDob() {
        return PhotonUtils.parseDate(this.dob);
    }
}