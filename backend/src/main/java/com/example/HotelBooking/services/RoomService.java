package com.example.HotelBooking.services;

import com.example.HotelBooking.dtos.Response;
import com.example.HotelBooking.dtos.RoomDTO;
import com.example.HotelBooking.enums.RoomType;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * ClassName:RoomService
 * Package:com.example.HotelBooking.services
 * Description:
 *
 * @date:2025-04-26 10:18 p.m.
 * @author:Qss
 */


public interface RoomService {

    Response addRoom(RoomDTO roomDTO, MultipartFile imageFile);

    Response updateRoom(RoomDTO roomDTO, MultipartFile imageFile);

    Response deleteRoom(Long id);

    Response getRoomById(Long id);

    Response getAllRooms();

    Response getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate, RoomType roomType);

    Response searchRoom(String input);

    List<RoomType> getAllRoomTypes();

}
