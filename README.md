# IRCTC Train Booking System

A console-based train ticket booking system built in Java, simulating core IRCTC features — signup/login, train search, seat booking, booking cancellation, and viewing past bookings — with file-based JSON persistence instead of a database.

## Why no Spring Boot / Database?

This project started as a Spring Boot application, but `@SpringBootApplication` is intentionally commented out in `BookingServiceApplication.java` — it currently runs as a **plain Java console app**, not a live Spring context. A `BookingController` with `@RestController` exists in the codebase but is dormant since the Spring context never starts.

Persistence is handled through **Jackson `ObjectMapper` reading/writing JSON files** (`local_Db/users.json`, `local_Db/train.json`) — there's no Repository/JPA layer, by design, since there's no active database connection here.

## Tech Stack

- Java 17
- Gradle (with wrapper)
- Jackson (`jackson-databind`) — JSON file persistence
- jBCrypt — password hashing
- Spring Boot Web (declared as a dependency, but unused at runtime since the app runs as plain Java)
- Lombok

## Architecture

```
src/main/java/com/irctc/
├── booking_service/
│   ├── BookingServiceApplication.java   → main() — console menu loop (entry point)
│   ├── controller/
│   │   └── BookingController.java       → @RestController, dormant (Spring context inactive)
│   ├── service/
│   │   ├── UserBookingService.java      → signup, login, booking, cancellation logic
│   │   └── TrainService.java            → train search, add/update train data
│   ├── entities/
│   │   ├── User.java                    → name, hashedPassword, ticketsBooked, userId
│   │   ├── Train.java                   → trainId, trainNo, seats matrix, stations, stationTimes
│   │   └── Ticket.java                  → embeds the full Train object (see Known Issues)
│   └── Util/
│       └── UserServiceUtil.java         → jBCrypt hash/check password
└── network/                              → standalone socket/threading exploration (see below)
```

## Features

- **Signup / Login** — passwords hashed with jBCrypt (`BCrypt.hashpw` / `BCrypt.checkpw`); raw passwords are never stored or compared directly
- **Search trains** — by source, destination, and travel date; validates that source appears before destination in the train's station order
- **Book a seat** — pick a train from search results, view the seat matrix, select row/column; booking is rejected if the seat is already taken
- **Cancel a booking** — frees the seat back on the train and removes the ticket from the user's booking list
- **Fetch bookings** — view all tickets booked by the logged-in user

## Setup & Run Locally

### Prerequisites
- JDK 17+
- Gradle (or use the included `gradlew`)

### Run

```
gradlew bootRun
```

Or run `BookingServiceApplication.java` directly from your IDE — since `@SpringBootApplication` is commented out, this just executes `main()` as a regular Java program and starts the console menu.

No database setup or environment variables needed — `local_Db/users.json` and `local_Db/train.json` are created automatically on first run if they don't already exist.

## Known Issues / Engineering Notes

Honest, real talking points from building and reviewing this project:

- **`Ticket` embeds a full `Train` object snapshot** instead of just a `trainId` reference. Since the train's seat matrix changes after every booking/cancellation, a ticket booked earlier can end up holding **stale train data** (e.g. an outdated seat layout) — verified this mismatch directly in `users.json`. The correct fix would be storing only `trainId` on the ticket and looking up the live `Train` object via `TrainService` when needed.
- **Inconsistent `userId` formats** — some test users in `users.json` have manually-entered IDs like `user-001`, while users created through signup get a proper `UUID.randomUUID()`. Not a functional bug today since both are unique strings, but inconsistent and would cause confusion at scale.
- **`cancelBooking` (menu option 6) lacks input validation that `bookTrainSeat` (option 5) has.** Option 5 wraps row/column input in a validated loop with `NumberFormatException` handling. Option 6's selection (`Integer.parseInt(scanner.next())`) has no such guard — entering non-numeric input crashes the app instead of showing an error and re-prompting.
- **Potential race condition on concurrent seat booking.** `bookTrainSeat` reads the seat matrix, checks if it's free, then writes — with no synchronization. In a genuinely concurrent (multi-client) scenario, two bookings could both pass the "is seat free" check before either writes, double-booking the same seat. Not exploitable in the current single-user console flow, but would need to be addressed (e.g. synchronized block, or a proper DB transaction) before this could support real concurrent users.
- **`IrctcServer` (see Networking section below) has an unhandled `SocketTimeoutException`.** `socket.accept()` sits inside a `while(true)` loop with a 20-second timeout and no try/catch around it — when no client connects in time, the exception propagates up, `main()`'s catch block logs it, and the server loop exits entirely (the server just stops, rather than continuing to listen). Sockets and streams (`acceptedConnection`, `toClient`, `fromClient`) are also never explicitly closed, which is a resource leak over repeated connections.

## Networking / Multithreading Exploration (`com.irctc.network`)

This package is **additional exploration built alongside IRCTC, not a connected IRCTC feature** — none of these classes import or call into `UserBookingService`, `Train`, or `User`. It was written separately to understand socket programming and multithreading fundamentals:

| Class | Purpose |
|---|---|
| `IrctcServer` / `IrctcClient` | Single-threaded, blocking server — handles **one client connection at a time** via `ServerSocket.accept()` |
| `IrctcMultiTreadServer` / `IrctcMultiTreadClient` | Multithreaded server — spawns a **new thread per connection** using a `Consumer<Socket>` functional interface |

**Tested directly:** sent 100 concurrent client connections to both servers. The multithreaded server successfully handled all 100. The single-threaded server crashed under the same load with a `SocketTimeoutException` (see the bug noted above) — a clear, hands-on demonstration of why thread-per-connection (or a thread pool) matters for handling concurrent clients.

If IRCTC were ever turned into a real client-server application supporting simultaneous bookings, this multithreaded pattern is the foundation that would be needed.

## Author

**Shruti Patil**
GitHub: [shrutipatil99](https://github.com/shrutipatil99)
