package com.example.HotelBooking.services.impl;

import com.example.HotelBooking.config.ModelMapperConfig;
import com.example.HotelBooking.dtos.*;
import com.example.HotelBooking.entities.Booking;
import com.example.HotelBooking.entities.User;
import com.example.HotelBooking.enums.UserRole;
import com.example.HotelBooking.exceptions.InvalidCredentialException;
import com.example.HotelBooking.exceptions.NotFoundException;
import com.example.HotelBooking.repositories.BookingRepository;
import com.example.HotelBooking.repositories.UserRepository;
import com.example.HotelBooking.security.JwtUtils;
import com.example.HotelBooking.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ClassName:UserServiceImpl
 * Package:com.example.HotelBooking.services.impl
 * Description:
 *
 * @date:2025-04-25 11:15 p.m.
 * @author:Qss
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final ModelMapper modelMapper;
    private final BookingRepository bookingRepository;

    /**
     * registration
     *
     * 根据注册请求创建新用户 → 存入数据库 → 返回一个标准结构的响应对象。
     *
     * @param registrationRequest
     * @return
     */
    @Override
    public Response registerUser(RegistrationRequest registrationRequest) {
        UserRole role = UserRole.CUSTOMER;

        if (registrationRequest.getRole() != null){
            role = registrationRequest.getRole();
        }

        User userToSave = User.builder()
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .phoneNumber(registrationRequest.getPhoneNumber())
                .role(role)
                .isActive(Boolean.TRUE)
                .build();

        userRepository.save(userToSave);

        return Response.builder()
                .status(200)
                .message("user created successfully")
                .build();
    }

    /**
     *
     * @param loginRequest
     * @return
     */
    @Override
    public Response loginUser(LoginRequest loginRequest) {
//        根据邮箱查找用户，不存在就抛出异常。
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

//() -> new RuntimeException(...)
//这是一个 Lambda 表达式，等同于匿名函数：
//new Supplier<RuntimeException>() {
//    public RuntimeException get() {
//        return new RuntimeException("User not found");
//    }
//}

//        用 Spring 的 PasswordEncoder 验证密码是否正确
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new InvalidCredentialException("Password doesn't match");
        }

//        使用自定义的 jwtUtils 工具类生成 JWT 令牌。
        String token = jwtUtils.generateToken(user.getEmail());

