# 🐾 PetCare – Pet Clinic Management System

A desktop Veterinary Clinic Management System built with **Java 21**, **JavaFX**, **MySQL**, and **JDBC** following modern **Model-View-Controller (MVC)** architecture.

---

## 🌟 Key Highlights & Capabilities

- **Modern Desktop UI**: Sleek interface inspired by modern veterinary dashboards, with responsive layouts, CSS card designs, and charts.
- **Role-Based Access Control (RBAC)**: Custom views and permissions for **Admin**, **Veterinarian**, **Receptionist**, and **Pet Owner**.
- **Comprehensive Clinic Modules**:
  1. 📊 **Dashboard Overview**: KPI cards, Appointment Status Distribution Chart, 6-Month Revenue Trend, and Today's Schedule.
  2. 🐶 **Pet & Owner Management**: Dual-tab directory for pets and owners with search, microchip identification, breeding details, and medical alerts.
  3. 📅 **Appointment Management**: Smart booking system with **conflict detection** (prevents double-booking doctors), time slots, and status workflows (*Scheduled*, *In Progress*, *Completed*, *Cancelled*).
  4. 🩺 **Veterinarian Staff Directory**: Doctor specializations, licensing, consultation rates, and duty days.
  5. 📋 **Clinical Records & Prescriptions**: Record visits, diagnoses, treatments, and attach prescription items.
  6. 💉 **Vaccination Tracker**: Track given vaccines, batch numbers, and upcoming booster deadlines with automatic overdue alerts.
  7. 💊 **Pharmacy & Inventory Management**: Stock tracking, minimum stock warning alerts, expiry dates, and quick stock restock (+/-).
  8. 💳 **Billing & Invoices**: Generate itemized clinic invoices, compute taxes and discounts, collect payments (Cash, Card, Digital), and view printable receipts.
  9. 📈 **Reports & Executive Analytics**: Executive summaries, billing audits, appointment utilization, and patient demographic breakdowns.

---

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| **Java 21 (LTS)** | Core programming language |
| **JavaFX 21** | Modern graphical user interface |
| **MySQL 8.0+ / MariaDB** | Relational database management |
| **JDBC + HikariCP** | High-performance connection pooling & database connectivity |
| **BCrypt** | Secure password hashing (`jbcrypt`) |
| **Maven** | Build and dependency management |

---

## 📁 Project Architecture & Structure

```
PetCare/
├── pom.xml                                    # Maven configuration & dependencies
├── database/
│   └── petcare_db.sql                         # Complete database schema & seed data
└── src/
    └── main/
        ├── java/com/petcare/
        │   ├── Main.java                      # Launcher entry point
        │   ├── MainApp.java                   # JavaFX Application
        │   ├── config/
        │   │   ├── DatabaseConnection.java    # HikariCP connection pool & JDBC fallback
        │   │   └── SessionManager.java        # User session & RBAC permissions
        │   ├── model/                         # MVC Data Models
        │   │   ├── Role.java
        │   │   ├── User.java
        │   │   ├── PetOwner.java
        │   │   ├── Veterinarian.java
        │   │   ├── Pet.java
        │   │   ├── Appointment.java
        │   │   ├── MedicalRecord.java
        │   │   ├── Vaccination.java
        │   │   ├── Medicine.java
        │   │   ├── Prescription.java
        │   │   ├── Invoice.java
        │   │   ├── Payment.java
        │   │   └── DashboardStats.java
        │   ├── dao/                           # Data Access Objects (JDBC)
        │   │   ├── UserDAO.java
        │   │   ├── PetOwnerDAO.java
        │   │   ├── PetDAO.java
        │   │   ├── VeterinarianDAO.java
        │   │   ├── AppointmentDAO.java
        │   │   ├── MedicalRecordDAO.java
        │   │   ├── VaccinationDAO.java
        │   │   ├── MedicineDAO.java
        │   │   ├── InvoiceDAO.java
        │   │   └── DashboardDAO.java
        │   ├── controller/                    # MVC Controllers
        │   │   ├── LoginController.java
        │   │   ├── MainLayoutController.java
        │   │   ├── DashboardController.java
        │   │   ├── PetController.java
        │   │   ├── AppointmentController.java
        │   │   ├── VeterinarianController.java
        │   │   ├── MedicalRecordController.java
        │   │   ├── InventoryController.java
        │   │   ├── BillingController.java
        │   │   └── ReportsController.java
        │   └── util/
        │       ├── PasswordUtil.java
        │       ├── AlertUtil.java
        │       └── DateUtil.java
        └── resources/
            ├── application.properties         # Database connection settings
            ├── css/
            │   ├── style.css                  # Main theme stylesheets
            │   └── login.css                  # Login styling
            └── com/petcare/view/              # FXML Views
                ├── Login.fxml
                ├── MainLayout.fxml
                ├── Dashboard.fxml
                ├── Pets.fxml
                ├── Appointments.fxml
                ├── Veterinarians.fxml
                ├── MedicalRecords.fxml
                ├── Inventory.fxml
                ├── Billing.fxml
                └── Reports.fxml
```

---

## 🗄️ Database Setup Instructions

1. Start your **MySQL** server (e.g. via **XAMPP**, **WAMP**, or standalone MySQL service).
2. Open your MySQL client (phpMyAdmin, MySQL Workbench, or CLI).
3. Import the provided schema:
   ```bash
   mysql -u root -p < database/petcare_db.sql
   ```
4. Update `src/main/resources/application.properties` if your MySQL username or password differs:
   ```properties
   db.url=jdbc:mysql://localhost:3306/petcare_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=root
   db.password=your_password
   ```

---

## 🔐 Demo User Credentials

Quick demo accounts are pre-configured on the login screen for instant testing:

| Role | Username | Password | Access Privileges |
|---|---|---|---|
| **Admin** | `admin` | `admin123` | Full system access across all 8 modules & user controls |
| **Veterinarian** | `dr.smith` | `vet123` | Clinical records, appointments, prescriptions, vaccinations, inventory |
| **Receptionist** | `sarah.recep` | `recep123` | Patient registrations, appointments, billing, payments |
| **Pet Owner** | `john.owner` | `owner123` | View registered pets, appointments, and medical histories |

---

## 🚀 How to Run the Application

### Option 1: Using Maven
Run the JavaFX application directly from terminal or command prompt:
```bash
mvn clean compile javafx:run
```

### Option 2: Using IntelliJ IDEA or Eclipse / NetBeans
1. Open the project folder `PetCare` as a **Maven project**.
2. Wait for dependencies to download.
3. Run `com.petcare.Main`.
