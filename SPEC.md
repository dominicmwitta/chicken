# Poultry Farm Manager - Specification

## 1. Project Overview

**Project Name:** PoultryFarmManager

**Core Functionality:** A mobile Android application for managing small poultry farm operations, including bird inventory, egg production tracking, feed management, and daily tasks.

## 2. Technology Stack & Choices

- **Framework:** Android Native (Kotlin)
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34
- **Language:** Kotlin 1.9.22
- **Architecture:** MVVM with Clean Architecture
- **Database:** Room 2.6.1 (local SQLite)
- **Dependency Injection:** Hilt 2.50
- **Async:** Kotlin Coroutines + Flow
- **UI:** Jetpack Compose with Material 3
- **Navigation:** Compose Navigation 2.7.6

## 3. Feature List

### 3.1 Dashboard
- Total bird count display
- Today's egg production summary
- Pending tasks count
- Quick action buttons

### 3.2 Bird Inventory
- Add/edit/delete bird batches
- Track breed, quantity, date acquired
- View total birds by breed

### 3.3 Egg Production
- Log daily egg collection
- Record eggs by category (small, medium, large, extra-large)
- View production history

### 3.4 Feed Management
- Track feed inventory
- Log feed consumption
- Low stock alerts

### 3.5 Tasks
- Create daily farm tasks
- Mark tasks complete
- Task due dates

## 4. UI/UX Design Direction

- **Visual Style:** Material Design 3 with clean, agricultural color palette
- **Color Scheme:** 
  - Primary: Green (#2E7D32) - representing growth/farming
  - Secondary: Amber (#FF8F00) - for eggs/accents
  - Background: Light cream/white
- **Layout:** Bottom navigation with 5 tabs (Dashboard, Birds, Eggs, Feed, Tasks)
- **Typography:** Clean, readable sans-serif