//        用 builder 模式构建统一格式的 JSON 响应：
//包含用户角色、激活状态、token、过期时间等字段。
        return Response.builder()
                .status(200)
                .message("User login Successfully")
                .token(token)
                .role(user.getRole())
                .isActive(user.getIsActive())
                .expirationTime("6 months")
                .build();
    }

    /**
     * 从数据库中查找所有用户（按 id 倒序排列）→ 把实体对象映射成 DTO 列表 → 返回统一格式的响应。
     *
     * 查数据库返回 List<User> ➔ 把 List<User> 映射成 List<UserDTO> ➔ 最后封装到 Response 中返回
     *
     * @return
     */
    @Override
    public Response getAllUsers() {

//        从 userRepository 查询所有用户。
//使用 Spring Data JPA 自带的排序功能：按 id 降序排列（最新注册的用户排在最前）。用户id是自增的（新用户 id 更大）
//
//如果你想让新注册的用户排在最前面，当然要用 DESC（降序）啊！
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

//        使用 ModelMapper 把 User 实体列表 转成 UserDTO 列表。 用 modelMapper 把 List<User> 转成 List<UserDTO>。
//TypeToken<List<UserDTO>>(){}.getType() 是因为 ModelMapper 需要知道目标类型是 List<UserDTO>，否则只会映射单个对象。
        List<UserDTO> userDTOList = modelMapper.map(users, new TypeToken<List<UserDTO>>(){}.getType());


//        构建统一的 Response 对象。
//
//status = 200：表示成功。
//
//message = "success"：返回简短提示。
//
//users = userDTOList：把转换好的用户列表放进响应体里。
        return Response.builder()
                .status(200)
                .message("success")
                .users(userDTOList)
                .build();

    }

    /**
     * view profile to see info
     * 1	获取当前登录用户的邮箱
     * 2	根据邮箱查询用户
     * 3	实体对象转成 DTO
     * 4	封装成标准 Response 返回
     * @return
     */
    @Override
    public Response getOwnAccountDetails() {
//        SecurityContextHolder 是 Spring Security 提供的安全上下文。
//getAuthentication().getName() 获取的是当前登录用户的用户名，通常是邮箱（如果你用邮箱作为登录名的话）。
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

//        根据邮箱到数据库中查询用户。
//如果查不到，就抛出自定义的 NotFoundException，告诉前端：“找不到用户”
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        log.info("Inside getOwnAccountDetails user email is {}", email);

//        用 modelMapper 把查到的 User 实体对象转成 UserDTO。
//这样返回的数据就不会包含敏感信息（比如密码）
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

//        用 Response.builder() 返回标准格式的 JSON。
//包含状态码、提示信息、以及当前用户的信息（userDTO）
        return Response.builder()
                .status(200)
                .message("success")
                .user(userDTO)
                .build();
    }

    /**
     * use user entity within our application to perform payment
     * @return
     */
    @Override
    public User getCurrentLoggedInUser() {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    /**
     * 1	获取当前登录用户
     * 2	逐字段判断，部分更新数据
     * 3	特别处理密码字段（加密后再保存）
     * 4	保存到数据库
     * 5	返回统一格式的成功响应
     * @param userDTO
     * @return
     */
    @Override
    public Response updateOwnAccount(UserDTO userDTO) {
        User existingUser = getCurrentLoggedInUser();

        log.info("Inside update user");

//        按字段逐一检查，如果 userDTO 中对应字段不是 null，就把新值覆盖原来的 existingUser 中的值。
//        userDTO.getEmail() 是前端提交的新数据；
//existingUser.setEmail(...) 是把这个新数据覆盖到原本数据库里的实体对象上；
//最后再把这个更新后的实体对象保存回数据库。
        if(userDTO.getEmail() != null) existingUser.setEmail(userDTO.getEmail());
        if(userDTO.getFirstName() != null) existingUser.setFirstName(userDTO.getFirstName());
        if(userDTO.getLastName() != null) existingUser.setLastName(userDTO.getLastName());
        if(userDTO.getPhoneNumber() != null) existingUser.setPhoneNumber(userDTO.getPhoneNumber());

//        特别处理密码更新：只有当新密码不为 null 且不为空字符串时，才更新。
// 更新密码时还用 passwordEncoder 进行了加密，防止明文密码直接存储到数据库里！非常规范。
        if(userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()){
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        userRepository.save(existingUser);

        return Response.builder()
                .status(200)
                .message("user updated successfully")
                .build();
    }

    /**
     * 删除账户
     * @return
     */
    @Override
    public Response deleteOwnAccount() {

        User user = getCurrentLoggedInUser();
        userRepository.delete(user);

        return Response.builder()
                .status(200)
                .message("user deleted successfully")
                .build();
    }

    /**
     * 1	获取当前登录用户
     * 2	查这个用户的所有 Booking
     * 3	把 Booking 实体列表转成 BookingDTO 列表
     * 4	封装到 Response 返回给前端
     * @return
     */
    @Override
    public Response getMyBookingHistory() {

//        调用你自己封装好的方法，获取当前登录的用户。
//（从 SecurityContextHolder 中取出登录的邮箱，再去数据库查 User 实体）
        User user = getCurrentLoggedInUser();

//        根据用户 ID，查询这个用户所有的预订记录（Booking）。
//findByUserId 是你自定义在 BookingRepository 里的方法，返回的是 List<Booking>。
//查询到的是数据库表里面的 Booking 实体列表。
        List<Booking> bookingList = bookingRepository.findByUserId(user.getId());

//      用 modelMapper 把 List<Booking> 转成 List<BookingDTO>。
//和前面 User 转 UserDTO 的原因一样：
//保护敏感信息（比如 Booking 可能包含内部状态）
//只返回前端需要的数据字段
        List<BookingDTO> bookingDTOList = modelMapper.map(bookingList,
                new TypeToken<List<BookingDTO>>(){}.getType());

//        构建一个统一格式的响应，返回状态码 + 消息 + 用户的预订历史。
//保证前端接收到的 Response 结构一致，方便统一处理。
        return Response.builder()
                .status(200)
                .message("success")
                .bookings(bookingDTOList)
                .build();
    }
}
