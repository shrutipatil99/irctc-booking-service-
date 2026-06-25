package com.irctc.booking_service.service;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.nio.file.Paths;
import java.util.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.irctc.booking_service.entities.User;
import com.irctc.booking_service.Util.UserServiceUtil;
import com.irctc.booking_service.entities.Train;
import com.irctc.booking_service.entities.Ticket;

public class UserBookingService{
	
		private User loggedInUser;
		private List<User>userList;
		
		private  ObjectMapper objectMapper = new ObjectMapper();		//need to add fasterxml file in dependency in gradle
		
		private static final String USER_PATH="local_Db/users.json";
		
		
		public UserBookingService(User loggedInUser)throws IOException  {
			this.loggedInUser=loggedInUser;
			 loadUserListFromFile();
		}
		
		public UserBookingService()throws IOException {
			loadUserListFromFile();		
		}
		
		private void loadUserListFromFile() throws IOException {
			/*File users= new File(USER_PATH);
	        userList = objectMapper.readValue(users,new TypeReference<List<User>>() {});       
										      //why we are using TypeReference bcz we are taking user from list
												//readValue posiblity to thorew error  so we use IOException and later we handle with try catch
		*/
			
			File usersFile = new File(USER_PATH);
			
			    if (!usersFile.exists()) {

			        // ✅ ADD THIS (create folder first)
			        File parentDir = usersFile.getParentFile();
			        if (parentDir != null && !parentDir.exists()) {
			            parentDir.mkdirs();
			        }

			        usersFile.createNewFile();   // now it will work
			        userList = new ArrayList<>();
			        return;
			    }
			    if (usersFile.length() == 0) {
			        userList = new ArrayList<>();
			        return;
			    }

			    userList = objectMapper.readValue(usersFile, new TypeReference<List<User>>() {});
			}
		
		// Login page
		public boolean loginUser(String username, String password) {
			for (User user : userList) {
			    if (user.getName().equals(username) &&
			        UserServiceUtil.checkPassword(password, user.getHashedPassword())) {

			        this.loggedInUser = user; // ✅ clear and correct
			        return true;
			    }
			
		    }
		    return false;
		}
		
		public boolean isUserLoggedIn() {
		    return loggedInUser != null;
		}
		public User getLoggedInUser() {
		    return loggedInUser;
		}
		
		
		//Signup page
		
		
		public boolean signup(User newUser) {
		    // Check if username already exists (case-insensitive)
		    Optional<User> existingUser = userList.stream()
		        .filter(u -> u.getName().equalsIgnoreCase(newUser.getName()))
		        .findFirst();

		    if(existingUser.isPresent()) {
		        System.out.println("Username already exists! Please choose another.");
		        return false;
		    }

		    try {
		        userList.add(newUser);
		        saveUserListToFile();
		        System.out.println("Signup successful ✅");
		        return true;
		    } catch (IOException ex){
		        System.out.println("Error saving user data!");
		        return false;
		    }
		}
		
	
		 private void saveUserListToFile() throws IOException {
		        File usersFile = new File(USER_PATH);
		        objectMapper.writeValue(usersFile, userList);
		    }
		 //no need this 
		/* public void fetchBookings(){
		        Optional<User> userFetched = userList.stream().filter(user1 -> {
		            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
		        }).findFirst();
		        if(userFetched.isPresent()){
		            userFetched.get().printTickets();
		        }
		    }*/
		 
		 public void fetchBookings() {
			    if (loggedInUser == null) {
			        System.out.println("Please login first ❌");
			        return;
			    }
			    loggedInUser.printTickets();
			}
		 	
		 public boolean cancelBooking(int bookingIndex) {

			    // ✅ 1. Check login
			    if (loggedInUser == null) {
			        System.out.println("Please login first ❌");
			        return false;
			    }

			    // ✅ 2. Get tickets
			    List<Ticket> tickets = loggedInUser.getTicketsBooked();

			    if (tickets == null || tickets.isEmpty()) {
			        System.out.println("No bookings found ❌");
			        return false;
			    }

			    // ✅ 3. Validate index
			    if (bookingIndex < 0 || bookingIndex >= tickets.size()) {
			        System.out.println("Invalid booking selection ❌");
			        return false;
			    }

			    // ✅ 4. Get ticket
			    Ticket ticket = tickets.get(bookingIndex);

			    // ✅ 5. Get train + seat details
			    Train train = ticket.getTrain();
			    List<List<Integer>> seats = train.getSeats();

			    int row = ticket.getRow();
			    int col = ticket.getCol();

			    // ✅ 6. Free seat
			    seats.get(row).set(col, 0);

			    // ✅ 7. SAVE updated train (VERY IMPORTANT)
			    try {
			        TrainService trainService = new TrainService();
			        trainService.addTrain(train);
			    } catch (IOException e) {
			        e.printStackTrace();
			    }

			    // ✅ 8. Remove booking
			    tickets.remove(bookingIndex);

			    // ✅ 9. Save user data
			    try {
			        saveUserListToFile();
			    } catch (IOException e) {
			        e.printStackTrace();
			    }

			    return true;
			}
		 
			
		    public List<Train> getTrains(String source, String destination){
		        try{
		            TrainService trainService = new TrainService();
		            return trainService.searchTrains(source, destination);
		        }catch(IOException ex){
		            return new ArrayList<>();
		        }
		    }
	
		    public List<List<Integer>> fetchSeats(Train train){
		        if (train == null || train.getSeats() == null) {
		            System.out.println("Seats not available for this train ❌");
		            return new ArrayList<>();
		        }
		        return train.getSeats();
		    }
		   
		    public boolean bookTrainSeat(Train train, int row, int col,
                    String source, String destination, String date) {
		    	
		    	if (loggedInUser == null) {
		    	    System.out.println("Please login first ❌");
		    	    return false;
		    	}
		    	
		    	
		        try{
		            TrainService trainService = new TrainService();
		            List<List<Integer>> seats = train.getSeats();
		            if (row >= 0 && row < seats.size() && col >= 0 && col < seats.get(row).size()) {
		                if (seats.get(row).get(col) == 0) {
		                    seats.get(row).set(col, 1);
		                    train.setSeats(seats);
		                    trainService.addTrain(train);
		                    
		                    Ticket ticket = new Ticket(
		                    	    UUID.randomUUID().toString(),
		                    	    loggedInUser.getUserId(),
		                    	    source,
		                    	    destination,
		                    	    date,
		                    	    train,
		                    	    row,
		                    	    col
		                    	);
		                
		                    loggedInUser.getTicketsBooked().add(ticket);
		                    try {
		                        saveUserListToFile();  // ✅ now handled
		                    } catch (IOException e) {
		                        e.printStackTrace();
		                        System.out.println("Error saving user data!");
		                    }

		                    return true; // Booking successful
		                } else {
		                    return false; // Seat is already booked
		                }
		            } else {
		                return false; // Invalid row or seat index
		            }
		        }catch (IOException ex){
		            return false;
		        }
		    } 
}
