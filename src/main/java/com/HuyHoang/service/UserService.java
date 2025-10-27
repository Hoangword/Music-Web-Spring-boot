package com.HuyHoang.service;

import com.HuyHoang.DTO.request.UserCreationRequest;
import com.HuyHoang.DTO.request.UserUpdateRequest;
import com.HuyHoang.DTO.response.UserResponse;
import com.HuyHoang.Entity.Role;
import com.HuyHoang.Entity.User;
import com.HuyHoang.exception.AppException;
import com.HuyHoang.exception.ErrorCode;
import com.HuyHoang.mapper.UserMapper;
import com.HuyHoang.repository.RoleRepository;
import com.HuyHoang.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
//    @Autowired
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    UserMapper userMapper;



    public UserResponse createUser(UserCreationRequest request){
        User user =new User();

        if(userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }



        user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        user.setRoles(roles);
        return userMapper.toUserRespone(userRepository.save(user));
    }


    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse updateUser(String userId, UserUpdateRequest request){
        User user = userRepository.findById(userId).
                orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user,request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        var roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));
        return userMapper.toUserRespone(userRepository.save(user));

    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> getAllUserPages(int page, int size, String sortBy, String direction){

        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page,size,sort);
        return userRepository.findAll(pageable).map(userMapper::toUserRespone);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponse> searchUser(String keyword, String role, String email){

        Pageable pageable = PageRequest.of(0, 5);
        return userRepository.searchUser(keyword,role,email,pageable).map(userMapper::toUserRespone);
    }


    public UserResponse getMyInfo(){
        var context = SecurityContextHolder.getContext();

        var name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserRespone(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deletedUser(String userId){
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers(){
        return userRepository.findAll().stream().map(userMapper::toUserRespone).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String userId){
        return userMapper.toUserRespone(userRepository.findById(userId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }
}
