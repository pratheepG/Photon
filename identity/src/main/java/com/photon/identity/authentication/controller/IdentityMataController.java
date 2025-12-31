package com.photon.identity.authentication.controller;

import com.photon.identity.idp.service.IdentityMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/identity-meta")
public class IdentityMataController {

private final IdentityMetaService identityMetaService;

  @Autowired
  public IdentityMataController(IdentityMetaService identityMetaService) {
      this.identityMetaService = identityMetaService;
  }

  @GetMapping
  public ResponseEntity<?> getIdentityMeta() {
    return ResponseEntity.ok().body(this.identityMetaService.getAllMeta());
  }

}