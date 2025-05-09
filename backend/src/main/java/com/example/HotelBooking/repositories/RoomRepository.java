package com.example.HotelBooking.repositories;

import com.example.HotelBooking.entities.Room;
import com.example.HotelBooking.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {


//    if (roomType == null) {
//    // 不限制房型
//    返回所有类型的房间 AND (true OR r.type = NULL)   --> 结果为 true
//} else {
//    // 只返回指定类型的房间
//    WHERE r.type = roomType AND (false OR r.type = 'DELUXE')   --> 等价于 AND r.type = 'DELUXE'
//}

// b.room：表示 从 Booking 对象跳到关联的 Room 对象。
//
//b.room.id：表示 拿到 Room 对象的 id 属性。
//
//:roomId：方法传入的参数，用来绑定这个值。

//    从 Room 实体中选出对象，别名叫 r

//    CAST(... AS string)：把房间号 roomNumber 强制转换成字符串
//
//LIKE %:searchParam%：模糊匹配，比如 roomNumber 包含某个数字或字母
//
//    ② LOWER(r.type) LIKE LOWER(:searchParam)
//    LOWER(...)：把房型（RoomType）转成小写字母
//
//    LIKE：模糊匹配，注意这里只是直接匹配，不加 %%，有点问题（等下讲）

//   CAST(r.pricePerNight AS string) LIKE %:searchParam%
//把房价 pricePerNight 转成字符串
//
//用 LIKE 去匹配
//
//✅ 举例：用户输入 "200"，能匹配到价格是 200.00 的房间。
//    ⑤ LOWER(r.description) LIKE LOWER(CONCAT('%', :searchParam, '%'))
//    把描述字段 description 也转小写
//
//    CONCAT('%', :searchParam, '%')：自动在搜索词两边加上 %
//
//    这样就能做完整的模糊匹配了。
//
//            ✅ 举例：输入 "ocean"，可以找到描述里有 "ocean view" 的房间。



    @Query("""
            SELECT r FROM Room r
            WHERE
                r.id NOT IN (
                    SELECT b.room.id
                    FROM Booking b
                    WHERE :checkInDate <= b.checkOutDate
                    AND :checkOutDate >= b.checkInDate
                    AND b.bookingStatus IN ('BOOKED', 'CHECKED_IN')
                )
                AND (:roomType IS NULL OR r.type = :roomType)
            """)
    List<Room> findAvailableRooms(
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("roomType") RoomType roomType
    );


    @Query("""
                SELECT r FROM Room r
                WHERE CAST(r.roomNumber AS string) LIKE %:searchParam%
                   OR LOWER(r.type) LIKE LOWER(:searchParam)
                   OR CAST(r.pricePerNight AS string) LIKE %:searchParam%
                   OR CAST(r.capacity AS string) LIKE %:searchParam%
                   OR LOWER(r.description) LIKE LOWER(CONCAT('%', :searchParam, '%'))
            """)
    List<Room> searchRooms(@Param("searchParam") String searchParam);

//    @Query("SELECT DISTINCT r.type FROM Room r")
//    List<RoomType> getAllRoomTypes();


}
