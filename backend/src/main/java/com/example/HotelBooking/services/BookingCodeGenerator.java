package com.example.HotelBooking.services;

import com.example.HotelBooking.entities.BookingReference;
import com.example.HotelBooking.repositories.BookingReferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * ClassName:BookingCodeGenerator
 * Package:com.example.HotelBooking.services
 * Description:
 *
 * @date:2025-04-27 9:11 p.m.
 * @author:Qss
 */

/**
 * 工具类：
 *
 * 客户提交一个预定（BookingDTO）
 *
 * BookingServiceImpl处理：
 *
 * 调用 BookingCodeGenerator.generateBookingReference()
 *
 * 得到一个10位字母编号，比如 ABCD1234EF
 *
 * 保存这个编号到 BookingReference表
 *
 * 最后把编号也返回给前端
 */
@Service
@RequiredArgsConstructor
public class BookingCodeGenerator {
/// 注入 BookingReferenceRepository，用来查询和保存预定编号
    private final BookingReferenceRepository  bookingReferenceRepository;

    /**
     * 生成预定编号
     * @return
     */
    public String generateBookingReference(){
// 循环生成，直到生成一个数据库中不存在的编号
        String bookingReference;
//        keep generating until a unique code is found
        do {
            // 调用生成随机字母字符串的方法，生成长度为10的预定编号
            bookingReference = generateRandomAlphaNumericCode(10);// generate code of length 10
        }while (isBookingReferenceExist(bookingReference));// check if the code already exists in the database,
//        if it doesn't exits, save it to the database and return it

        saveBookingReferenceToDatabase(bookingReference);
        // 返回这个新生成的编号
        return bookingReference;
    }

    /**
     * 生成随机的10位字母编号
     * random.nextInt(n) 的意思就是：
     *
     * 随机生成一个 0 到 n-1 之间的整数
     *
     * 包括0
     *
     * 包括 n-1
     *
     * 但是不包括 n 以外的数
     * @param length
     * @return
     */
    private String generateRandomAlphaNumericCode(int length){
        // 定义可用字符集，只包含大写英文字母
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWSYZ";
        Random random = new Random();

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < length; i++){
            // 每次随机选一个字符加入到结果中，循环 length 次
            int index = random.nextInt(characters.length());// 在0到(字母表长度-1)之间随机生成一个数字
            stringBuilder.append(characters.charAt(index));// 把对应的字母加到结果中
        }

        return stringBuilder.toString();// 返回最终生成的字符串
//        String是不可变的！（一旦创建，每次修改其实是新建一个对象），内部其实会很慢，会生成很多无用的中间字符串，浪费内存，性能很差！
//stringBuilder.toString() 是什么？ | ✅ 把拼接好的字符集合，最终变成一个标准的 String
//为什么要用？ | ✅ 因为 StringBuilder高效拼接，但最后要返回普通字符串给别人用

//        StringBuilder 是一个可变的字符序列（可以不停 .append() 加内容）
//但是前端 / 数据库 / 网络传输 ➔ 最后需要的是标准的 String
//所以最后要 .toString()，把这个“拼接器”生成成一个正式的字符串！
    }

    private boolean isBookingReferenceExist(String bookingReference){
        return bookingReferenceRepository.findByReferenceNo(bookingReference).isPresent();
    }

    private void saveBookingReferenceToDatabase(String bookingReference){
        BookingReference newBookingReference = BookingReference.builder()
                .referenceNo(bookingReference)
                .build();
        bookingReferenceRepository.save(newBookingReference);
    }

}
