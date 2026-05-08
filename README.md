# ScrollFirewall

An Android application to monitor and limit time spent on short-form video content like Instagram Reels and YouTube Shorts.

## Features
- Monitors usage of specific apps/feeds using Accessibility Services.
- Tracks cumulative time spent.
- Enforces strict session limits by minimizing the app when the threshold is reached.

## Tech Stack
- Java
- Android SDK
- XML (UI)

## How it Works
The application uses an `AccessibilityService` to detect when the user is interacting with reels or shorts. It keeps track of the time and blocks re-entry once the limit is hit.
