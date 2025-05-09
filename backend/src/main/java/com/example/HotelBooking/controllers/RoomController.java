package com.example.HotelBooking.controllers;

import com.example.HotelBooking.dtos.Response;
import com.example.HotelBooking.dtos.RoomDTO;
import com.example.HotelBooking.enums.RoomType;
import com.example.HotelBooking.services.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ClassName:RoomController
 * Package:com.example.HotelBooking.controllers
 * Description:
 *
 * @date:2025-04-27 12:32 a.m.
 * @author:Qss
 */

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * ResponseEntity：用来自定义返回的 HTTP 响应（包括状态码、Body内容等）
     * Response：你们项目里封装好的一个统一返回体（比如有status, message字段
     * response.data —— 对应的是后端 ResponseEntity里的body部分
     *
     * response.status —— 对应的是后端 ResponseEntity里的状态码
     *
     * response.headers —— 对应的是后端 ResponseEntity里的headers
     *
     * 这个接口要求客户端提交这些表单参数，比如前端可以通过表单上传：
     * 因为是@RequestParam，所以这些值都是从表单中来的，而不是JSON Body
     * @param roomNumber
     * @param type
     * @param pricePerNight
     * @param capacity
     * @param description
     * @param imageFile
     * @return
     */
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> addRoom(
            @RequestParam Integer roomNumber,
            @RequestParam RoomType type,
            @RequestParam BigDecimal pricePerNight,
            @RequestParam Integer capacity,
            @RequestParam String description,
            @RequestParam MultipartFile imageFile
            ){
// 把上面收集到的表单参数，封装成一个RoomDTO对象。
//（DTO就是数据传输对象，便于后续调用Service）
//用的是Builder模式，让代码更简洁。
        RoomDTO roomDTO = RoomDTO.builder()
                .roomNumber(roomNumber)
                .type(type)
                .pricePerNight(pricePerNight)
                .capacity(capacity)
                .description(description)
                .build();

//调用 roomService.addRoom()，把房间信息(roomDTO)和图片(imageFile)交给业务逻辑处理。
//addRoom() 会处理保存，比如把房间数据保存到数据库、图片存到服务器。
//处理完后，包装成ResponseEntity返回给前端，HTTP状态是200 OK。
        return ResponseEntity.ok(roomService.addRoom(roomDTO, imageFile));
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateRoom(
            @RequestParam (value = "roomNumber", required = false) Integer roomNumber,
            @RequestParam (value = "type", required = false) RoomType type,
            @RequestParam (value = "pricePerNight", required = false) BigDecimal pricePerNight,
            @RequestParam (value = "capacity", required = false) Integer capacity,
            @RequestParam (value = "description", required = false) String description,
            @RequestParam (value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam (value = "id", required = true) Long id
    ){
        RoomDTO roomDTO = RoomDTO.builder()
                .id(id)
                .roomNumber(roomNumber)
                .type(type)
                .pricePerNight(pricePerNight)
                .capacity(capacity)
                .description(description)
                .build();

        return ResponseEntity.ok(roomService.updateRoom(roomDTO, imageFile));

    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllRooms(){
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getRoomById(@PathVariable Long id){
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteRoom(@PathVariable Long id){
        return ResponseEntity.ok(roomService.deleteRoom(id));
    }

    @GetMapping("/avaliable")
    public ResponseEntity<Response> getAvailableRooms(
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate,
            @RequestParam(required = false) RoomType roomType
    ){
        return ResponseEntity.ok(roomService.getAvailableRooms(checkInDate, checkOutDate, roomType));
    }

    /**
     * 它是一个 GET 接口，负责查询并返回所有房间的类型（RoomType 列表）
     *
     * 比如前端要去生成一个下拉框（Dropdown），让管理员选择"房间类型"时，就可以调用这个接口！
     *
     * ResponseEntity<List<RoomType>>
     *
     * ResponseEntity：包装整个HTTP响应（可以控制状态码、头部、内容）
     *
     * List<RoomType>：响应体（Body）里面是一个房间类型列表
     * @return
     */
    @GetMapping("/types")
    public ResponseEntity<List<RoomType>> getAllRoomTypes(){
        return ResponseEntity.ok(roomService.getAllRoomTypes());
    }

    @GetMapping("/search")
    public ResponseEntity<Response> searchRoom(@RequestParam String input){
        return ResponseEntity.ok(roomService.searchRoom(input));
    }

}
