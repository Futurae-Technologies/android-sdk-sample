# [Manual Entry Enrollment](https://www.futurae.com/docs/guide/futurae-sdks/mobile-sdk-android/#enroll-with-activation-shortcode)

This package demonstrates how to enroll an account using an activation shortcode.

---

## 🛠 Getting Started

- `ActivationCodeScreen` is the main composable responsible for allowing the user to provide a shortcode to enroll an account.
- `ActivationCodeViewModel` delegates the enrollment with the appropriate configuration to the `EnrollmentRoute`.

---

## 🔍 Flow Overview

This is a straightforward screen that allows the user to provide a 16-digit shortcode for enrollment and delegates the actual enrollment process to the `EnrollmentRoute`.
Enrollment is performed using `AccountApi.enrollAndGetAccount` with the appropriate `EnrollmentParams`, providing `EnrollmentInput.ShortActivationCode`.

---
