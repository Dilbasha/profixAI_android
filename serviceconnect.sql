-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Jan 30, 2026 at 08:56 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `serviceconnect`
--

-- --------------------------------------------------------

--
-- Table structure for table `addresses`
--

CREATE TABLE `addresses` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `label` varchar(50) DEFAULT 'Home',
  `address` text NOT NULL,
  `city` varchar(100) DEFAULT NULL,
  `pincode` varchar(10) DEFAULT NULL,
  `is_default` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `admins`
--

CREATE TABLE `admins` (
  `id` int(11) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admins`
--

INSERT INTO `admins` (`id`, `email`, `password`, `full_name`, `created_at`) VALUES
(1, 'a@gmail.com', 'admin', 'Admin User', '2025-12-31 19:29:33');

-- --------------------------------------------------------

--
-- Table structure for table `bookings`
--

CREATE TABLE `bookings` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `provider_id` int(11) NOT NULL,
  `service_id` int(11) NOT NULL,
  `booking_date` date NOT NULL,
  `booking_time` time NOT NULL,
  `address` text NOT NULL,
  `city` varchar(100) DEFAULT NULL,
  `pincode` varchar(10) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `estimated_hours` int(11) DEFAULT 1,
  `total_amount` decimal(10,2) NOT NULL,
  `status` enum('pending','accepted','in_progress','completed','cancelled') DEFAULT 'pending',
  `payment_status` enum('pending','paid','refunded') DEFAULT 'pending',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bookings`
--

INSERT INTO `bookings` (`id`, `user_id`, `provider_id`, `service_id`, `booking_date`, `booking_time`, `address`, `city`, `pincode`, `description`, `estimated_hours`, `total_amount`, `status`, `payment_status`, `created_at`, `updated_at`) VALUES
(7, 2, 3, 6, '2026-01-02', '20:12:00', 'Chennai railwaystation', 'Chennai', '600001', '', 2, 1000.00, 'completed', 'pending', '2026-01-01 14:43:24', '2026-01-01 14:45:11'),
(14, 13, 11, 1, '2026-01-31', '02:02:00', 'dkndkckklm', '', '521463', 'hml', 2, 247512.00, 'pending', 'pending', '2026-01-30 05:32:59', '2026-01-30 05:32:59'),
(15, 13, 4, 3, '2026-01-31', '04:25:00', 'djsoa thi dosla', '', '659543', '', 2, 1000.00, 'pending', 'pending', '2026-01-30 05:34:13', '2026-01-30 05:34:13'),
(16, 13, 15, 1, '2026-01-30', '11:09:00', 'chennai', 'chennai', '602105', 'jsjs', 2, 2000.00, 'accepted', 'pending', '2026-01-30 05:39:39', '2026-01-30 05:40:33');

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `provider_id` int(11) DEFAULT NULL,
  `type` varchar(50) NOT NULL,
  `title` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `related_booking_id` int(11) DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`id`, `user_id`, `provider_id`, `type`, `title`, `message`, `related_booking_id`, `is_read`, `created_at`) VALUES
(1, NULL, 1, 'booking_created', 'New Booking Request', 'You have a new booking request from w for Cleaner on 2026-01-15', NULL, 0, '2026-01-05 18:07:34'),
(6, NULL, 1, 'job_completed', 'Job Completed', 'You have successfully completed the Cleaner job for w. Payment of ₹200.00 is due.', NULL, 0, '2026-01-05 18:15:54'),
(7, NULL, 11, 'booking_created', 'New Booking Request', 'You have a new booking request from Ranbeer for Cleaner on 2026-01-31', 14, 0, '2026-01-30 05:32:59'),
(8, 13, NULL, 'booking_submitted', 'Booking Submitted', 'Your booking for Cleaner with tt has been submitted. Waiting for confirmation.', 14, 1, '2026-01-30 05:32:59'),
(9, NULL, 4, 'booking_created', 'New Booking Request', 'You have a new booking request from Ranbeer for Painter on 2026-01-31', 15, 0, '2026-01-30 05:34:13'),
(10, 13, NULL, 'booking_submitted', 'Booking Submitted', 'Your booking for Painter with t has been submitted. Waiting for confirmation.', 15, 1, '2026-01-30 05:34:13'),
(11, NULL, 15, 'booking_created', 'New Booking Request', 'You have a new booking request from Ranbeer for Cleaner on 2026-01-30', 16, 0, '2026-01-30 05:39:39'),
(12, 13, NULL, 'booking_submitted', 'Booking Submitted', 'Your booking for Cleaner with dillu has been submitted. Waiting for confirmation.', 16, 0, '2026-01-30 05:39:39'),
(13, 13, NULL, 'booking_accepted', 'Booking Confirmed', 'Your booking for Cleaner with dillu has been confirmed.', 16, 0, '2026-01-30 05:40:33');

