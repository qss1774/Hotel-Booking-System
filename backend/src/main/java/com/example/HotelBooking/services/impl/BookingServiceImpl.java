package com.example.HotelBooking.services.impl;

import com.example.HotelBooking.dtos.BookingDTO;
import com.example.HotelBooking.dtos.NotificationDTO;
import com.example.HotelBooking.dtos.Response;
import com.example.HotelBooking.entities.Booking;
import com.example.HotelBooking.entities.Room;
import com.example.HotelBooking.entities.User;
import com.example.HotelBooking.enums.BookingStatus;
import com.example.HotelBooking.enums.PaymentStatus;
import com.example.HotelBooking.exceptions.InvalidBookingStateAndDateException;
import com.example.HotelBooking.exceptions.NotFoundException;
import com.example.HotelBooking.repositories.BookingRepository;
import com.example.HotelBooking.repositories.RoomRepository;
import com.example.HotelBooking.services.BookingCodeGenerator;
import com.example.HotelBooking.services.BookingService;
import com.example.HotelBooking.services.NotificationService;
import com.example.HotelBooking.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * | 变量名 | 用途 |
 * |:------|:-----|
 * | `bookingRepository` | 用来操作 **Booking 表**（数据库里的预订记录） |
 * | `roomRepository` | 用来操作 **Room 表**（数据库里的房间信息） |
 * | `notificationService` | 用来发送通知，比如发邮件 |
 * | `modelMapper` | 用来把数据库实体对象（Booking）转换成 DTO 对象（BookingDTO） |
 * | `userService` | 获取当前登录的用户 |
 * | `bookingCodeGenerator` | 生成一个**唯一的预订编号** |
 */

