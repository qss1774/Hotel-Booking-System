package com.example.HotelBooking.services.impl;

import com.example.HotelBooking.dtos.Response;
import com.example.HotelBooking.dtos.RoomDTO;
import com.example.HotelBooking.entities.Room;
import com.example.HotelBooking.enums.RoomType;
import com.example.HotelBooking.exceptions.InvalidBookingStateAndDateException;
import com.example.HotelBooking.exceptions.NotFoundException;
import com.example.HotelBooking.repositories.RoomRepository;
import com.example.HotelBooking.services.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * ClassName:RoomServiceImpl
 * Package:com.example.HotelBooking.services.impl
 *
 * Description:
 *
 * 这段代码是一个 RoomServiceImpl 类，属于一个酒店预订系统（或类似管理系统）。
 * 它负责处理关于**房间（Room）**的所有业务逻辑，比如添加、更新、删除、查询房间信息，还包括图片上传和房间类型的查询。
 *
 * 它是 @Service 注解的，说明这是一个**业务逻辑层（Service Layer）**的类。
 *
 * 它实现了 RoomService 接口。
 *
 * 使用了 ModelMapper 把 DTO（数据传输对象）和实体类（Entity）之间相互转换。
 *
 * 用了 RoomRepository（应该是继承了Spring Data JPA的接口）去访问数据库。
 *
 * @date:2025-04-26 10:24 p.m.
 * @author:Qss
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

//    private static final String IMAGE_DIRECTORY = System.getProperty("user.dir") + "/product-image/";
// image directory for our frontend app
    private static final String IMAGE_DIRECTORY_FRONTEND = "/Users/tanshushu/Desktop/my-hotel/public/rooms/";

    /**
     * 添加一个房间
     * 用 modelMapper 把 RoomDTO 转成 Room 实体。
     *
     * 如果有上传图片，调用 saveImage() 方法保存图片，并把图片路径设到 roomToSave。
     *
     * 调用 roomRepository.save(roomToSave) 保存到数据库。
     *
     * 返回一个 Response 对象，提示添加成功。
     * @param roomDTO
     * @param imageFile
     * @return
     */
    @Override
    public Response addRoom(RoomDTO roomDTO, MultipartFile imageFile) {
        Room roomToSave = modelMapper.map(roomDTO, Room.class);

        if (imageFile != null){
            String imagePath = saveImageToFrontend(imageFile);
            roomToSave.setImageUrl(imagePath);
        }

        roomRepository.save(roomToSave);

        return Response.builder()
                .status(200)
                .message("Room added successfully")
                .build();
    }

    /**
     * 根据 roomDTO.getId() 查询数据库找原有房间，如果找不到就抛 NotFoundException。
     *
     * 有新图片就保存新图片并更新图片地址。
     *
     * 根据 roomDTO 里有没有传不同字段，选择性地更新房间（例如只改价格或容量，不会覆盖其他字段）。
     *
     * 保存更新后的房间对象。
     * @param roomDTO
     * @param imageFile
     * @return
     */
    @Override
    public Response updateRoom(RoomDTO roomDTO, MultipartFile imageFile) {

        Room existingRoom = roomRepository.findById(roomDTO.getId())
                .orElseThrow(()-> new NotFoundException("Room not Found"));

        if (imageFile != null && !imageFile.isEmpty()){
            String imagePath = saveImageToFrontend(imageFile);
            existingRoom.setImageUrl(imagePath);
        }

        if (roomDTO.getRoomNumber() != null && roomDTO.getRoomNumber() >= 0){
            existingRoom.setRoomNumber(roomDTO.getRoomNumber());
        }

        if (roomDTO.getPricePerNight() != null && roomDTO.getPricePerNight().compareTo(BigDecimal.ZERO) >= 0){
            existingRoom.setPricePerNight(roomDTO.getPricePerNight());
        }

        if (roomDTO.getCapacity() != null && roomDTO.getCapacity() > 0){
            existingRoom.setCapacity(roomDTO.getCapacity());
        }

        if (roomDTO.getType() != null) existingRoom.setType(roomDTO.getType());

        if (roomDTO.getDescription() != null) existingRoom.setDescription(roomDTO.getDescription());

        roomRepository.save(existingRoom);

        return Response.builder()
                .status(200)
                .message("Room updated successfully")
                .build();
    }

    /**
     * 根据 id 从数据库中删除房间。
     * 先判断房间是否存在，不存在就抛 NotFoundException。
     *
     * 存在则 roomRepository.deleteById(id) 删除。
     *
     * 返回删除成功的响应。
     * @param id
     * @return
     */
    @Override
    public Response deleteRoom(Long id) {

        if (!roomRepository.existsById(id)){
            throw new NotFoundException("Room not Found");
        }

        roomRepository.deleteById(id);
        return Response.builder()
                .status(200)
                .message("Room deleted successfully")
                .build();
    }

    /**
     * 根据 id 从数据库中查询房间。
     * 查数据库拿到房间对象，找不到就抛异常。
     *
     * 用 modelMapper 转成 RoomDTO。
     *
     * 封装到 Response 里返回。
     * @param id
     * @return
     */
    @Override
    public Response getRoomById(Long id) {

        Room room = roomRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Room not Found"));

        RoomDTO roomDTO = modelMapper.map(room, RoomDTO.class);

        return Response.builder()
                .status(200)
                .message("successfully")
                .room(roomDTO)
                .build();
    }

    /**
     * findAll(Sort.by(...)) 从数据库拉所有房间，按id倒序。
     *
     * 把 List<Room> 映射成 List<RoomDTO>。
     * @return
     */
    @Override
    public Response getAllRooms() {

        List<Room> roomList = roomRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));

        List<RoomDTO> roomDTOList = modelMapper.map(roomList, new TypeToken<List<RoomDTO>>(){}.getType());
