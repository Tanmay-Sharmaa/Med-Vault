# MedVault — Secure Health Record System

A Spring Boot + Thymeleaf app with role-based access (Patient/Doctor/Admin), encrypted file storage, OTP login, and basic AI features (report summarizer, chatbot, intent search).

## Tech Stack
Spring Boot 3 · Thymeleaf · Spring Security · JPA (Hibernate) · MySQL 8 · Lombok · Maven  
Extras: Configuration Processor, Validation, Spring Mail, PDFBox (later)

## Features (MVP)
- Patients: upload/view medical history, prescriptions, reports
- Doctors: add diagnoses & prescriptions, view patient history
- Admin: user & audit log management
- Security: role-based access, AES-GCM encrypted files, OTP login (email/SMS)
- AI: report text summarizer (no handwriting), chatbot (safe guidance), intent-based search
- PDF export (diagnosis/prescription/summary)

## Prerequisites
- JDK 22 (or 21 LTS)
- Maven 3.9+
- MySQL 8 (DB: `medvault`)
- Git

## Quick Start (Dev)
1. Create DB & user:
   ```sql
   CREATE DATABASE medvault CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'medvault_user'@'%' IDENTIFIED BY 'StrongPwd123!';
   GRANT ALL PRIVILEGES ON medvault.* TO 'medvault_user'@'%';
   FLUSH PRIVILEGES;
