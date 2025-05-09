package com.example.HotelBooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HotelBookingApplication {

//	@Autowired --------- 在类上加上implements CommandLineRunner
//	private NotificationService notificationService;

	public static void main(String[] args) {

		SpringApplication.run(HotelBookingApplication.class, args);
	}

//	@Override
//	public void run(String... args) throws Exception{
//		NotificationDTO notificationDTO = NotificationDTO.builder()
//				.subject("Test Notification")
//				.recipient("19976011774qss@gmail.com")
//				.body("This is a test notification.")
//				.bookingReference("ABC123")
//				.type(NotificationType.EMAIL)
//				.build();
//		notificationService.sendEmail(notificationDTO);
//	}

}