//所以真正决定 .rooms() 这个方法能不能用的，是你的 Response 这个类有没有一个字段叫 rooms
        return Response.builder()
                .status(200)
                .message("successfully")
                .rooms(roomDTOList)
                .build();
    }

    /**
     * 先做各种日期校验：
     *
     * 入住时间不能早于今天
     *
     * 退房时间不能早于入住时间
     *
     * 入住时间和退房时间不能是同一天
     *
     * 调用 roomRepository.findAvailableRooms(...) 查符合条件的房间。
     *
     * 转成DTO返回。
     * @param checkInDate
     * @param checkOutDate
     * @param roomType
     * @return
     */
    @Override
    public Response getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate, RoomType roomType) {
//        validation:ensure the checkin date is not before today
        if (checkInDate.isBefore(LocalDate.now())){
            throw new InvalidBookingStateAndDateException("check in date cannot be before today");
        }
//        validation:ensure the checkout date is not before the checkout date
        if (checkOutDate.isBefore(checkInDate)){
            throw new InvalidBookingStateAndDateException("check out date cannot be before check in date");
        }

//        validation:ensure the checkin date is not same as check out date
        if (checkInDate.isEqual(checkOutDate)){
            throw new InvalidBookingStateAndDateException("check in date cannot be same as check out date");
        }

        List<Room> roomList = roomRepository.findAvailableRooms(checkInDate, checkOutDate, roomType);

        List<RoomDTO> roomDTOList = modelMapper.map(roomList, new TypeToken<List<RoomDTO>>(){}.getType());


        return Response.builder()
                .status(200)
                .message("success")
                .rooms(roomDTOList)
                .build();
    }

    /**
     * 调用 roomRepository.searchRooms(input)。
     *
     * 转成DTO列表返回。
     * @param input
     * @return
     */
    @Override
    public Response searchRoom(String input) {

        List<Room> roomList = roomRepository.searchRooms(input);

        List<RoomDTO> roomDTOList = modelMapper.map(roomList, new TypeToken<List<RoomDTO>>(){}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .rooms(roomDTOList)
                .build();
    }


    /**
     * 调用 roomRepository.getAllRoomTypes()。
     * @return
     */
    @Override
    public List<RoomType> getAllRoomTypes() {
//        return roomRepository.getAllRoomTypes();
        return Arrays.asList(RoomType.values());
    }

    /**
     * 功能： 保存上传的图片到本地目录，并返回图片路径。
     * 流程：
     *
     * 检查上传的文件类型，必须是图片（contentType以"image/"开头）。
     *
     * 创建图片保存的文件夹（/product-image/），如果没有就创建。
     *
     * 生成一个带UUID的唯一文件名防止覆盖。
     *
     * 将上传文件存储到本地。
     *
     * 返回图片路径。
     *
     * ✏️ 生成唯一文件名、类型检查、防止覆盖，非常规范。
     * @param imageFile
     * @return
     */
//    private String saveImage(MultipartFile imageFile){
//        if (!imageFile.getContentType().startsWith("image/")){
//            throw new IllegalArgumentException("only image files are allowed");
//        }
//
////        create directory to store images if it does not exist
//        File directory = new File(IMAGE_DIRECTORY);
//
//        if (!directory.exists()){
//            directory.mkdirs();
//        }
//
////        Generate a unique filename for the image
//        String uniqueFilename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
//
////        get absolute path of images
//        String imagePath = IMAGE_DIRECTORY + uniqueFilename;
//
//        try {
//            File destinationFile = new File(imagePath);
//            imageFile.transferTo(destinationFile);
//        } catch (Exception e) {
//            throw new IllegalArgumentException(e.getMessage());
//        }
//
//        return imagePath;
//    }

    /**
     * save image to frontend
     * @param imageFile
     * @return
     */
    private String saveImageToFrontend(MultipartFile imageFile){
        if (!imageFile.getContentType().startsWith("image/")){
            throw new IllegalArgumentException("only image files are allowed");
        }

//        create directory to store images if it does not exist
        File directory = new File(IMAGE_DIRECTORY_FRONTEND);

        if (!directory.exists()){
            directory.mkdirs();
        }

//        Generate a unique filename for the image
        String uniqueFilename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();

//        get absolute path of images
        String imagePath = IMAGE_DIRECTORY_FRONTEND + uniqueFilename;

        try {
            File destinationFile = new File(imagePath);
            imageFile.transferTo(destinationFile);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        return "/rooms/" + uniqueFilename;
    }
}
