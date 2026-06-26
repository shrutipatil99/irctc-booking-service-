package com.irctc.booking_service;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.UUID;
import java.time.LocalDate;
import com.irctc.booking_service.Util.UserServiceUtil;
import com.irctc.booking_service.entities.Train;
import com.irctc.booking_service.entities.User;
import com.irctc.booking_service.service.UserBookingService;
import com.irctc.booking_service.entities.Ticket;

//import org.springframework.boot.SpringApplication;

//@SpringBootApplication
public class BookingServiceApplication {
	
	

	public static void main(String[] args) {
		
		String lastSource = null;
		String lastDestination = null;
		String lastTravelDate = null;
				
		System.out.println("Running Train Booking System");
        Scanner scanner = new Scanner(System.in);
        int option = 0;
        UserBookingService userBookingService;
        try{
            userBookingService = new UserBookingService();
        }catch(IOException ex){
        	ex.printStackTrace();
            System.out.println("There is something wrong");
            return;
        }
        
        Train trainSelectedForBooking = null; 
        while(option!=7){
            System.out.println("Choose option");
            System.out.println("1. Sign up");
            System.out.println("2. Login");
            System.out.println("3. Fetch Bookings");
            System.out.println("4. Search Trains");
            System.out.println("5. Book a Seat");
            System.out.println("6. Cancel my Booking");
            System.out.println("7. Exit the App");
            option = Integer.parseInt(scanner.next());
            
            switch (option){
                case 1:
                	 scanner.nextLine(); // clear buffer
                	 
                	String nameToSignUp = "";
                	while(nameToSignUp.isEmpty()) {
                	    System.out.println("Enter the username to signup:");
                	    nameToSignUp = scanner.nextLine().trim();
                	   
                	    if(nameToSignUp.isEmpty()) {
                	        System.out.println("Username cannot be empty ❌");
                	    }
                	}

                	String passwordToSignUp = "";
                	while(passwordToSignUp.isEmpty()) {
                	    System.out.println("Enter the password to signup:");
                	    passwordToSignUp = scanner.nextLine().trim();
                	    
                	    if(passwordToSignUp.isEmpty()) {
                	        System.out.println("Password cannot be empty ❌");
                	    }
                	}
                   
                	User userToSignup = new User(
                		    nameToSignUp,
                		    UserServiceUtil.hashPassword(passwordToSignUp),
                		    new ArrayList<>(),
                		    UUID.randomUUID().toString()
                		);
                    
                    userBookingService.signup(userToSignup);
                    break;
                
                case 2:
                    System.out.println("Enter the username to Login");
                    String nameToLogin = scanner.next();

                    System.out.println("Enter the password to Login");
                    String passwordToLogin = scanner.next();

                    if (userBookingService.loginUser(nameToLogin, passwordToLogin)) {
                        System.out.println("Login Successful ✅");
                    } else {
                        System.out.println("Invalid Credentials ❌");
                    }
                    break;
                   
                    
                case 3:
                	if (!userBookingService.isUserLoggedIn()) {
                	    System.out.println("Please login first ❌");
                	    break;
                	}
                	
                    System.out.println("Fetching your bookings");
                    userBookingService.fetchBookings();
                    break;
                    
                case 4:
                	
                    System.out.println("Type your source station");
                    String source = scanner.next();
                    System.out.println("Type your destination station");
                    String dest = scanner.next();
                    
                    String date = "";

                    while (true) {
                        System.out.println("Enter date (yyyy-mm-dd):");
                        date = scanner.next();

                        try {
                            java.time.LocalDate.parse(date);
                            break;
                        } catch (Exception e) {
                            System.out.println("Invalid date format! Please use yyyy-mm-dd ❌");
                        }
                    }

                    lastTravelDate = date;
                    
                 // RESET OLD TRAIN SELECTION HERE
                    trainSelectedForBooking = null;
                    
                    lastSource = source;
                    lastDestination = dest;
                    
                    List<Train> trains = userBookingService.getTrains(source, dest);
                    
                    
                    //  Step 1: check empty
                    if (trains.isEmpty()) {
                        System.out.println("No trains found ❌");
                        break;
                    }

                    //  Step 2: print trains
                    int index = 1;
                    for (Train t : trains) {
                        System.out.println(index + " Train id : " + t.getTrainId());
                        index++;
                    }

                    //  Step 3: ask user to select
                    System.out.println("Select a train by typing 1,2,3...");
                    int choice = Integer.parseInt(scanner.next());

                    if (choice < 1 || choice > trains.size()) {
                        System.out.println("Invalid selection ❌");
                        break;
                    }

                    trainSelectedForBooking = trains.get(choice - 1);
                    break;
                  
              
                	
                case 5:
                	if (!userBookingService.isUserLoggedIn()) {
                	    System.out.println("Please login first ❌");
                	    break;
                	}
                	
                	// validation
                    if (trainSelectedForBooking == null) {
                        System.out.println("Please search and select a train first ❌");
                        break;
                    }

                    if (lastSource == null || lastDestination == null) {
                        System.out.println("Please search train first ❌");
                        break;
                    }

                    System.out.println("Select a seat out of these seats");
                    List<List<Integer>> seats = userBookingService.fetchSeats(trainSelectedForBooking);

                    if (seats == null || seats.isEmpty()) {
                        System.out.println("No seats available ❌");
                        break;
                    }

                    // Display seat matrix
                    for (List<Integer> rowList : seats) {
                        for (Integer val : rowList) {
                            System.out.print(val + " ");
                        }
                        System.out.println();
                    }

                    int row = -1, col = -1;

                    //  Safe row input
                    while (true) {
                        System.out.println("Enter the row number (0 to " + (seats.size() - 1) + "):");
                        String input = scanner.next();
                        try {
                            row = Integer.parseInt(input);
                            if (row < 0 || row >= seats.size()) {
                                System.out.println("Invalid row number ❌");
                            } else {
                                break;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input! Please enter a numeric row ❌");
                        }
                    }

                    //  Safe column input
                    while (true) {
                        System.out.println("Enter the column number (0 to " + (seats.get(0).size() - 1) + "):");
                        String input = scanner.next();
                        try {
                            col = Integer.parseInt(input);
                            if (col < 0 || col >= seats.get(0).size()) {
                                System.out.println("Invalid column number ❌");
                            } else {
                                break;
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input! Please enter a numeric column ❌");
                        }
                    }

                    System.out.println("Booking your seat....");
                    
                    Boolean booked = userBookingService.bookTrainSeat(
                            trainSelectedForBooking, row, col, lastSource, lastDestination, lastTravelDate
                    );
                    
                    if (booked) {
                        System.out.println("Booked! Enjoy your journey ✅");
                     //  RESET OLD TRAIN SELECTION HERE
                        trainSelectedForBooking = null;
                        lastTravelDate = null;
                    } else {
                        System.out.println("Can't book this seat ❌");
                    }
                    break;
                    
                case 6:
                    if (!userBookingService.isUserLoggedIn()) {
                        System.out.println("Please login first ❌");
                        break;
                    }

                    List<Ticket> bookings = userBookingService.getLoggedInUser().getTicketsBooked();

                    if (bookings == null || bookings.isEmpty()) {
                        System.out.println("No bookings to cancel ❌");
                        break;
                    }

                    System.out.println("Your Bookings:");

                    for (int i = 0; i < bookings.size(); i++) {
                        Ticket t = bookings.get(i);
                        System.out.println((i + 1) + ". Train: " + t.getTrain().getTrainId()
                                + " | Seat: (" + t.getRow() + "," + t.getCol() + ")"
                                + " | Date: " + t.getDateOfTravel());
                    }

                    System.out.println("Select booking number to cancel:");
                    int cancelChoice = Integer.parseInt(scanner.next());

                    boolean cancelled = userBookingService.cancelBooking(cancelChoice - 1);

                    if (cancelled) {
                        System.out.println("Booking cancelled successfully ✅");
                    } else {
                        System.out.println("Cancellation failed ❌");
                    }
                    break;
                    
                default:
                    break;
            }
         
        }
     //  ADD HERE
        scanner.close();
        System.out.println("Thank you for using IRCTC 🚆");
	}
}
