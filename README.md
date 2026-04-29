# Budget Buddi

## Project Overview

**Budget Buddi** is an Android-based personal finance application designed to help users manage spending, organize expenses, and monitor financial habits.

The app allows users to:
- Record expenses
- Organize spending using categories
- Set monthly budget goals
- View and analyze expenses over time

### Tech Stack
- Kotlin + Jetpack Compose
- RoomDB
- MVVM Architecture

---

## Core Features

### Authentication
- Username/password login
- Session persistence
- Password reset via token system

---

### Category Management
- Default categories preloaded
- Create, edit, delete categories
- Subcategory support
- Emoji/Icon selection
- Custom color picker
- Dynamic search filtering

---

### Expense Management
- Add expenses with:
  - Date
  - Description
  - Category
  - Optional photo
- Camera + gallery support
- Image stored as file path

---

### Expense List
- View all expenses
- Filter by date range
- Card-based UI
- Photo thumbnails
- Full image viewer
- Delete with confirmation

---

### Budget Goals
- Set monthly min/max spending
- View category totals
- Compare spending vs goals

---

## Not Included In This Part
- Charts/graphs
- Income tracking
- Recurring transactions
- Google Sign-In
- Gamification

---

## Architecture

The app follows **MVVM architecture**:

- **Model:** Room Database
- **View:** Jetpack Compose UI
- **ViewModel:** State & logic

Additional structure:
- Repository layer
- Session management
- Modular feature branches

---

## Changelog (Part 1 to Part 2)

---

### Authentication

#### Password Reset Feature

- **What:** Added password reset functionality  
- **How:** Implemented a token-based reset system where a random token is generated and used to update the password  
- **Why:** Allows users to recover access without creating a new account  

---

### Categories

#### 1. Delete Screen → Edit/Delete Screen

- **What:** Converted delete-only screen into a combined edit and delete screen  
- **How:** Added editable input fields alongside delete functionality in the same screen  
- **Why:** Prevents unnecessary deletion when users only want to modify existing categories  

---

#### 2. Color Picker Feature

- **What:** Added color selection for category visuals  
- **How:** Implemented a color wheel allowing users to choose an outline color for icons/emojis  
- **Why:** Improves customization and helps visually distinguish categories  

---

#### 3. Search Bar Repositioning

- **What:** Moved search bar from bottom to top  
- **How:** Updated layout structure in the categories screen  
- **Why:** Improves usability and aligns with standard UI practices  

---

### Expenses

#### Camera Integration

- **What:** Added camera support for expense images  
- **How:** Integrated device camera API alongside gallery selection  
- **Why:** Allows faster and more convenient expense recording  

---

### Expense List

#### 1. Photo Thumbnails

- **What:** Replaced placeholder icons with real images  
- **How:** Loaded images from stored file paths into thumbnail views  
- **Why:** Improves visual clarity and user experience  

---

#### 2. Card-Based Layout

- **What:** Converted plain list into card-based UI  
- **How:** Used elevated Compose Card components with spacing and rounded corners  
- **Why:** Improves readability and visual hierarchy  

---

#### 3. Enhanced Visual Hierarchy

- **What:** Improved layout of expense information  
- **How:**  
  - Category name emphasized  
  - Date shown as secondary text  
  - Amount highlighted in green  

- **Why:** Makes key information easier to read quickly  

---

#### 4. Interactive Features

- **What:** Added interaction to expense items  
- **How:**  
  - Click card → show details  
  - Click image → open full-screen viewer  
  - Added delete button with confirmation  

- **Why:** Improves usability and navigation  

---

### Budget Goals

#### 1. Set Budget Goal Screen

- **What:** Redesigned budget goal input screen  
- **How:**  
  - Added brand logo  
  - Converted month display into styled card  
  - Increased font sizes  
  - Added currency prefix  
  - Replaced borders with dividers  

- **Why:** Improves readability and provides clearer input structure  

---

#### 2. Category Totals Screen

- **What:** Redesigned category totals display  
- **How:**  
  - Converted layout into card-based UI  
  - Improved date selector with structured design  
  - Added visual grouping of data  

- **Why:** Enhances clarity and makes financial data easier to interpret  

---

## Future Improvements
- Graphs and analytics
- Income tracking
- Cloud sync & Firebase addition
- Advanced budgeting tools
- Gamification features

### Project Members
- Kaeden Samsunder - ST10313111
- Raheel Singh - ST10321249
- Avikar Maharaj - ST10325729
- Akeev Sivai - ST10312856
- Mohamed Shaheer Joosab - ST10108240
