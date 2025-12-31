package com.photon.identity.authentication.service;

import com.photon.identity.authentication.entity.TempCredential;
import com.photon.identity.authentication.repository.TempCredentialRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TempCredentialService {

    private final TempCredentialRepository tempCredentialRepository;

    public TempCredentialService(TempCredentialRepository tempCredentialRepository) {
        this.tempCredentialRepository = tempCredentialRepository;
    }

    @Transactional
    public void createAndInvalidateOld(TempCredential newCredential) {
        tempCredentialRepository.deactivateOldCredentials(
                newCredential.getPhoneNumber(),
                newCredential.getOperation()
        );
        newCredential.setActive(true);
        tempCredentialRepository.save(newCredential);
    }

}