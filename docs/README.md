# Pasabayan Android - Documentation Index

## Overview
This directory contains comprehensive documentation for the Pasabayan Android project, covering architecture decisions, implementation guides, and refactoring efforts.

## 📚 Documentation Files

### 🏗️ Architecture & Refactoring
- **[Functional Programming Refactoring](FUNCTIONAL_PROGRAMMING_REFACTORING.md)** - Complete guide to the functional programming migration (Phases 1-3 completed)
- **[Role Switching Implementation](ROLE_SWITCHING_IMPLEMENTATION.md)** - iOS-style role switching between User/Carrier modes

### 🔐 Authentication Implementation
- **[Google Authentication Flow](google-authentication-flow.md)** - Google Sign-In flow documentation
- **[Google Auth Implementation](GOOGLE_AUTH_IMPLEMENTATION.md)** - Detailed Google authentication implementation
- **[Facebook Auth Implementation](FACEBOOK_AUTH_IMPLEMENTATION.md)** - Facebook authentication setup and flow

### 🚛 Carrier Features
- **[Carrier Trips Implementation](CARRIER_TRIPS_IMPLEMENTATION.md)** - Trip management and carrier functionality

## 📋 Quick Reference

### Current Project Status
- ✅ **Build Status**: Successful
- ✅ **Phase 1-3**: Functional programming refactoring completed
- ⏳ **Phase 4**: UI Composables refactoring pending
- 🎯 **Target SDK**: 35 (Android 14+)
- 🔧 **Min SDK**: 33 (Android 13+)

### Key Technologies
- **Language**: Kotlin 2.0.0
- **UI Framework**: Jetpack Compose (BOM 2024.04.01)
- **Architecture**: MVVM with Functional Programming patterns
- **Authentication**: Google Sign-In, Facebook Login
- **Build System**: Gradle with Kotlin DSL

### Development Guidelines
- **Java Compatibility**: Java 11 target
- **Code Style**: Kotlin official style
- **Architecture**: iOS-inspired patterns with functional programming
- **State Management**: Immutable state with StateFlow
- **Error Handling**: Functional Result type

## 🚀 Getting Started

1. **For New Developers**: Start with [FUNCTIONAL_PROGRAMMING_REFACTORING.md](FUNCTIONAL_PROGRAMMING_REFACTORING.md) to understand the architecture
2. **For Authentication Work**: Review the authentication implementation guides
3. **For Carrier Features**: Check the carrier-specific documentation
4. **For Architecture Questions**: Reference the functional programming patterns and examples

## 🔄 Recent Updates

### Functional Programming Migration (Latest)
- ✅ **Data Layer**: Result types, validation utilities, immutable models
- ✅ **Repository Layer**: Functional error handling, pure functions
- ✅ **ViewModels**: FunctionalViewModel base class, AuthViewModel, CarrierViewModel
- 🎯 **Next**: UI Composables refactoring (Phase 4)

### Authentication System
- ✅ Google Sign-In with One Tap support
- ✅ Facebook authentication integration
- ✅ Mock authentication for development/testing
- ✅ Type-safe error handling with Result types

### Carrier System
- ✅ Trip management with iOS Trip model compatibility
- ✅ Booking system integration
- ✅ Carrier profile and statistics
- ✅ Filter and sort functionality

## 📞 Support

For questions about:
- **Architecture & Patterns**: See functional programming documentation
- **Authentication Issues**: Check authentication implementation guides
- **Build Problems**: Review project configuration in main README
- **Feature Implementation**: Reference specific feature documentation

---

*Last Updated: January 2024*
*Documentation maintained alongside active development* 