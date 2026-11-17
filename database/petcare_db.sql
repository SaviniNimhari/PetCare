-- PetCare Pet Clinic Management System Database Schema
-- Compatible with MySQL 8.0+ / MariaDB 10.4+

CREATE DATABASE IF NOT EXISTS `petcare_db` 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `petcare_db`;

-- 1. Users Table (System Authentication & Roles)
CREATE TABLE IF NOT EXISTS `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `phone` VARCHAR(20),
    `role` ENUM('ADMIN', 'VETERINARIAN', 'RECEPTIONIST', 'PET_OWNER') NOT NULL DEFAULT 'RECEPTIONIST',
    `status` ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Pet Owners Table
CREATE TABLE IF NOT EXISTS `pet_owners` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NULL,
    `first_name` VARCHAR(50) NOT NULL,
    `last_name` VARCHAR(50) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `phone` VARCHAR(20) NOT NULL,
    `address` VARCHAR(255),
    `city` VARCHAR(50),
    `emergency_contact` VARCHAR(50),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 3. Veterinarians Table
CREATE TABLE IF NOT EXISTS `veterinarians` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NULL,
    `first_name` VARCHAR(50) NOT NULL,
    `last_name` VARCHAR(50) NOT NULL,
    `specialization` VARCHAR(100) NOT NULL DEFAULT 'General Veterinary',
    `license_number` VARCHAR(50) NOT NULL UNIQUE,
    `phone` VARCHAR(20) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `available_days` VARCHAR(100) DEFAULT 'Monday - Friday',
    `consultation_fee` DECIMAL(10,2) NOT NULL DEFAULT 50.00,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 4. Pets Table
CREATE TABLE IF NOT EXISTS `pets` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `owner_id` INT NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `species` VARCHAR(50) NOT NULL,
    `breed` VARCHAR(50),
    `gender` ENUM('MALE', 'FEMALE') NOT NULL DEFAULT 'MALE',
    `date_of_birth` DATE,
    `weight_kg` DECIMAL(5,2),
    `color` VARCHAR(50),
    `microchip_id` VARCHAR(50) UNIQUE,
    `photo_url` VARCHAR(255),
    `allergies_or_notes` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`owner_id`) REFERENCES `pet_owners`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Appointments Table
CREATE TABLE IF NOT EXISTS `appointments` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `pet_id` INT NOT NULL,
    `owner_id` INT NOT NULL,
    `vet_id` INT NOT NULL,
    `appointment_date` DATE NOT NULL,
    `appointment_time` TIME NOT NULL,
    `reason` VARCHAR(255) NOT NULL,
    `status` ENUM('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    `notes` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`pet_id`) REFERENCES `pets`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`owner_id`) REFERENCES `pet_owners`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`vet_id`) REFERENCES `veterinarians`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 6. Medical Records Table
CREATE TABLE IF NOT EXISTS `medical_records` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `pet_id` INT NOT NULL,
    `vet_id` INT NOT NULL,
    `appointment_id` INT NULL,
    `visit_date` DATE NOT NULL,
    `symptoms` TEXT,
    `diagnosis` TEXT NOT NULL,
    `treatment` TEXT NOT NULL,
    `notes` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`pet_id`) REFERENCES `pets`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`vet_id`) REFERENCES `veterinarians`(`id`) ON DELETE RESTRICT,
    FOREIGN KEY (`appointment_id`) REFERENCES `appointments`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 7. Vaccinations Table
CREATE TABLE IF NOT EXISTS `vaccinations` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `pet_id` INT NOT NULL,
    `vaccine_name` VARCHAR(100) NOT NULL,
    `administered_date` DATE NOT NULL,
    `next_due_date` DATE NULL,
    `batch_number` VARCHAR(50),
    `administered_by_vet_id` INT NULL,
    `status` ENUM('GIVEN', 'UPCOMING', 'OVERDUE') NOT NULL DEFAULT 'GIVEN',
    `notes` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`pet_id`) REFERENCES `pets`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`administered_by_vet_id`) REFERENCES `veterinarians`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 8. Medicines / Inventory Table
CREATE TABLE IF NOT EXISTS `medicines` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `category` VARCHAR(50) NOT NULL,
    `dosage_form` VARCHAR(50) NOT NULL,
    `strength` VARCHAR(50),
    `quantity_in_stock` INT NOT NULL DEFAULT 0,
    `min_stock_level` INT NOT NULL DEFAULT 10,
    `unit_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `expiry_date` DATE NOT NULL,
    `manufacturer` VARCHAR(100),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 9. Prescriptions Table
