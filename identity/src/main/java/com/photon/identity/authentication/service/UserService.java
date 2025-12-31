package com.photon.identity.authentication.service;

import java.util.*;
import java.util.stream.Collectors;

import com.photon.constants.ApplicationConstants;
import com.photon.dto.ApiResponseDto;
import com.photon.enums.ExceptionEnum;
import com.photon.enums.SuccessEnum;
import com.photon.exception.ApplicationException;
import com.photon.grpc.subscriber.CreateSubscriberResponse;
import com.photon.identity.authentication.dto.ElectronicAddressDto;
import com.photon.identity.authentication.dto.UserDto;
import com.photon.identity.authentication.dto.mapper.AddressMapper;
import com.photon.identity.authentication.dto.mapper.ElectronicAddressMapper;
import com.photon.identity.authentication.dto.mapper.UserMapper;
import com.photon.identity.authentication.dto.request.*;
import com.photon.identity.authentication.dto.response.UserListDTO;
import com.photon.identity.authentication.entity.Tenant;
import com.photon.identity.authentication.repository.RoleRepository;
import com.photon.identity.authentication.entity.ElectronicAddress;
import com.photon.identity.authentication.entity.Role;
import com.photon.identity.authentication.entity.User;
import com.photon.identity.authentication.repository.TenantRepository;
import com.photon.identity.authentication.utils.SecurePasswordUtil;
import com.photon.identity.commons.dto.OnboardingSessionDto;
import com.photon.identity.commons.dto.mapper.CdnAssetInfoMapper;
import com.photon.identity.commons.dto.request.CdnAssetInfoDto;
import com.photon.identity.commons.entity.CdnAssetInfo;
import com.photon.identity.commons.enums.AuthAdaptor;
import com.photon.identity.commons.enums.ElectronicAddressType;
import com.photon.identity.commons.enums.ServerConsoleRole;
import com.photon.identity.commons.enums.SubscriberStatus;
import com.photon.identity.commons.grpc.client.SubscriberSyncService;
import com.photon.identity.authentication.repository.UserRepository;
import com.photon.identity.commons.properties.IdentityConfigProperties;
import com.photon.identity.commons.repository.CdnAssetInfoRepository;
import com.photon.identity.commons.utils.IdentityAlertHandler;
import com.photon.properties.AlertEventConfigProperties;
import com.photon.storage.dto.CdnEventDto;
import com.photon.storage.enums.CDNOperation;
import com.photon.storage.producer.StorageEventProducer;
import com.photon.utils.HttpRequestManager;
import com.photon.utils.PhotonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private final Environment environment;
	private final UserRepository userRepository;
	private final PasswordEncoder bcryptEncoder;
	private final RoleRepository roleRepository;
	private final SubscriberSyncService subscriberSyncService;
	private final CdnAssetInfoRepository cdnAssetInfoRepository;
	private final StorageEventProducer storageEventProducer;
	private final IdentityAlertHandler identityAlertHandler;
	private final TenantRepository tenantRepository;
	private final IdentityConfigProperties identityConfigProperties;
	private final AlertEventConfigProperties alertEventConfigProperties;

	private static final String ONBOARDING_SELF_REGISTRATION_ROLE_ID = "photon.identity.onboarding.${IDP}.role-id";
	private static final String ONBOARDING_SELF_REGISTRATION_TENANT_ID = "photon.identity.onboarding.${IDP}.tenant-id";

	@Transactional
	public synchronized ApiResponseDto<?> saveFirstUser(ServerConsoleFirstUserDto user) throws ApplicationException {
		Set<AuthAdaptor> authAdaptors = new HashSet<>();
		try {
			authAdaptors.add(AuthAdaptor.STATIC_PWD);

			if(userRepository.count()>0)
				throw new ApplicationException(ExceptionEnum.ERR_1022.getErrorResponseBody("Super Admin is already registered"), HttpStatus.FORBIDDEN);

			User newUser = new User();
			newUser.setUserName(user.getUserName());
			newUser.setPassword(bcryptEncoder.encode(user.getPassword()));
			newUser.setFirstName(user.getFirstName());
			newUser.setLastName(user.getLastName());
			newUser.setDob(user.getDob());
			newUser.setCreatedOn(new Date());
			newUser.setIsEnabled(true);
			newUser.setIsAccountNonExpired(true);
			newUser.setIsAccountNonLocked(true);
			newUser.setIsCredentialsNonExpired(true);
			newUser.setSex(user.getSex());
			newUser.setIsMfaEnabled(true);
			newUser.setAddress(user.getAddress());
			newUser.getAddress().forEach(a->a.setUser(newUser));
			newUser.setActiveAuthAdapters(authAdaptors);
			newUser.setIsServerConsoleUser(true);
			newUser.setLastPasswordUpdatedOn(new Date());

			Optional<Role> role = this.roleRepository.findByRoleIdAndIdp(ServerConsoleRole.SERVER_CONSOLE_ADMIN_ROLE.name(),"SC_IDP");
			role.orElseThrow(()-> new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to register server console user"), HttpStatus.INTERNAL_SERVER_ERROR));

			Set<Role> roles = new HashSet<>();
			roles.add(role.get());

			Tenant userTenant = new Tenant();
			userTenant.setName(user.getTenant().getName());
			userTenant.setDescription(user.getTenant().getDescription());
			Tenant persistedTenant = tenantRepository.save(userTenant);

			newUser.setRoles(roles);
			newUser.setTenants(Set.of(persistedTenant));

			User usr = userRepository.save(newUser);

			SubscriberRequestDto subscriberRequest = SubscriberRequestDto.builder()
					.subscriberStatus(SubscriberStatus.ACTIVE)
					.subscriberName(usr.getFirstName().concat(" ").concat(usr.getLastName()))
					.userId(usr.getUserId()).subscriberUniqueId(usr.getUserId()).uniqueId(usr.getUserId())
					.userName(usr.getUsername()).email(usr.getEmail()).phoneNumber(usr.getPhoneNumber())
					.countryCode(usr.getCountryCode()).build();

			try {
				CreateSubscriberResponse subscriberResponse = this.subscriberSyncService.createSubscriber(subscriberRequest);
				if (StringUtils.hasText(subscriberResponse.getSubscriberId())) {
					usr.setSubscriberId(subscriberResponse.getSubscriberId());
					userRepository.save(usr);
				}
			} catch (Exception e) {
				log.error("Application Exception while subscribing user to alerts: {}", e.getMessage());
			}

			return SuccessEnum.CREATED.getSuccessResponseBody("user created successfully.");
		} catch(ApplicationException ex) {
			log.error("Application Exception while saveFirstUser: {}", ex.getMessage());
			throw ex;
		} catch(Exception e) {
			log.error("Exception while saveFirstUser: {}", e.getMessage());
			throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to register first user"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public ApiResponseDto<?> saveServerConsoleUser(ServerConsoleUserDto user) throws ApplicationException {
		Set<AuthAdaptor> authAdaptors = new HashSet<>();
		try {
			authAdaptors.add(AuthAdaptor.STATIC_PWD);

			if(userRepository.count()>0)
				throw new ApplicationException(ExceptionEnum.ERR_1022.getErrorResponseBody("Super Admin is already registered"), HttpStatus.FORBIDDEN);

			User newUser = new User();
			newUser.setUserName(user.getUserName());
			newUser.setPassword(bcryptEncoder.encode(user.getPassword()));
			newUser.setFirstName(user.getFirstName());
			newUser.setLastName(user.getLastName());
			newUser.setDob(user.getDob());
			newUser.setCreatedOn(new Date());
			newUser.setIsEnabled(true);
			newUser.setIsAccountNonExpired(true);
			newUser.setIsAccountNonLocked(true);
			newUser.setIsCredentialsNonExpired(true);
			newUser.setSex(user.getSex());
			newUser.setEmail(user.getEmail());
			newUser.setCountryCode(user.getCountryCode());
			newUser.setPhoneNumber(user.getPhoneNumber());
			newUser.setIsMfaEnabled(true);
			newUser.setAddress(user.getAddress());
			newUser.getAddress().forEach(a->a.setUser(newUser));
			newUser.setActiveAuthAdapters(authAdaptors);
			newUser.setIsServerConsoleUser(true);

			Optional<Role> role = this.roleRepository.findByRoleIdAndIdp(user.getServerConsoleRole().name(),"SC_IDP");
			role.orElseThrow(()-> new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to register server console user"), HttpStatus.INTERNAL_SERVER_ERROR));

			Set<Role> roles = new HashSet<>();
			roles.add(role.get());

			Set<ElectronicAddress> electronicAddresses = new HashSet<>();
			if(StringUtils.hasText(user.getPhoneNumber()))
				electronicAddresses.add(new ElectronicAddress(ElectronicAddressType.PHONE, newUser, newUser.getCountryCode(), newUser.getPhoneNumber(), true));
			if(StringUtils.hasText(user.getEmail()))
				electronicAddresses.add(new ElectronicAddress(ElectronicAddressType.E_MAIL, newUser, null, newUser.getEmail(), true));

			if(!electronicAddresses.isEmpty())
				newUser.setElectronicAddress(electronicAddresses);

			Tenant userTenant = tenantRepository.findById(UUID.fromString(user.getTenantId()))
					.orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1051.getErrorResponseBody("Tenant with ID " + user.getTenantId() + " not found."), HttpStatus.BAD_REQUEST));

			newUser.setRoles(roles);
			newUser.setTenants(Set.of(userTenant));

			User usr = userRepository.save(newUser);

			SubscriberRequestDto subscriberRequest = SubscriberRequestDto.builder()
					.subscriberStatus(SubscriberStatus.ACTIVE)
					.subscriberName(usr.getFirstName().concat(" ").concat(usr.getLastName()))
					.userId(usr.getUserId()).subscriberUniqueId(usr.getUserId()).uniqueId(usr.getUserId())
					.userName(usr.getUsername()).email(usr.getEmail()).phoneNumber(usr.getPhoneNumber())
					.countryCode(usr.getCountryCode()).build();

			try {
				CreateSubscriberResponse subscriberResponse = this.subscriberSyncService.createSubscriber(subscriberRequest);
				if (StringUtils.hasText(subscriberResponse.getSubscriberId())) {
					usr.setSubscriberId(subscriberResponse.getSubscriberId());
					userRepository.save(usr);
				}
			} catch (Exception e) {
				log.error("Application Exception while subscribing server console user to alerts: {}", e.getMessage());
			}

			return SuccessEnum.CREATED.getSuccessResponseBody("user created successfully.");
		} catch(ApplicationException ex) {
			log.error("Application Exception while saveServerConsoleUser: {}", ex.getMessage());
			throw ex;
		} catch(Exception e) {
			log.error("Exception while saveServerConsoleUser: {}", e.getMessage());
			throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to register Server Console User"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

    @Transactional
	public ApiResponseDto<?> createUser(UserByAdminRequestDto user) throws ApplicationException {
		Set<AuthAdaptor> authAdaptors = new HashSet<>();
		char[] pwd = null;
		try {

			Optional<ElectronicAddressDto> electronicAddressPhone = user.getElectronicAddress().stream().filter(electronicAddressDto -> electronicAddressDto.getType().equals(ElectronicAddressType.PHONE) && electronicAddressDto.getIsPrimary()).findFirst();
			Optional<ElectronicAddressDto> electronicAddressEmail = user.getElectronicAddress().stream().filter(electronicAddressDto -> electronicAddressDto.getType().equals(ElectronicAddressType.E_MAIL) && electronicAddressDto.getIsPrimary()).findFirst();

			if (electronicAddressPhone.isEmpty() && electronicAddressEmail.isEmpty())
				throw new ApplicationException(ExceptionEnum.ERR_1051.getErrorResponseBody("at-least one primary electronic address (e-mail/phone) needed"), HttpStatus.BAD_REQUEST);

			if(Objects.nonNull(user.getUserName()) && StringUtils.hasText(user.getUserName())){
				if(userRepository.findByUserName(user.getUserName()).isPresent())
					throw new ApplicationException(ExceptionEnum.ERR_1048.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
				authAdaptors.add(AuthAdaptor.TMP_PWD);
			}
			if (electronicAddressPhone.isPresent() && StringUtils.hasText(electronicAddressPhone.get().getValue())) {
				if(userRepository.findByPhoneNumber(electronicAddressPhone.get().getValue()).isPresent())
					throw new ApplicationException(ExceptionEnum.ERR_1049.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
				authAdaptors.add(AuthAdaptor.SMS_OTP);
			}
			if (electronicAddressEmail.isPresent() && StringUtils.hasText(electronicAddressEmail.get().getValue())) {
				if(userRepository.findByEmail(electronicAddressEmail.get().getValue()).isPresent())
					throw new ApplicationException(ExceptionEnum.ERR_1050.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
				authAdaptors.add(AuthAdaptor.EML_OTP);
			}
			if(authAdaptors.isEmpty()){
				throw new ApplicationException(ExceptionEnum.ERR_1047.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
			}

			User newUser = new User();
			newUser.setUserName(user.getUserName());
			newUser.setFirstName(user.getFirstName());
			newUser.setLastName(user.getLastName());
			newUser.setDob(user.getDob());
			newUser.setCreatedOn(new Date());
			newUser.setIsEnabled(true);
			newUser.setIsAccountNonExpired(true);
			newUser.setIsAccountNonLocked(true);
			newUser.setIsCredentialsNonExpired(true);
			newUser.setSex(user.getSex());
			newUser.setIsMfaEnabled(true);
			newUser.setCountryCode(electronicAddressPhone.isEmpty()?"":electronicAddressPhone.get().getCountryCode());
			newUser.setPhoneNumber(electronicAddressPhone.isEmpty()?"":electronicAddressPhone.get().getValue());
			newUser.setEmail(electronicAddressEmail.isEmpty()?"":electronicAddressEmail.get().getValue());
			newUser.setAddress(user.getAddress().stream().map(AddressMapper::toEntity).collect(Collectors.toSet()));
			newUser.setElectronicAddress(user.getElectronicAddress().stream().map(ElectronicAddressMapper::toEntity).collect(Collectors.toSet()));
			newUser.getAddress().forEach(a->a.setUser(newUser));
			newUser.getElectronicAddress().forEach(ea -> ea.setUser(newUser));
			newUser.setActiveAuthAdapters(authAdaptors);
			newUser.setIsServerConsoleUser(false);
			newUser.setLastPasswordUpdatedOn(new Date());

			List<Role> roleList = this.roleRepository.findAllById(user.getRoles());
			if(roleList.isEmpty())
				throw new ApplicationException(ExceptionEnum.ERR_1051.getErrorResponseBody("user role can't be empty"), HttpStatus.BAD_REQUEST);

			Tenant userTenant = tenantRepository.findById(UUID.fromString(user.getTenantId()))
					.orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1051.getErrorResponseBody("Tenant with ID " + user.getTenantId() + " not found."), HttpStatus.BAD_REQUEST));

			newUser.setRoles(new HashSet<>(roleList));
			newUser.setTenants(Set.of(userTenant));

			User usr = userRepository.save(newUser);

			SubscriberRequestDto subscriberRequest = SubscriberRequestDto.builder()
					.subscriberStatus(SubscriberStatus.ACTIVE).subscriberName(usr.getFirstName().concat(" ").concat(usr.getLastName()))
					.userId(usr.getUserId()).subscriberUniqueId(usr.getUserId()).uniqueId(usr.getUserId()).userName(usr.getUsername())
					.email(usr.getEmail()).phoneNumber(usr.getPhoneNumber()).countryCode(usr.getCountryCode()).build();

			CreateSubscriberResponse subscriberResponse = this.subscriberSyncService.createSubscriber(subscriberRequest);

			if(!StringUtils.hasText(subscriberResponse.getSubscriberId()))
				throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to register subscriber"), HttpStatus.INTERNAL_SERVER_ERROR);

			usr.setSubscriberId(subscriberResponse.getSubscriberId());
			userRepository.save(usr);

			if(authAdaptors.contains(AuthAdaptor.TMP_PWD)) {
				pwd = SecurePasswordUtil.generatePassword(16, true);
				Map<String, String> placeHolders = new HashMap<>();
				placeHolders.put("password", new String(pwd));
				placeHolders.put("expiry", PhotonUtils.convertMinutesToFriendlyDuration(this.identityConfigProperties.getTmpPwdExpInMinutes()));
				identityAlertHandler.dispatchUserTempPwdForRegister(placeHolders, usr.getUserId());
			}

			Map<String, String> placeHolders = new HashMap<>();
			placeHolders.put("fullName", user.getFirstName().concat(" ").concat(user.getLastName()));
			identityAlertHandler.dispatchUserRegistrationAck(placeHolders, newUser, authAdaptors);

			return SuccessEnum.CREATED.getSuccessResponseBody("user created successfully.");
		} catch(ApplicationException ex) {
			log.error("Application Exception while registering user: {}", ex.getMessage());
			throw ex;
		} catch(Exception e) {
			log.error("Exception while registering user: {}", e.getMessage());
			throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to register user"), HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			SecurePasswordUtil.wipe(pwd);
		}
	}

	@Transactional
	public ApiResponseDto<?> selfRegistration(UserRequestDto user) throws ApplicationException {
		Set<AuthAdaptor> authAdaptors = new HashSet<>();
		try {

			String idp = HttpRequestManager.getIdp();
			String roleIdStr = environment.getProperty(ONBOARDING_SELF_REGISTRATION_ROLE_ID.replace("${IDP}", idp));
			String tenantIdStr = environment.getProperty(ONBOARDING_SELF_REGISTRATION_TENANT_ID.replace("${IDP}", idp));
			OnboardingSessionDto onboardingSession = getOnboardingSession();

			if(!StringUtils.hasText(roleIdStr) || !StringUtils.hasText(tenantIdStr)){
				throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody(), HttpStatus.INTERNAL_SERVER_ERROR);
			}

			Long roleId = Long.parseLong(roleIdStr);
			UUID tenantId = UUID.fromString(tenantIdStr);

			User newUser = new User();
			Set<ElectronicAddress> electronicAddresses = new HashSet<>();

			if (onboardingSession.getAdaptor().equals(AuthAdaptor.SMS_OTP)) {
				if(userRepository.findByPhoneNumber(onboardingSession.getPhone()).isPresent())
					throw new ApplicationException(ExceptionEnum.ERR_1049.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
				authAdaptors.add(AuthAdaptor.SMS_OTP);
				newUser.setPhoneNumber(onboardingSession.getPhone());
				newUser.setCountryCode(onboardingSession.getCountryCode());
				ElectronicAddress phone = new ElectronicAddress();
				phone.setCountryCode(onboardingSession.getCountryCode());
				phone.setValue(onboardingSession.getPhone());
				electronicAddresses.add(phone);
			}

			if (onboardingSession.getAdaptor().equals(AuthAdaptor.EML_OTP)) {
				if(userRepository.findByEmail(onboardingSession.getEmail()).isPresent())
					throw new ApplicationException(ExceptionEnum.ERR_1050.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
				authAdaptors.add(AuthAdaptor.EML_OTP);
				newUser.setEmail(onboardingSession.getEmail());
				ElectronicAddress email = new ElectronicAddress();
				email.setValue(onboardingSession.getEmail());
				electronicAddresses.add(email);
			}

			newUser.setFirstName(user.getFirstName());
			newUser.setLastName(user.getLastName());
			newUser.setDob(user.getDob());
			newUser.setCreatedOn(new Date());
			newUser.setIsEnabled(true);
			newUser.setIsAccountNonExpired(true);
			newUser.setIsAccountNonLocked(true);
			newUser.setIsCredentialsNonExpired(true);
			newUser.setSex(user.getSex());
			newUser.setIsMfaEnabled(true);
			newUser.setAddress(user.getAddress().stream().map(AddressMapper::toEntity).collect(Collectors.toSet()));
			newUser.setElectronicAddress(electronicAddresses);
			newUser.getAddress().forEach(a->a.setUser(newUser));
			newUser.setActiveAuthAdapters(authAdaptors);
			newUser.setIsServerConsoleUser(false);

			Optional<Role> role = this.roleRepository.findById(roleId);
			role.orElseThrow(()-> new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to register subscriber"), HttpStatus.INTERNAL_SERVER_ERROR));
			Set<Role> roles = new HashSet<>();
			roles.add(role.get());
			newUser.setRoles(roles);

			Tenant userTenant = tenantRepository.findById(tenantId).orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1051.getErrorResponseBody("Tenant with ID " + tenantId + " not found."), HttpStatus.BAD_REQUEST));
			newUser.setTenants(Set.of(userTenant));

			User usr = userRepository.save(newUser);

			SubscriberRequestDto subscriberRequest = SubscriberRequestDto.builder()
					.subscriberStatus(SubscriberStatus.ACTIVE).subscriberName(usr.getFirstName().concat(" ").concat(usr.getLastName()))
					.userId(usr.getUserId()).subscriberUniqueId(usr.getUserId()).uniqueId(usr.getUserId()).userName(usr.getUsername())
					.email(usr.getEmail()).phoneNumber(usr.getPhoneNumber()).countryCode(usr.getCountryCode()).build();

			CreateSubscriberResponse subscriberResponse = this.subscriberSyncService.createSubscriber(subscriberRequest);

			if(!StringUtils.hasText(subscriberResponse.getSubscriberId()))
				throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to register subscriber"), HttpStatus.INTERNAL_SERVER_ERROR);

			usr.setSubscriberId(subscriberResponse.getSubscriberId());
			userRepository.save(usr);

			Map<String, String> placeHolders = new HashMap<>();
			placeHolders.put("fullName", user.getFirstName().concat(" ").concat(user.getLastName()));
			identityAlertHandler.dispatchUserRegistrationAck(placeHolders, newUser, authAdaptors);

			return SuccessEnum.CREATED.getSuccessResponseBody("user created successfully.");
		} catch(ApplicationException ex) {
			log.error("Application Exception while user self-registration: {}", ex.getMessage());
			throw ex;
		} catch(Exception e) {
			log.error("Exception while registering user self-registration: {}", e.getMessage());
			throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to register user"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public ApiResponseDto<?> updateUserByAdmin(UserByAdminRequestDto user, String userId) throws ApplicationException {
		Set<AuthAdaptor> authAdaptors = new HashSet<>();
		char[] pwd = null;
		try {

			Optional<ElectronicAddressDto> electronicAddressPhone = user.getElectronicAddress().stream().filter(electronicAddressDto -> electronicAddressDto.getType().equals(ElectronicAddressType.PHONE) && electronicAddressDto.getIsPrimary()).findFirst();
			Optional<ElectronicAddressDto> electronicAddressEmail = user.getElectronicAddress().stream().filter(electronicAddressDto -> electronicAddressDto.getType().equals(ElectronicAddressType.E_MAIL) && electronicAddressDto.getIsPrimary()).findFirst();

			if (electronicAddressPhone.isEmpty() && electronicAddressEmail.isEmpty())
				throw new ApplicationException(ExceptionEnum.ERR_1051.getErrorResponseBody("at-least one primary electronic address (e-mail/phone) needed"), HttpStatus.BAD_REQUEST);

			if(Objects.nonNull(user.getUserName()) && StringUtils.hasText(user.getUserName())){
				authAdaptors.add(AuthAdaptor.TMP_PWD);
			}
			if (electronicAddressPhone.isPresent() && StringUtils.hasText(electronicAddressPhone.get().getValue())) {
				authAdaptors.add(AuthAdaptor.SMS_OTP);
			}
			if (electronicAddressEmail.isPresent() && StringUtils.hasText(electronicAddressEmail.get().getValue())) {
				authAdaptors.add(AuthAdaptor.EML_OTP);
			}

			User userEntity = userRepository.findByUserId(userId)
					.orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1020.getErrorResponseBody(), HttpStatus.NOT_FOUND));

			if(Objects.nonNull(user.getRoles()) && !user.getRoles().isEmpty()) {
				List<Role> roleList = this.roleRepository.findAllById(user.getRoles());
				if (roleList.isEmpty())
					throw new ApplicationException(ExceptionEnum.ERR_1051.getErrorResponseBody("user role can't be empty"), HttpStatus.BAD_REQUEST);
				userEntity.setRoles(new HashSet<>(roleList));
			}

			User usr = UserMapper.partialUpdate(user, userEntity);

			if(electronicAddressPhone.isPresent() && !usr.getPhoneNumber().equals(electronicAddressPhone.get().getValue())){
				usr.setCountryCode(electronicAddressPhone.get().getCountryCode());
				usr.setPhoneNumber(electronicAddressPhone.get().getValue());
			}

			if(electronicAddressEmail.isPresent() && !usr.getEmail().equals(electronicAddressEmail.get().getValue())){
				usr.setEmail(electronicAddressEmail.get().getValue());
			}

			SubscriberRequestDto subscriberRequest = SubscriberRequestDto.builder()
					.subscriberStatus(SubscriberStatus.ACTIVE).subscriberName(usr.getFirstName().concat(" ").concat(usr.getLastName()))
					.userId(usr.getUserId()).subscriberUniqueId(usr.getUserId()).uniqueId(usr.getUserId()).userName(usr.getUsername())
					.email(usr.getEmail()).phoneNumber(usr.getPhoneNumber()).countryCode(usr.getCountryCode()).build();

			CreateSubscriberResponse subscriberResponse = this.subscriberSyncService.updateSubscriber(subscriberRequest, usr.getSubscriberId());

			if(!StringUtils.hasText(subscriberResponse.getSubscriberId()))
				throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to register subscriber"), HttpStatus.INTERNAL_SERVER_ERROR);

			usr.setSubscriberId(subscriberResponse.getSubscriberId());
			userRepository.save(usr);

			if(alertEventConfigProperties.feature("USER").action("UPDATE_USER_BY_ADMIN").event("success").isEnabled()) {
				Map<String, String> placeHolders = new HashMap<>();
				placeHolders.put("fullName", usr.getFirstName().concat(" ").concat(usr.getLastName()));
				identityAlertHandler.dispatchUserRegistrationAck(placeHolders, usr, authAdaptors);
			}

			return SuccessEnum.CREATED.getSuccessResponseBody("user created successfully.");
		} catch(ApplicationException ex) {
			log.error("Application Exception while updateUserByAdmin : {}", ex.getMessage());
			throw ex;
		} catch(Exception e) {
			log.error("Exception while updateUserByAdmin : {}", e.getMessage());
			throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to update user"), HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			SecurePasswordUtil.wipe(pwd);
		}
	}

	public ApiResponseDto<?> addUserTenant(List<UUID> tenantIds, String userId) {
		try {
			List<Tenant> tenants = tenantRepository.findAllById(tenantIds);
			User user = userRepository.findByUserId(userId)
					.orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1020.getErrorResponseBody(), HttpStatus.NOT_FOUND));
			user.getTenants().addAll(tenants);
			userRepository.save(user);
			return SuccessEnum.SUCCESS.getSuccessResponseBody("user-tenant added successfully.");
		} catch (Exception e) {
			log.error("Exception while addUserTenant : {}", e.getMessage());
			throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to add the user-tenant"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ApiResponseDto<?> removeUserTenant(List<UUID> tenantIds, String userId) {
		try {
			List<Tenant> tenants = tenantRepository.findAllById(tenantIds);
			User user = userRepository.findByUserId(userId)
					.orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1020.getErrorResponseBody(), HttpStatus.NOT_FOUND));
			tenants.forEach(user.getTenants()::remove);
			userRepository.save(user);
			return SuccessEnum.SUCCESS.getSuccessResponseBody("user-tenant removed successfully.");
		} catch (Exception e) {
			log.error("Exception while removeUserTenant : {}", e.getMessage());
			throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to remove the user-tenant"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public ApiResponseDto<?> linkProfileDpWithUser(CdnAssetInfoDto cdnAssetInfoDto){
		try{
			String userId = HttpRequestManager.getUserId();
			CdnAssetInfo cdnAssetInfo = this.cdnAssetInfoRepository.save(CdnAssetInfoMapper.toEntity(cdnAssetInfoDto));
			Optional<User> userOptional = this.userRepository.findByUserId(userId);
			if(userOptional.isEmpty())
				throw new ApplicationException(ExceptionEnum.ERR_1020.getErrorResponseBody(), HttpStatus.NOT_FOUND);
			User user = userOptional.get();

			CdnEventDto cdnEventDto = CdnEventDto.builder().operation(CDNOperation.UPDATE).entityId(userId).metaId(user.getProfilePic().getMetaId()).build();
			this.storageEventProducer.dispatch(cdnEventDto);

			user.setProfilePic(cdnAssetInfo);
			this.userRepository.save(user);
			return SuccessEnum.SUCCESS.getSuccessResponseBody();
		} catch (Exception e) {
			log.error("Exception while linkProfileDpWithUser : {}", e.getMessage());
			throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to link ProfileDp With User"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public ApiResponseDto<?> removeUserProfileDp(){
		try{
			String userId = HttpRequestManager.getUserId();
			Optional<User> userOptional = this.userRepository.findByUserId(userId);
			if(userOptional.isEmpty())
				throw new ApplicationException(ExceptionEnum.ERR_1020.getErrorResponseBody(), HttpStatus.NOT_FOUND);
			User user = userOptional.get();
			user.setProfilePic(null);
			this.userRepository.save(user);
			return SuccessEnum.SUCCESS.getSuccessResponseBody();
		} catch (Exception e) {
			log.error("Exception while removeUserProfileDp : {}", e.getMessage());
			throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to link ProfileDp With User"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public ApiResponseDto<?> linkProfileDpWithUserByUserId(CdnAssetInfoDto cdnAssetInfoDto){
		try{
			String userId = HttpRequestManager.getUserId();
			CdnAssetInfo cdnAssetInfo = this.cdnAssetInfoRepository.save(CdnAssetInfoMapper.toEntity(cdnAssetInfoDto));
			Optional<User> userOptional = this.userRepository.findByUserId(userId);
			if(userOptional.isEmpty())
				throw new ApplicationException(ExceptionEnum.ERR_1020.getErrorResponseBody(), HttpStatus.NOT_FOUND);
			User user = userOptional.get();

			CdnEventDto cdnEventDto = CdnEventDto.builder().operation(CDNOperation.UPDATE).entityId(userId).metaId(user.getProfilePic().getMetaId()).build();
			this.storageEventProducer.dispatch(cdnEventDto);

			user.setProfilePic(cdnAssetInfo);
			this.userRepository.save(user);
			return SuccessEnum.SUCCESS.getSuccessResponseBody();
		} catch (Exception e) {
			log.error("Exception while linkProfileDpWithUserByUserId : {}", e.getMessage());
			throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to link ProfileDp With User"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public ApiResponseDto<?> removeUserProfileDpByUserId(String userId){
		try{
			Optional<User> userOptional = this.userRepository.findByUserId(userId);
			if(userOptional.isEmpty())
				throw new ApplicationException(ExceptionEnum.ERR_1020.getErrorResponseBody(), HttpStatus.NOT_FOUND);
			User user = userOptional.get();
			user.setProfilePic(null);
			this.userRepository.save(user);
			return SuccessEnum.SUCCESS.getSuccessResponseBody();
		} catch (Exception e) {
			log.error("Exception while removeUserProfileDpByUserId : {}", e.getMessage());
			throw new ApplicationException(ExceptionEnum.ERR_1007.getErrorResponseBody("Failed to link ProfileDp With User"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional(readOnly = true)
	public ApiResponseDto<List<UserDto>> getAllServerConsoleUser(int pageNumber, int pageSize){
		try{
			Pageable pageable = PageRequest.of(Math.max(0,pageNumber), Math.max(1,pageSize));
			Page<UserDto> usersPage = userRepository.findByIsServerConsoleUserTrue(pageable).map(UserMapper::toDto);
			return SuccessEnum.SUCCESS.getSuccessResponseBody(usersPage);
		} catch (Exception e) {
			log.error("Exception while getAllServerConsoleUser : {}", e.getMessage());
			throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("Failed to fetch Users"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional(readOnly = true)
	public ApiResponseDto<List<UserDto>> getAllUser(int pageNumber, int pageSize){
		try{
			Pageable pageable = PageRequest.of(Math.max(0,pageNumber), Math.max(1,pageSize));
			Page<UserDto> usersPage = userRepository.findByIsServerConsoleUserFalse(pageable).map(UserMapper::toDto);
			return SuccessEnum.SUCCESS.getSuccessResponseBody(usersPage);
		} catch (Exception e) {
			log.error("Exception while getServerConsoleUsers : {}", e.getMessage());
			throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("Failed to fetch Users"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional(readOnly = true)
	public ApiResponseDto<List<UserListDTO>> getAllUserMinData(int pageNumber, int pageSize){
		try{
			Pageable pageable = PageRequest.of(Math.max(0,pageNumber), Math.max(1,pageSize));
			Page<UserListDTO> usersPage = userRepository.findAllNonConsoleUserSummary(pageable);
			return SuccessEnum.SUCCESS.getSuccessResponseBody(usersPage);
		} catch (Exception e) {
			log.error("Exception while getAllUserMinData : {}", e.getMessage());
			throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("Failed to fetch Users"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional(readOnly = true)
	public ApiResponseDto<UserDto> getUserDetails(String userId){
		try{
			if(!StringUtils.hasText(userId))
				userId = HttpRequestManager.getUserId();
			UserDto user = userRepository.findByUserId(userId).map(UserMapper::toDto)
					.orElseThrow(() -> new ApplicationException(ExceptionEnum.ERR_1020.getErrorResponseBody(), HttpStatus.NOT_FOUND));
			return SuccessEnum.SUCCESS.getSuccessResponseBody(user);
		} catch (Exception e) {
			log.error("Exception while getUserDetails : {}", e.getMessage());
			throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("Failed to fetch User Details"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional
	public ApiResponseDto<?> updateUserStatus(boolean isEnabled, String userId){
		try{
			int modifiedRec = userRepository.updateEnabledStatus(userId, isEnabled);
			if(modifiedRec!=1)
				throw new ApplicationException(ExceptionEnum.ERR_1008.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);

			if(alertEventConfigProperties.feature("USER").action("UPDATE_USER_STATUS").event("success").isEnabled()) {
				identityAlertHandler.dispatchUserEnabledSuccessAck(userId, isEnabled);
			}

			return SuccessEnum.UPDATED.getSuccessResponseBody();
		} catch (Exception e) {
			log.error("Exception while updateUserStatus : {}", e.getMessage());
			throw new ApplicationException(ExceptionEnum.ERR_1000.getErrorResponseBody("Failed to updateUserStatus"), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private static OnboardingSessionDto getOnboardingSession() {
		try {
			return (OnboardingSessionDto) Objects.requireNonNull(HttpRequestManager.getCurrentHttpRequest()).getSession().getAttribute(ApplicationConstants.USER_SELF_REGISTRATION_SESSION);
		} catch (Exception e){
			throw new ApplicationException(ExceptionEnum.ERR_1041.getErrorResponseBody(), HttpStatus.EXPECTATION_FAILED);
		}
	}
	
}