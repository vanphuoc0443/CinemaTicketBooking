# 🎬 Cinema Ticket Booking System

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-00758F?style=for-the-badge&logo=mysql&logoColor=white)
![HikariCP](https://img.shields.io/badge/HikariCP-5.1.0-green?style=for-the-badge)

A modern, responsive desktop application for booking cinema tickets, built with **JavaFX** and **MySQL**.

## ✨ Features

### 👤 User Experience
- **🔐 Secure Authentication**: Login and Registration system with password hashing.
- **🎥 Movie Gallery**: Browse currently showing movies with posters and details.
- **📅 Showtime Selection**: Pick your preferred date and time slot.
- **💺 Interactive Seat Map**: 
  - Visual seat selection grid.
  - Real-time status (Available, Booked, Selected).
  - Dynamic pricing calculation.
- **💳 Payment & Booking**: Review summary and simulate payment.
- **📜 History**: View your past bookings.

### 🛠️ Technical Highlights
- **Architecture**: MVC pattern with clear separation of concerns.
- **Database**: 
  - MySQL for robust data storage.
  - **HikariCP** for high-performance connection pooling.
- **Configuration**: Flexible config via `config.properties` or Environment Variables.
- **UI/UX**: Custom CSS styling for a dark-themed, cinematic experience.

## 🚀 Getting Started

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Maven 3.8+
- MySQL Server 8.0+

### ⚙️ Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/vanphuoc0443/CinemaTicketBooking.git
   cd CinemaTicketBooking
   ```

2. **Configure Database**
   - Import the database schema (if provided in `sql/` or docs).
   - Rename `src/main/resources/config.properties.example` to `config.properties`.
   - Update your database credentials in `config.properties`.

3. **Build the Project**
   ```bash
   mvn clean install
   ```

4. **Run the Application**
   ```bash
   mvn javafx:run
   ```

## 📂 Project Structure

```
src/main/java/
├── app/            # Application entry point
├── controller/     # Business logic controllers
├── dao/            # Data Access Objects (DB integration)
├── fxcontroller/   # JavaFX UI Controllers
├── model/          # Data Models
└── util/           # Utilities (DB Connection, Session, etc.)

src/main/resources/
├── css/            # Stylesheets
├── image/          # Assets
└── ui/view/        # FXML Layouts
```
---
*Built with ❤️ by vanphuoc0443 and minh656512-debug*