-- --------------------------------------------------------

--
-- Table structure for table `providers`
--

CREATE TABLE `providers` (
  `id` int(11) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(15) NOT NULL,
  `password` varchar(255) NOT NULL,
  `service_id` int(11) NOT NULL,
  `experience_years` int(11) DEFAULT 0,
  `hourly_rate` decimal(10,2) NOT NULL,
  `description` text DEFAULT NULL,
  `address` text DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `pincode` varchar(10) DEFAULT NULL,
  `profile_image` varchar(255) DEFAULT NULL,
  `aadhaar` varchar(12) DEFAULT NULL,
  `is_verified` tinyint(1) DEFAULT 0,
  `verification_status` enum('pending','verified','rejected') DEFAULT 'pending',
  `rating` decimal(2,1) DEFAULT 0.0,
  `total_reviews` int(11) DEFAULT 0,
  `total_jobs` int(11) DEFAULT 0,
  `is_available` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `rejection_reason` text DEFAULT NULL,
  `rejected_at` datetime DEFAULT NULL,
  `honor_score` decimal(4,1) DEFAULT 0.0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `providers`
--

INSERT INTO `providers` (`id`, `full_name`, `email`, `phone`, `password`, `service_id`, `experience_years`, `hourly_rate`, `description`, `address`, `city`, `pincode`, `profile_image`, `aadhaar`, `is_verified`, `verification_status`, `rating`, `total_reviews`, `total_jobs`, `is_available`, `created_at`, `rejection_reason`, `rejected_at`, `honor_score`) VALUES
(1, 'q', 'q@gmail.com', '123', '123456', 1, 5, 100.00, 'good cleaner.', 'Chennaii', 'Chennai', '523232', 'uploads/providers/provider_1_1767638893.jpg', '123654789052', 1, 'verified', 3.7, 3, 4, 1, '2025-12-31 19:47:25', NULL, NULL, 56.4),
(2, 'qq', 'qq@gmail.com', '1236547890', '123456', 2, 10, 500.00, 'good electrian', 'Chennai', 'Chennai', '123456', NULL, '1234567890', 1, 'verified', 0.0, 0, 0, 1, '2025-12-31 20:27:59', NULL, NULL, 0.0),
(3, 'Basha', 'basha@gmail.com', '9849350706', '123456', 6, 8, 500.00, 'good mechanic', 'Chennai', 'Chennai', '600001', NULL, '123456789870', 1, 'verified', 5.0, 1, 1, 0, '2026-01-01 14:35:16', NULL, NULL, 0.0),
(4, 't', 't@gmail.com', '1236547895', '123456', 3, 8, 500.00, 'ok', 'Hyderabad', 'Hyderabad', '5226689', NULL, '', 1, 'verified', 0.0, 0, 0, 1, '2026-01-04 12:00:31', NULL, NULL, 0.0),
(5, '1', '1@gmail.com', '12547890', '123456', 1, 8, 500.00, 'rtyuo', 'uiop', 'ccb', '6886', 'uploads/providers/provider_5_1767529292.jpg', '756555505066', 1, 'verified', 0.0, 0, 0, 1, '2026-01-04 12:09:27', NULL, NULL, 0.0),
(6, '2', '2@gmail.com', '12365', '123456', 5, 5, 150.00, 'e', 'w', 'v', '6', NULL, '8', 0, 'rejected', 0.0, 0, 0, 1, '2026-01-04 12:26:05', NULL, NULL, 0.0),
(7, 'bhavanesh', 'b@gmail.com', '1236584798', '123456', 1, 6, 500.00, 'Hi', 'Chennai', 'Chennai', '600001', NULL, '123441232143', 1, 'verified', 0.0, 0, 0, 1, '2026-01-04 15:03:14', NULL, NULL, 0.0),
(8, 'kiran', 'k@gmail.com', '1478523695', '123456', 5, 5, 200.00, 'hi', 'Chennai', 'chennai', '60001', NULL, '852396322147', 0, 'rejected', 0.0, 0, 0, 1, '2026-01-04 15:09:04', 'Policy violation', '2026-01-04 20:41:34', 0.0),
(9, 'gjjbhjjk#', 'ghiii', '6666', '000000', 1, 5, 55.00, '', '', '', '', NULL, '', 0, 'rejected', 0.0, 0, 0, 1, '2026-01-28 15:16:58', 'Failed verification', '2026-01-30 09:54:30', 0.0),
(10, 'Pavaneshwar', 'p@gmail.com', '6305630512', '123456', 6, 5, 250.00, 'I am good at modification of bikes and cars', 'chettipedu', 'chennai', '602105', NULL, '', 1, 'verified', 0.0, 0, 0, 1, '2026-01-28 15:27:54', NULL, NULL, 0.0),
(11, 'tt', 'pavaneswar224@gmail.com', '6305248281', 'pavan6', 1, 123, 123756.00, 'no', 'sjjsjs', 'bzbd', '515591', NULL, '', 0, 'verified', 0.0, 0, 0, 1, '2026-01-28 17:39:13', NULL, NULL, 0.0),
(13, 'Rakeshjj', 'r@gmail.co', '7894561238', '000000', 4, 2, 500.00, '', 'chettipedu', 'chennai', '642105', NULL, '', 0, 'rejected', 0.0, 0, 0, 1, '2026-01-30 04:05:00', 'Invalid documents', '2026-01-30 11:11:33', 0.0),
(14, 'pavan', 'pp@gmail.com', '7561049464', '000000', 6, 4, 500.00, '', 'kadiri', 'chennai', '601646', NULL, '', 1, 'verified', 0.0, 0, 0, 1, '2026-01-30 04:23:19', NULL, NULL, 0.0),
(15, 'dillu', 'dd@gmail.com', '7864646766', '000000', 1, 2, 1000.00, 'sbhz', 'chebnai', 'chebnai', '601626', NULL, '', 1, 'verified', 0.0, 0, 0, 1, '2026-01-30 05:37:30', NULL, NULL, 0.0);

-- --------------------------------------------------------

--
-- Table structure for table `provider_availability`
--

CREATE TABLE `provider_availability` (
  `id` int(11) NOT NULL,
  `provider_id` int(11) NOT NULL,
  `date` date NOT NULL,
  `status` enum('available','unavailable','partial') DEFAULT 'available',
  `start_time` time DEFAULT '09:00:00',
  `end_time` time DEFAULT '17:00:00',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `provider_locations`
--

CREATE TABLE `provider_locations` (
  `id` int(11) NOT NULL,
  `provider_id` int(11) NOT NULL,
  `booking_id` int(11) DEFAULT NULL,
  `latitude` decimal(10,8) NOT NULL,
  `longitude` decimal(11,8) NOT NULL,
  `is_sharing` tinyint(1) DEFAULT 0,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `provider_portfolio`
--

CREATE TABLE `provider_portfolio` (
  `id` int(11) NOT NULL,
  `provider_id` int(11) NOT NULL,
  `image_url` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `reviews`
--

CREATE TABLE `reviews` (
  `id` int(11) NOT NULL,
  `booking_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `provider_id` int(11) NOT NULL,
  `rating` int(11) NOT NULL CHECK (`rating` >= 1 and `rating` <= 5),
  `comment` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `services`
--

CREATE TABLE `services` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `icon` varchar(50) NOT NULL,
  `description` text DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `services`
--

INSERT INTO `services` (`id`, `name`, `icon`, `description`, `is_active`, `created_at`) VALUES
(1, 'Cleaner', '🧹', 'Home & office cleaning services', 1, '2025-12-31 19:29:33'),
(2, 'Electrician', '⚡', 'Electrical repairs & installations', 1, '2025-12-31 19:29:33'),
(3, 'Painter', '🎨', 'Interior & exterior painting', 1, '2025-12-31 19:29:33'),
(4, 'Salon', '💇', 'Beauty & grooming services', 1, '2025-12-31 19:29:33'),
(5, 'Carpenter', '🪚', 'Woodwork & furniture', 1, '2025-12-31 19:29:33'),
(6, 'Mechanic', '🔧', 'Vehicle repairs & maintenance', 1, '2025-12-31 19:29:33');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(15) NOT NULL,
  `password` varchar(255) NOT NULL,
  `profile_image` varchar(255) DEFAULT NULL,
  `address` text DEFAULT NULL,
  `city` varchar(100) DEFAULT NULL,
  `pincode` varchar(10) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `full_name`, `email`, `phone`, `password`, `profile_image`, `address`, `city`, `pincode`, `created_at`) VALUES
(2, 'Sarath', 'sarath@gmail.com', '1236547895', '123456', NULL, 'Chennai', 'Chennai', '600001', '2026-01-01 14:41:00'),
(3, 'hari', 'hari@gmail.com', '1236543895', '123456', NULL, 'qwetu', 'duucxezruxu', '06060', '2026-01-04 15:21:28'),
(4, 'Dil@-+(', 'dilfmakmm.com', '781609485899464', '000000', NULL, 'hsjsjks', '', '', '2026-01-28 15:12:17'),
(5, 'dodlehhsj', 'dil@gmaol.com', '1234567891', '123456', NULL, 'chettipedu', '', '', '2026-01-28 16:03:59'),
(6, 'pavan', 'pavaneswar224@gmail.com', '6305240281', 'dillu123', NULL, 'jsjejjdj', 'jdjdj', '959591', '2026-01-28 16:17:54'),
(8, 'satyam', 's@gmail.co', '7806091858', '123456', NULL, 'chennai', 'chennai', '601205', '2026-01-28 17:54:59'),
(10, 'satyam', 'j@gmail.coo', '7819094858', '123456', NULL, 'chennai', 'chenni', '649499', '2026-01-28 17:58:41'),
(12, 'sharukh', 'srk@gmail.co', '6035801766', '000000', NULL, 'hi-tech city,mumbai', 'mumbai', '515591', '2026-01-30 04:08:19'),
(13, 'Ranbeer', 'rk@gmail.com', '7854648796', '000000', NULL, 'Banjara hills , hyderabad', 'Hyderabad', '601634', '2026-01-30 04:15:26');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `addresses`
--
ALTER TABLE `addresses`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `admins`
--
ALTER TABLE `admins`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `bookings`
--
ALTER TABLE `bookings`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `provider_id` (`provider_id`),
  ADD KEY `service_id` (`service_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `related_booking_id` (`related_booking_id`),
  ADD KEY `idx_notifications_user` (`user_id`,`is_read`),
  ADD KEY `idx_notifications_provider` (`provider_id`,`is_read`),
  ADD KEY `idx_notifications_created` (`created_at`);

--
-- Indexes for table `providers`
--
ALTER TABLE `providers`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `service_id` (`service_id`);

--
-- Indexes for table `provider_availability`
--
ALTER TABLE `provider_availability`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_provider_date` (`provider_id`,`date`),
  ADD KEY `idx_availability_provider_date` (`provider_id`,`date`);

--
-- Indexes for table `provider_locations`
--
ALTER TABLE `provider_locations`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_provider_location` (`provider_id`),
  ADD KEY `booking_id` (`booking_id`);

--
-- Indexes for table `provider_portfolio`
--
ALTER TABLE `provider_portfolio`
  ADD PRIMARY KEY (`id`),
  ADD KEY `provider_id` (`provider_id`);

--
-- Indexes for table `reviews`
--
ALTER TABLE `reviews`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_review` (`booking_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `provider_id` (`provider_id`);

--
-- Indexes for table `services`
--
ALTER TABLE `services`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `addresses`
--
ALTER TABLE `addresses`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `admins`
--
ALTER TABLE `admins`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `bookings`
--
ALTER TABLE `bookings`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `providers`
--
ALTER TABLE `providers`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `provider_availability`
--
ALTER TABLE `provider_availability`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `provider_locations`
--
ALTER TABLE `provider_locations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `provider_portfolio`
--
ALTER TABLE `provider_portfolio`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `reviews`
--
ALTER TABLE `reviews`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `services`
--
ALTER TABLE `services`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `addresses`
--
ALTER TABLE `addresses`
  ADD CONSTRAINT `addresses_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `bookings`
--
ALTER TABLE `bookings`
  ADD CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`provider_id`) REFERENCES `providers` (`id`),
  ADD CONSTRAINT `bookings_ibfk_3` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`);

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `notifications_ibfk_2` FOREIGN KEY (`provider_id`) REFERENCES `providers` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `notifications_ibfk_3` FOREIGN KEY (`related_booking_id`) REFERENCES `bookings` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `providers`
--
ALTER TABLE `providers`
  ADD CONSTRAINT `providers_ibfk_1` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`);

--
-- Constraints for table `provider_availability`
--
ALTER TABLE `provider_availability`
  ADD CONSTRAINT `provider_availability_ibfk_1` FOREIGN KEY (`provider_id`) REFERENCES `providers` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `provider_locations`
--
ALTER TABLE `provider_locations`
  ADD CONSTRAINT `provider_locations_ibfk_1` FOREIGN KEY (`provider_id`) REFERENCES `providers` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `provider_locations_ibfk_2` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `provider_portfolio`
--
ALTER TABLE `provider_portfolio`
  ADD CONSTRAINT `provider_portfolio_ibfk_1` FOREIGN KEY (`provider_id`) REFERENCES `providers` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `reviews`
--
ALTER TABLE `reviews`
  ADD CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`),
  ADD CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `reviews_ibfk_3` FOREIGN KEY (`provider_id`) REFERENCES `providers` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
