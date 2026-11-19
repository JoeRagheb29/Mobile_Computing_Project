[readme.md](https://github.com/user-attachments/files/35111093/readme.md)
# Daf3a — Digital Wallet Application

An Android digital wallet prototype built in **Kotlin** for the **Mobile Computing** course (**Team 30**) at the **Faculty of Computer and Information Sciences, Ain Shams University**, supervised by **Dr. Soha Hussien**.

📄 **Project Abstract:** [Daf3a\_ACM\_Abstract.pdf](https://www.google.com/search?q=./Daf3a_ACM_Abstract.pdf)

---

## Overview

**Daf3a** demonstrates how core mobile computing concepts—reactive UI, local relational persistence, session management, background polling, two-factor authentication, and Android system intents—fit together in a multi-screen Android application.

> **Note:** Daf3a is a local course prototype. Account balances and transfers are stored on-device using Room (SQLite), and external mobile wallet transfers are simulated locally without connecting to a live payment gateway.

---

## Features

* **Registration & Login:** Supports account creation and sign-in with Egyptian mobile number format validation and duplicate username/phone checks.
* **Two-Factor Authentication (2FA / OTP):** Protects registration, login, and money transfers using OTP generation and verification (`OtpManager`, `SmsHelper`, and `TwoFactorAuthDialog`).
* **Persistent Sessions:** Keeps users signed in across app launches via `SessionManager` (`SharedPreferences`).
* **Home Dashboard & Balance Recharge:** Displays the current balance, quick navigation actions, and a password-verified balance recharge dialog.
* **Incoming Transaction Scanner:** Periodically checks for new incoming transfers (`startTransactionScanner` + `Daf3aTxPrefs`), updates the balance automatically, and triggers an SMS notification.
* **Send Money (Daf3a & Simulated Wallet):**
  * **Daf3a Transfer:** Transfers funds between registered Daf3a accounts with recipient lookup, self-transfer prevention, balance validation, and 2FA confirmation.
  * **Wallet Transfer:** Simulated external mobile wallet transfer where the sender's balance is validated and deducted locally.
  * **Contact Picker:** Lets users select a recipient's phone number directly from device contacts (`ContactsContract`).
* **Shareable Transaction Receipts:** Renders transfer details inside a `MaterialCardView`, exports the card as a `Bitmap` via `Canvas`, and shares it through `FileProvider`.
* **Transaction History & Profile:** Lists Daf3a transactions from newest to oldest and displays user account details (username, phone number, balance, and account ID).

---

## Tech Stack

* **Language:** 100% Kotlin (`minSdk 24`, `compileSdk` / `targetSdk 37`, Java 11)
* **UI:** Jetpack Compose + Material 3 (core screens) and XML + AppCompat / Material Components (`TransactionReceiptActivity`)
* **Local Database:** Room (`v2.8.4`) with KSP (`v2.3.8`) and Kotlin Coroutines
* **Session & State:** `SharedPreferences` (`Daf3aPrefs` and `Daf3aTxPrefs`)
* **Testing:** JUnit unit tests for `OtpManager`

---

## Project Structure

```text
app/src/main/java/com/example/mobile_computing_project/
├── DataBase/
│   ├── DataBase.kt          # Daf3aDatabase (Room v2), UserEntity, TransactionEntity, DAOs
│   └── SessionManager.kt    # SharedPreferences wrapper (Daf3aPrefs & Daf3aTxPrefs)
├── utils/
│   ├── OtpManager.kt        # OTP generation and validation
│   └── SmsHelper.kt         # SMS notification helper
├── MainActivity.kt          # Login & Registration entry point
├── HomeActivity.kt          # Dashboard, balance recharge, & incoming transaction scanner
├── SendMoneyActivity.kt     # Daf3a & simulated Wallet transfers + contact picker
├── TransactionReceiptActivity.kt  # Receipt card rendering & FileProvider sharing
├── TransactionHistoryActivity.kt  # Chronological transaction log
└── ProfileActivity.kt       # User account details
```

---

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/JoeRagheb29/Mobile_Computing_Project.git
   ```
2. Open the project in **Android Studio**.
3. Let Gradle sync all dependencies.
4. Run the `app` configuration on an Android emulator or physical device running **Android 7.0 (API 24)** or higher.

---

## Team 30

* **Yousef Ayman Fathy** ([@YousefAyman92](https://www.google.com/search?q=https://github.com/YousefAyman92))
* **Yousef Ahmed Ebid** ([@Youssef-Ebid](https://www.google.com/search?q=https://github.com/Youssef-Ebid))
* **Hisham Mohamed Maher** ([@HishamMohamedMaher](https://www.google.com/search?q=https://github.com/HishamMohamedMaher))
* **Yousif Hassan Mamdouh** ([@usifhsan16](https://www.google.com/search?q=https://github.com/usifhsan16))
* **Youssef Ragheb Abdelbadie** ([@JoeRagheb29](https://github.com/JoeRagheb29))
* **Shahd Nagy Mohamed** ([@shahdnagy](https://www.google.com/search?q=https://github.com/shahdnagy))