CREATE TABLE IF NOT EXISTS `prescriptions` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `medical_record_id` INT NOT NULL,
    `medicine_id` INT NOT NULL,
    `dosage` VARCHAR(100) NOT NULL,
    `frequency` VARCHAR(100) NOT NULL,
    `duration_days` INT NOT NULL DEFAULT 7,
    `instructions` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`medical_record_id`) REFERENCES `medical_records`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`medicine_id`) REFERENCES `medicines`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 10. Invoices Table
CREATE TABLE IF NOT EXISTS `invoices` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `invoice_number` VARCHAR(30) NOT NULL UNIQUE,
    `appointment_id` INT NULL,
    `pet_id` INT NOT NULL,
    `owner_id` INT NOT NULL,
    `invoice_date` DATE NOT NULL,
    `consultation_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `treatment_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `medicine_total` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `tax_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `status` ENUM('PENDING', 'PAID', 'PARTIALLY_PAID', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`appointment_id`) REFERENCES `appointments`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`pet_id`) REFERENCES `pets`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`owner_id`) REFERENCES `pet_owners`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 11. Payments Table
CREATE TABLE IF NOT EXISTS `payments` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `invoice_id` INT NOT NULL,
    `payment_date` DATE NOT NULL,
    `amount` DECIMAL(10,2) NOT NULL,
    `payment_method` ENUM('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER', 'DIGITAL_WALLET') NOT NULL DEFAULT 'CASH',
    `transaction_reference` VARCHAR(100),
    `notes` TEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`invoice_id`) REFERENCES `invoices`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ===================================================
-- SEED DATA FOR QUICK START & TESTING
-- ===================================================

-- 1. Initial Users (Passwords stored plain or hashed as demo: admin123, vet123, recep123, owner123)
INSERT INTO `users` (`username`, `password`, `full_name`, `email`, `phone`, `role`, `status`) VALUES
('admin', 'admin123', 'System Administrator', 'admin@petcare.com', '+1-555-0100', 'ADMIN', 'ACTIVE'),
('dr.smith', 'vet123', 'Dr. Alexander Smith, DVM', 'asmith@petcare.com', '+1-555-0101', 'VETERINARIAN', 'ACTIVE'),
('dr.watson', 'vet123', 'Dr. Clara Watson, DVM', 'cwatson@petcare.com', '+1-555-0102', 'VETERINARIAN', 'ACTIVE'),
('sarah.recep', 'recep123', 'Sarah Jenkins', 'sjenkins@petcare.com', '+1-555-0103', 'RECEPTIONIST', 'ACTIVE'),
('john.owner', 'owner123', 'Johnathan Doe', 'john.doe@example.com', '+1-555-0104', 'PET_OWNER', 'ACTIVE')
ON DUPLICATE KEY UPDATE `username`=`username`;

-- 2. Initial Veterinarians
INSERT INTO `veterinarians` (`user_id`, `first_name`, `last_name`, `specialization`, `license_number`, `phone`, `email`, `available_days`, `consultation_fee`) VALUES
(2, 'Alexander', 'Smith', 'Small Animal Surgery & Canine Medicine', 'VET-NY-98231', '+1-555-0101', 'asmith@petcare.com', 'Mon, Wed, Fri', 65.00),
(3, 'Clara', 'Watson', 'Feline Medicine & Dental Care', 'VET-NY-98445', '+1-555-0102', 'cwatson@petcare.com', 'Tue, Thu, Sat', 60.00)
ON DUPLICATE KEY UPDATE `license_number`=`license_number`;

-- 3. Initial Pet Owners
INSERT INTO `pet_owners` (`user_id`, `first_name`, `last_name`, `email`, `phone`, `address`, `city`, `emergency_contact`) VALUES
(5, 'Johnathan', 'Doe', 'john.doe@example.com', '+1-555-0104', '742 Evergreen Terrace', 'Springfield', '+1-555-0199'),
(NULL, 'Emily', 'Clark', 'emily.clark@example.com', '+1-555-0215', '12 Maple Avenue', 'Riverdale', '+1-555-0299'),
(NULL, 'Marcus', 'Vance', 'marcus.v@example.com', '+1-555-0344', '88 Oak Ridge Way', 'Greendale', '+1-555-0399')
ON DUPLICATE KEY UPDATE `email`=`email`;

-- 4. Initial Pets
INSERT INTO `pets` (`owner_id`, `name`, `species`, `breed`, `gender`, `date_of_birth`, `weight_kg`, `color`, `microchip_id`, `allergies_or_notes`) VALUES
(1, 'Milo', 'Dog', 'Golden Retriever', 'MALE', '2022-04-15', 31.50, 'Golden / Honey', 'MC-981001-CAN', 'Allergic to chicken proteins'),
(1, 'Luna', 'Cat', 'Siamese', 'FEMALE', '2023-01-20', 4.20, 'Cream & Chocolate Point', 'MC-981002-FEL', 'Mild asthma in spring'),
(2, 'Barnaby', 'Dog', 'French Bulldog', 'MALE', '2021-08-10', 12.80, 'Brindle', 'MC-981003-CAN', 'Brachycephalic airway syndrome watch'),
(3, 'Oliver', 'Cat', 'British Shorthair', 'MALE', '2022-11-05', 5.60, 'Blue Grey', 'MC-981004-FEL', 'Regular ear checkups needed')
ON DUPLICATE KEY UPDATE `microchip_id`=`microchip_id`;

-- 5. Initial Medicines / Inventory
INSERT INTO `medicines` (`name`, `category`, `dosage_form`, `strength`, `quantity_in_stock`, `min_stock_level`, `unit_price`, `expiry_date`, `manufacturer`) VALUES
('Amoxicillin Clavulanate', 'Antibiotic', 'Tablet', '250mg', 120, 25, 1.85, '2027-12-31', 'Zoetis'),
('Meloxicam (Metacam)', 'Anti-inflammatory / NSAID', 'Oral Suspension', '1.5mg/ml', 45, 10, 24.50, '2027-08-15', 'Boehringer Ingelheim'),
('NexGard Spectra', 'Parasiticide / Flea & Tick', 'Chewable', 'M (7.5-15kg)', 80, 15, 18.00, '2028-02-28', 'Boehringer Ingelheim'),
('Rabies Vaccine (Defensor 3)', 'Vaccine', 'Injectable', '1 Dose', 50, 20, 15.00, '2027-10-30', 'Zoetis'),
('DHPP Core Vaccine', 'Vaccine', 'Injectable', '1 Dose', 60, 20, 17.50, '2027-11-20', 'Merck Animal Health'),
('Prednisolone', 'Corticosteroid', 'Tablet', '5mg', 200, 30, 0.45, '2027-06-30', 'Dechra'),
('Ear Cleansing Solution (EpiOtic)', 'Dermatology / Otic', 'Liquid Rinse', '125ml', 8, 10, 14.20, '2027-05-15', 'Virbac')
ON DUPLICATE KEY UPDATE `name`=`name`;

-- 6. Initial Appointments
INSERT INTO `appointments` (`pet_id`, `owner_id`, `vet_id`, `appointment_date`, `appointment_time`, `reason`, `status`, `notes`) VALUES
(1, 1, 1, CURDATE(), '09:30:00', 'Annual Health Checkup & Booster Vaccination', 'SCHEDULED', 'Owner requested weight monitoring check'),
(2, 1, 2, CURDATE(), '11:00:00', 'Sneezing and Mild Lethargy', 'IN_PROGRESS', 'Check respiratory and lung sounds'),
(3, 2, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', 'Ear scratching and head shaking', 'SCHEDULED', 'Suspected otitis externa'),
(4, 3, 2, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '16:15:00', 'Routine Dental Cleaning Evaluation', 'SCHEDULED', 'Check tartar buildup')
ON DUPLICATE KEY UPDATE `id`=`id`;

-- 7. Initial Invoices
INSERT INTO `invoices` (`invoice_number`, `appointment_id`, `pet_id`, `owner_id`, `invoice_date`, `consultation_fee`, `treatment_fee`, `medicine_total`, `tax_amount`, `discount_amount`, `total_amount`, `status`) VALUES
('INV-2026-0001', 1, 1, 1, CURDATE(), 65.00, 25.00, 32.50, 9.75, 0.00, 132.25, 'PAID')
ON DUPLICATE KEY UPDATE `invoice_number`=`invoice_number`;

-- 8. Initial Payments
INSERT INTO `payments` (`invoice_id`, `payment_date`, `amount`, `payment_method`, `transaction_reference`, `notes`) VALUES
(1, CURDATE(), 132.25, 'CREDIT_CARD', 'TXN-984412-AUTH', 'Full payment collected at front desk')
ON DUPLICATE KEY UPDATE `id`=`id`;
