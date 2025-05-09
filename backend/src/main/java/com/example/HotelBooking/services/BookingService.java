package com.example.HotelBooking.services;

import com.example.HotelBooking.dtos.BookingDTO;
import com.example.HotelBooking.dtos.Response;

/**
 * ClassName:BookingService
 * Package:com.example.HotelBooking.services
 * Description:
 *
 * @date:2025-04-27 9:03 p.m.
 * @author:Qss
 */


public interface BookingService {

    Response getAllBookings();

    Response createBooking(BookingDTO bookingDTO);

    Response updateBooking(BookingDTO bookingDTO);

    Response findBookingByReferenceNo(String bookingReference);
}
