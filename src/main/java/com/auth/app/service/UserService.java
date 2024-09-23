package com.auth.app.service;

import com.auth.app.DAO.PrivilegeRepository;
import com.auth.app.DAO.RoleRepository;
import com.auth.app.DAO.UserRepository;
import com.auth.app.common.AppUtils;
import com.auth.app.common.ServiceConstants;
import com.auth.app.configuration.JWTGenerator;
import com.auth.app.model.domain.Privilege;
import com.auth.app.model.domain.Role;
import com.auth.app.model.domain.User;
import com.auth.app.exception.ClientException;
import com.auth.app.exception.ExceptionMessageCreator;
import com.auth.app.model.DTO.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


import java.time.ZonedDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.auth.app.common.AppUtils.generateID;
import static com.auth.app.common.AppUtils.getUsername;


@Component
@Service
@Transactional
public class UserService implements UserDetailsService, UserDetailsPasswordService, ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTGenerator jwtGenerator;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private ExceptionMessageCreator messageCreator;



    static final Logger LOGGER = Logger.getLogger(UserService.class.getName());


    public boolean createUser(CreateUserDTO dto , boolean isAdmin) {
        if(repository.getUserByEmail(dto.getEmail()).isPresent()){
            throw ClientException.of(messageCreator.createMessage(ServiceConstants.USERNAME_ALREADY_EXISTS));
        }
        String roleString =  isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(roleString));
        User tempUser =
                new User();
                        tempUser.setFirstName(dto.getFirstName());
                        tempUser.setLastName(dto.getLastName());
                        tempUser.setEmail(dto.getEmail());
                        tempUser.setDateModified(ZonedDateTime.now());
                        tempUser.setActive(false);
                        tempUser.setIsSystemAdmin(false);
                        tempUser.setIsDeleted(false);
                        tempUser.setPhone(dto.getPhone());
                        tempUser.setRoles(roles);
                        tempUser.setPasswordHash(AppUtils.hash(dto.getPassword()));
        User user = repository.save(tempUser);
        return true;
    }

    public Token getToken(LoginDTO loginModel){
        User user = repository.getUserByEmail(loginModel.getEmail()).orElseThrow(() -> ClientException.of(messageCreator.createMessage(ServiceConstants.WRONG_EMAIL_OR_PASSWORD)));
        Authentication authentication = authenticationManager
            .authenticate(new UsernamePasswordAuthenticationToken(loginModel.getEmail(), loginModel.getPassword()));

        String token = jwtGenerator.generateToken(authentication,
            String.format("%s,%s", user.getEmail(), user.getPhone()), new HashMap<>());
        return new Token(token, "", ProfileDTO.fromUserModel(user), ServiceConstants.JWT_EXPIRATION);

    }

    public Token refreshToken(String refreshToken) {
        return null;
    }

    public List<ProfileDTO> getUsers() {
        return repository.findAll().stream().map(ProfileDTO::fromUserModel).toList();
    }

    public User getUserByEmail(String email) {
        return repository.getUserByEmail(email).orElseThrow(() -> ClientException.of(messageCreator.createMessage(ServiceConstants.USER_NOT_FOUND)));
    }

    public ProfileDTO getUserProfileByEmail(String email) {
        User user =  repository.getUserByEmail(email).orElseThrow(() -> ClientException.of(messageCreator.createMessage(ServiceConstants.USER_NOT_FOUND)));
        return mapper.map(user, ProfileDTO.class);
    }

    public boolean deleteUser(String email) {
        User user =  repository.getUserByEmail(email).orElseThrow(() -> ClientException.of(messageCreator.createMessage(ServiceConstants.USER_NOT_FOUND)));
        String currentUser = repository.getUserByEmail(getUsername()).get().getEmail();
        if(currentUser.contentEquals(user.getEmail())){
            throw ClientException.of(messageCreator.createMessage(ServiceConstants.CANT_DELETE_LOGGED_IN_USER));
        }else if (!user.getRoles().stream().filter(e -> e.getName().contains("ADMIN")).toList().isEmpty()){
            throw ClientException.of(messageCreator.createMessage(ServiceConstants.CANT_DELETE_THIS_USER));
        }
        repository.delete(user);
        return true;
    }

    public User replaceUser(User newUser, UUID id) {
          User oldUser = repository.findById(id).orElse(new User());
          if(oldUser.getId() == null){
              oldUser.setId(id);
          }
          oldUser.setEmail(newUser.getEmail());
          oldUser.setFirstName(newUser.getFirstName());
          oldUser.setMiddleName(newUser.getMiddleName());
          oldUser.setLastName(newUser.getLastName());
          oldUser.setPasswordHash(newUser.getPasswordHash());
          oldUser.setIsDeleted(newUser.getIsDeleted());
          oldUser.setIsSystemAdmin(newUser.getIsSystemAdmin());
          oldUser.setPhone(newUser.getPhone());
          oldUser.setRoles(newUser.getRoles());
          oldUser.setActive(newUser.isActive());
          oldUser.setDateCreated(newUser.getDateCreated());
          oldUser.setDateModified(newUser.getDateModified());
          oldUser.setDateDeleted(newUser.getDateDeleted());
          return repository.save(oldUser);
    }

    public ProfileDTO updateUserDetails(ProfileDTO profileDTO)  {
        String email = profileDTO.getEmail();
        var user = repository.getUserByEmail(email).orElseThrow(() -> ClientException.of(messageCreator.createMessage(ServiceConstants.USER_NOT_FOUND)));
            user.setFirstName(profileDTO.getFirstName());
            user.setLastName(profileDTO.getLastName());
            user.setActive(profileDTO.getIsActive());
            user.setPhone(profileDTO.getPhone());
            User newRecord = repository.saveAndFlush(user);
            return mapper.map(newRecord, ProfileDTO.class);
    }

    public boolean changePassword(ChangePasswordDTO dto) {
        User user = getLoggedUser();
                if(user == null){
                    throw ClientException.of(messageCreator.createMessage(ServiceConstants.USER_NOT_FOUND));
                }
        if (AppUtils.hashMatch(dto.getOldPassword(), user.getPasswordHash())) throw new IllegalArgumentException(messageCreator.createMessage(ServiceConstants.WRONG_PASSWORD));
        user.setPasswordHash(AppUtils.hash(dto.getNewPassword()));
        replaceUser(user, user.getId());
        return true;
    }





    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = repository.getUserByEmail(username).orElseThrow(() -> ClientException.of(messageCreator.createMessage(ServiceConstants.USER_NOT_FOUND)));
            return user.toUserDetails();
    }

    @Override
    public UserDetails updatePassword(UserDetails user,String newPassword) {
        User userCredentials = repository.getUserByEmail((user.getUsername())).orElseThrow(() -> ClientException.of(messageCreator.createMessage(ServiceConstants.USER_NOT_FOUND)));
        userCredentials.setPasswordHash(newPassword);
        return userCredentials.toUserDetails();
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (alreadySetup) return;
        User adminUser = null;
        try {
           adminUser = getUserByEmail("admin@authapp.com");

        }catch (Exception e){}
        String newPassword = generateID(10, false);

        if (adminUser == null){
            Privilege readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
            Privilege writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");


        Set<Privilege> adminPrivileges = new HashSet<>();
        adminPrivileges.add(readPrivilege);
        adminPrivileges.add(writePrivilege);



        Set<Privilege> userPrivileges = new HashSet<>();
        adminPrivileges.add(readPrivilege);



        createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
        createRoleIfNotFound("ROLE_USER", userPrivileges);

        Role adminRole = roleRepository.findByName("ROLE_ADMIN");
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        User user = new User();
        user.setFirstName("Admin");
        user.setLastName("Admin");
        user.setPasswordHash(AppUtils.hash(newPassword));
        user.setEmail("admin@authapp.com");
        user.setRoles(roles);
        user.setActive(true);
        adminUser = repository.saveAndFlush(user);
        alreadySetup = true;
        }else{
            adminUser.setPasswordHash(AppUtils.hash(newPassword));
        repository.saveAndFlush(adminUser);
        }
        alreadySetup = true;
        Logger.getAnonymousLogger().log(Level.INFO, "Password : "+ newPassword);

    }

    @Transactional
    public Privilege createPrivilegeIfNotFound(String name) {
        var privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new Privilege(null, name);
            privilegeRepository.save(privilege);
        }
        return privilege;
    }

    @Transactional
    public void createRoleIfNotFound(String name, Set<Privilege> privileges) {
        Role role = roleRepository.findByName(name);

        if (role == null) {
            role = new Role(null, name, privileges, ZonedDateTime.now(), null);
            roleRepository.save(role);
        }
    }

    public Collection<GrantedAuthority> getAuthorities(Collection<Role> roles)  {
        return getGrantedAuthorities(getPrivileges(roles));
    }

    public List<String> getPrivileges(Collection<Role> roles) {
        List<String> privileges = new ArrayList<>();
        List<Privilege> collection = new ArrayList<>();
        roles.forEach(role -> {
            privileges.add(role.getName());
             collection.addAll(role.getPrivileges());
        });

        for (Privilege item : collection) {
            privileges.add(item.getName());
        };
        return privileges;
    }

    public List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : privileges) {
           authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }

    public User getLoggedUser() {
        return repository.getUserByEmail(AppUtils.getUsername()).orElseThrow(() -> ClientException.of(messageCreator.createMessage(ServiceConstants.USER_NOT_FOUND)));
    }



}



