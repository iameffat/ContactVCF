<div align="center">
  <img src="https://github.com/iameffat/contactvcf/blob/master/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp?raw=true" alt="logo" width="100" height="100">

  # Contacts VCF

  **A simple and intuitive Android application to manage your contacts from VCF files.**

  <p align="center">
    <a href="https://github.com/iameffat/contactvcf/releases/latest">
      <img src="https://img.shields.io/github/v/release/iameffat/contactvcf?style=for-the-badge" alt="Release">
    </a>
    <a href="https://github.com/iameffat/contactvcf/stargazers">
      <img src="https://img.shields.io/github/stars/iameffat/contactvcf?style=for-the-badge" alt="Stars">
    </a>
    <a href="https://github.com/iameffat/contactvcf/network/members">
      <img src="https://img.shields.io/github/forks/iameffat/contactvcf?style=for-the-badge" alt="Forks">
    </a>
    <a href="https://github.com/iameffat/contactvcf/issues">
      <img src="https://img.shields.io/github/issues/iameffat/contactvcf?style=for-the-badge" alt="Issues">
    </a>
  </p>
</div>

---

### **Overview**
Contact VCF is a user-friendly Android app designed to simplify the process of importing and organizing your contacts from `.vcf` files. With a clean and modern interface built using Jetpack Compose, it provides a seamless experience for managing your contact groups.

> **Note:** Currently, the app works best with VCF files. CSV import functionality is under development and will be improved in future updates.

---

### **Table of Contents**
* [Key Features](#-key-features)
* [Technology Stack](#-technology-stack)
* [Getting Started](#-getting-started)
* [Release Notes](#-release-notes)
* [Contributing](#-contributing)
* [License](#-license)
* [Contact](#-contact)

---

### ✨ **Key Features**

| Feature | Description |
|---|---|
| 📂 **Import Contacts** | Easily import contacts from `.vcf` files. Each file creates a new, manageable group. |
| 🗂️ **Contact Groups** | Organize your imported contacts into distinct groups for better management. |
| 🔍 **Search Functionality** | Quickly find the contacts you need with a powerful and intuitive search feature. |
| ✏️ **Contact Management** | Rename, delete, and manage your contact groups and individual contacts with ease. |
| 🎨 **Theme Customization** | Personalize your experience by choosing between light, dark, or system default themes. |
| 📱 **User-Friendly Interface** | Enjoy a clean, modern, and intuitive UI built with Jetpack Compose. |

---

### 🚀 **Technology Stack**

* **Kotlin**: The primary programming language.
* **Jetpack Compose**: For building a modern and declarative UI.
* **Coroutines**: For managing background tasks and asynchronous operations.
* **Android ViewModel**: To manage UI-related data in a lifecycle-conscious way.
* **DataStore**: For storing user preferences like themes.
* **Gradle**: For build automation and dependency management.

---

### 🏁 **Getting Started**

To get a local copy up and running, follow these simple steps.

#### **Prerequisites**
* Android Studio
* An Android device or emulator running API level 24 or higher

#### **Installation**
1. Clone the repo:
   ```sh
   git clone [https://github.com/iameffat/contactvcf.git](https://github.com/iameffat/contactvcf.git)
2. Open the project in Android Studio.
3. Build and run the app on your device or emulator.

---

### 📢 **Release Notes**

<details>
  <summary><b>Contact VCF Version 1.0.0 (Latest)</b> - <i>September 24, 2025</i></summary>

### **Contact VCF Version 1.0.0**
**Release Date: September 24, 2025**

Welcome to the first release of the Contact VCF app! 🎉 We've created a powerful and simple tool to help you import and manage contacts from `.vcf` or `.csv` files.

#### ✨ **New Features**
* **File Import:** Easily import contacts from `.vcf` and `.csv` files. Each file will be saved as a separate group or "fragment."
* **Contact Profile:** Click on any contact to view their detailed information (name, photo, all phone numbers) on a beautiful profile page.
* **In-App Updates:** Check for new updates within the app and install the latest version directly from GitHub.
* **Contact Management:** A three-dot menu has been added to the contact list to edit or delete each contact.
* **Group Management:** Rename or delete unnecessary imported groups (fragments) by long-pressing on the tab.
* **Direct Actions:** Directly call, SMS, or WhatsApp each number from the contact profile.
* **Copy Number:** Long-press on any phone number to copy it to the clipboard.

#### 🎨 **Design & Performance**
* **Dynamic Theming (Material You):** The app now matches the colors of your phone's wallpaper and system theme (Android 12+).
* **Theme Control:** Choose between system default, light, or dark mode. The app will remember your preference.
* **Modern Toolbar:** A modern and functional search bar, similar to Google apps, has been added.
* **Smooth Scrolling:** Scrolling through the contact list is now smoother and lag-free.
* **Photo Fallback:** If a contact doesn't have a photo, the first letter of their name will be displayed in a colored circle.

#### 🐞 **Bug Fixes**
* Fixed a crash that occurred when importing complex VCF files (with multiple phone numbers and photos).
* Fixed an issue where an empty space was displayed above the toolbar on some devices.
* Fixed an issue where the page would not change when clicking on a tab name.

We hope this app makes your contact management easier. Your feedback is very valuable to us. Thank you!
</details>

---

### 🤝 **Contributing**

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".

Don't forget to give the project a star! Thanks again!

---

### 📜 **License**

Distributed under the MIT License. See `LICENSE` for more information.

---

### 📬 **Contact**

Your Name - [@iameffat](https://twitter.com/iameffat) - @iameffat

Project Link: [https://github.com/iameffat/contactvcf](https://github.com/iameffat/contactvcf)
