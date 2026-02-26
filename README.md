# 🎬 Cinema Ticket Booking System

![Java](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-00758F?style=for-the-badge&logo=mysql&logoColor=white)
![HikariCP](https://img.shields.io/badge/HikariCP-5.1.0-green?style=for-the-badge)

Ứng dụng Desktop giúp quản lý và đặt vé xem phim được phát triển bằng **JavaFX** và **MySQL**, tích hợp API dữ liệu phim từ **OMDb API**.

Hệ thống bao gồm 2 module riêng biệt chia sẻ chung Database:
1. **User App:** Dành cho khách hàng (xem lịch chiếu, chọn ghế, đặt vé).
2. **Admin App:** Dành cho rạp phim (quản lý rạp, fetch data từ OMDb, xếp lịch chiếu).

---

## ✨ Features

### 👤 User App (Khách hàng)
- **Authentication**: Dễ dàng Login / Register với hệ thống mã hóa password an toàn.
- **Movie Booking**: 
  - Phân loại rõ phim **Now Showing** (đang chiếu) và **Coming Soon** (sắp chiếu).
  - Tự động fetch thông tin đầy đủ gồm Director, Genre, Language, IMDb Rating và Plot chi tiết từ OMDb API.
- **Seat Map Interactive**:
  - UI sơ đồ ghế trực quan (A-H, 1-10) với trạng thái Available / Booked / Selected.
  - Tích hợp tính năng Lock ghế tạm thời trong 5 phút khi người dùng chuyển sang bước Checkout.
- **Checkout & History**: Auto-calculate pricing, hóa đơn điện tử và lưu trữ Booking History.

### ⚙️ Admin App (Quản trị)
- **Theater Management**: Thêm / Xóa phòng chiếu (limit: 10). Auto naming phòng chiếu hợp lý.
- **Movie Management (OMDb Integration)**:
  - Search phim qua tiếng Anh → Hệ thống fetch data trực tiếp từ OMDb API (Poster, Director, Rating, etc.).
  - Lưu vào Database nội bộ chỉ với 1 click.
- **Showtime Scheduling**:
  - Giao diện chọn Movie + Theater + Date + Time slots.
  - **Conflict Detection**: Tự động chặn trùng lịch chiếu dựa trên Runtime của phim + 15 phút Setup dọn dẹp.
  - Auto-generate Layout ghế ngồi đi kèm theo từng suất chiếu.
- **Data Integrity / Cascade Delete**: Khi xóa Showtime, toàn bộ data liên quan (Seats, Bookings, Payments) sẽ được clean-up an toàn dựa trên ràng buộc Database.

---

## 🛠 Tech Stack & Architecture
- **UI/UX**: Dark mode theme sang trọng. Layout code chuẩn qua FXML và CSS styling thuần.
- **Architecture**: MVC Pattern (Model - View - Controller) & DAO (Data Access Object) Pattern giúp tách biệt Business Logic và Database.
- **Database**: 
  - Dùng MySQL với Foreign Keys & Cascade operations.
  - Tích hợp **HikariCP** Connection Pooling cho database performance tối ưu.
- **API Integration**: REST HTTP Client gọi đến `https://www.omdbapi.com/`.

---

## 🚀 Setup & Run (Cài đặt & Chạy)

### Prerequisites
- **JDK 17** trở lên.
- **Maven** 3.8+.
- **MySQL Server** 8.0+.

### ⚙️ Database Configuration

1. Mở MySQL và chạy các script sau trong thư mục `database/` để khởi tạo cấu trúc DB:
   ```bash
   mysql -u root -p < database/SQL.sql
   mysql -u root -p < database/migration_theaters.sql
   mysql -u root -p < database/migration_movie_details.sql
   ```
2. Cập nhật thông tin MySQL tại file `src/main/resources/config.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/cinema_booking
   db.user=root
   db.password=mat_khau_cua_ban
   ```

### 💻 Chạy App thông qua Maven

Ví dụ chạy 2 App riêng lẻ thông qua command Maven:

```bash
# Compile project
mvn clean compile

# 1. Khởi chạy User App (Chế độ đặt vé)
mvn exec:java

# 2. Khởi chạy Admin App (Chế độ quản lý)
mvn exec:java -Padmin
```

> **Testing Tip:** Khởi động *Admin App* trước để setup Phòng chiếu, fetch Movies từ OMDb và tạo Showtimes. Sau đó sang *User App* để test luồng đặt vé.

---

## � Project Structure

```text
CinemaTicketBooking/
├── database/            # Database scripts & Migrations
├── src/main/java/
│   ├── app/             # MainApp (User) & AdminApp (Admin) launchers
│   ├── controller/      # Business Logic Controllers
│   ├── dao/             # Data Access Objects (với MySQL & HikariCP)
│   ├── fxcontroller/    # JavaFX UI Controllers
│   ├── model/           # Models (User, Movie, Theater, etc.)
│   ├── service/         # API Services (OMDb API) & Email Service
│   └── util/            # Support utils (ConnectionPool, Session)
├── src/main/resources/
│   ├── css/             # CSS Giao diện (Dark theme)
│   ├── ui/view/         # UI templates (.fxml)
│   └── config.properties# DB/API Configuration
└── pom.xml              # Maven configuration
```

---
*Built with ❤️ by vanphuoc0443 & minh656512-debug*