/**
 * ClassName:BookingServiceImpl
 * Package:com.example.HotelBooking.services.impl
 * Description:
 *
 * @date:2025-04-27 9:04 p.m.
 * @author:Qss
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;
    private final UserService userService;

    private final BookingCodeGenerator bookingCodeGenerator;

    /**
     *
     * Get all bookings, sorted by newest first, and hide sensitive data.
     *
     * 中文：
     * 获取所有预订信息（按 id 降序排序），但把每条预订里的 user 和 room 信息隐藏掉（设为 null），防止暴露敏感信息。
     *
     * 简单流程：
     *
     * 查找所有预订记录 ➔ 转成 BookingDTO 列表。
     *
     * 把每个 BookingDTO 里面的 user 和 room 设为 null。
     *
     * 返回封装好的 Response。
     * @return
     */
    @Override
    public Response getAllBookings() {

        List<Booking> bookingList = bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<BookingDTO> bookingDTOList = modelMapper.map(bookingList, new TypeToken<List<BookingDTO>>(){}.getType());

        for (BookingDTO bookingDTO : bookingDTOList){
            bookingDTO.setUser(null);
            bookingDTO.setRoom(null);
        }

        return Response.builder()
                .status(200)
                .message("Booking retrieved successfully")
                .bookings(bookingDTOList)
                .build();

    }

    /**
     * Create a new booking based on user's request, validate dates and availability,
     * calculate total price, save to database, and send a confirmation email.
     *
     * 创建一个新的预订，主要步骤是：
     *
     * 获取当前登录的用户。
     *
     * 根据 roomId 找到对应房间。
     *
     * 做各种检查（重要！！）：
     *
     * 入住日期不能早于今天。
     *
     * 退房日期不能早于入住日期。
     *
     * 入住日期和退房日期不能是同一天。
     *
     * 检查这个房间在这个时间段内是否可用（用 bookingRepository.isRoomAvailable() 方法）。
     *
     * 计算总价格（单价 × 天数）。
     *
     * 生成一个新的预订编号（bookingReference）。
     *
     * 创建 Booking 对象并保存到数据库。
     *
     * 生成一个支付链接，比如：
     * http://localhost:3000/payment/bookingReference/totalPrice
     *
     * 用 notificationService 给用户发一封确认邮件，里面包含支付链接。
     *
     * 返回成功的 Response。
     *
     * ⚡ 小细节提醒：虽然前端传了 totalPrice 和 bookingReference，但是这里完全不信前端，自己重新生成，保证数据安全！
     */
    @Override
    public Response createBooking(BookingDTO bookingDTO) {

        User currentUser = userService.getCurrentLoggedInUser();

        Room room = roomRepository.findById(bookingDTO.getRoomId())
                .orElseThrow(()-> new NotFoundException("Room Not Found"));

//        validate: ensure the checkin date is not before today
        if (bookingDTO.getCheckInDate().isBefore(LocalDate.now())){
            throw new InvalidBookingStateAndDateException("check in date cannot be before today");
        }
//        validation:ensure the checkout date is not before the checkout date
        if (bookingDTO.getCheckOutDate().isBefore(bookingDTO.getCheckInDate())){
            throw new InvalidBookingStateAndDateException("check out date cannot be before check in date");
        }

//        validation:ensure the checkin date is not same as check out date
        if (bookingDTO.getCheckInDate().isEqual(bookingDTO.getCheckOutDate())){
            throw new InvalidBookingStateAndDateException("check in date cannot be same as check out date");
        }

//        validate room avaliability
        boolean isAvailable = bookingRepository.isRoomAvailable(room.getId(), bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());

        if (!isAvailable){
            throw new InvalidBookingStateAndDateException("Room is not available");
        }


//        calculate the total price needed to pay for the stay，
//        BookingDTO 里虽然有传过来的 totalPrice 和 bookingReference
//但是你 完全没有用 bookingDTO.getTotalPrice() 或 bookingDTO.getBookingReference()
//你是自己在 ServiceImpl里重新赋值的！
        BigDecimal totalPrice = calculateTotalPrice(room, bookingDTO);

        String bookingReference = bookingCodeGenerator.generateBookingReference();

//        create and save the booking
        Booking booking = new Booking();
        booking.setUser(currentUser);
        booking.setRoom(room);
        booking.setCheckInDate(bookingDTO.getCheckInDate());
        booking.setCheckOutDate(bookingDTO.getCheckOutDate());
        booking.setTotalPrice(totalPrice);
        booking.setBookingReference(bookingReference);
        booking.setBookingStatus(BookingStatus.BOOKED);
        booking.setPaymentStatus(PaymentStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

//        Booking booking = Booking.builder()
//                .user(currentUser)
//                .room(room)
//                .checkInDate(bookingDTO.getCheckInDate())
//                .checkOutDate(bookingDTO.getCheckOutDate())
//                .totalPrice(totalPrice)
//                .bookingReference(bookingReference)
//                .bookingStatus(BookingStatus.BOOKED)
//                .paymentStatus(PaymentStatus.PENDING)
//                .createdAt(LocalDateTime.now())
//                .build();

        bookingRepository.save(booking);

//        generate the payment url which will be sent via email
        String paymentUrl = "http://localhost:3000/payment/" + bookingReference + "/" + totalPrice;

        log.info("Payment URL: {}", paymentUrl);

//        send notiification via email
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .recipient(currentUser.getEmail())
                .subject("Booking Confirmation")
                .body(String.format("Your booking has been created successfully, please proceed with your payment using the payment link below" +
                        "\n%s", paymentUrl))
                .bookingReference(bookingReference)
                .build();

        notificationService.sendEmail(notificationDTO); // send email

        return Response.builder()
                .status(200)
                .message("Booking created successfully")
                .booking(bookingDTO)
                .build();
    }

    private BigDecimal calculateTotalPrice(Room room, BookingDTO bookingDTO) {
        BigDecimal pricePerNight = room.getPricePerNight();
        long days = ChronoUnit.DAYS.between(bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());
        return pricePerNight.multiply(BigDecimal.valueOf(days));
    }

    /**
     * Update an existing booking's status or payment status.
     *
     * 更新预订，主要是修改预订的状态或者支付状态。
     *
     * 如果传了新的 BookingStatus，就更新。
     *
     * 如果传了新的 PaymentStatus，也更新。
     *
     * 保存回数据库。
     *
     * 返回成功的 Response
     * @param bookingDTO
     * @return
     */
    @Override
    public Response updateBooking(BookingDTO bookingDTO) {
        if (bookingDTO.getId() == null) throw new NotFoundException("Booking id is required");

        Booking existingBooking = bookingRepository.findById(bookingDTO.getId())
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (bookingDTO.getBookingStatus() != null){
            existingBooking.setBookingStatus(bookingDTO.getBookingStatus());
        }

        if (bookingDTO.getPaymentStatus() != null){
            existingBooking.setPaymentStatus(bookingDTO.getPaymentStatus());
        }

        bookingRepository.save(existingBooking);

        return Response.builder()
                .status(200)
                .message("Booking updated successfully")
                .booking(bookingDTO)
                .build();
    }

    /**
     * Find and return a booking by its reference number.
     *
     * 根据预订编号（bookingReference）查找一条预订记录。
     *
     * 找不到就抛出 NotFoundException。
     *
     * 找到的话，转成 BookingDTO，返回成功的 Response。
     * @param bookingReference
     * @return
     */
    @Override
    public Response findBookingByReferenceNo(String bookingReference) {

        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new NotFoundException("Booking with reference No:" + bookingReference + "Not Found"));

        BookingDTO bookingDTO = modelMapper.map(booking, BookingDTO.class);

        return Response.builder()
                .status(200)
                .message("success")
                .booking(bookingDTO)
                .build();
    }
}
