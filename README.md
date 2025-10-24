# Emulsify - Document Analysis Android App

This is a Kotlin-based Android application that provides intelligent document analysis functionality, converted from the original React frontend. The app allows users to upload PDF documents, perform cross-document analysis, and get detailed reasoning insights powered by AI.

## Features

### 🔬 Core Functionality

- **Document Upload**: Upload up to 3 PDF documents simultaneously
- **Natural Language Querying**: Ask questions in plain English about your documents
- **Cross-Document Analysis**: Compare and analyze information across multiple documents
- **Intelligent Reasoning**: Get detailed insights into how queries are processed and answered

### 📱 Mobile-Optimized Features

- **Bottom Navigation**: Easy access to Dashboard, Documents, and Reasoning sections
- **Responsive Design**: Material Design 3 components with clean, modern UI
- **File Management**: Intuitive file selection and management
- **Real-time Status**: Live upload and processing status updates
- **Offline-Ready**: Local data management with network-based processing

## Architecture

### 🏗️ Project Structure

```
app/
├── src/main/java/com/emulsify/documentanalysis/
│   ├── MainActivity.kt                 # Main activity with navigation
│   ├── fragments/                      # UI fragments
│   │   ├── DashboardFragment.kt        # Welcome screen
│   │   ├── DocumentAnalysisFragment.kt # Document upload & search
│   │   └── ReasoningFragment.kt        # Analysis insights
│   ├── viewmodels/
│   │   └── MainViewModel.kt            # Shared app state management
│   ├── network/
│   │   ├── ApiService.kt               # Retrofit API definitions
│   │   └── NetworkClient.kt            # HTTP client configuration
│   ├── models/
│   │   └── DataModels.kt               # Data classes for API responses
│   └── adapters/
│       ├── SelectedFilesAdapter.kt     # File selection display
│       └── DocumentAdapter.kt          # Uploaded documents display
└── src/main/res/                       # Resources (layouts, strings, etc.)
```

### 🎯 Key Components

#### MainActivity

- Handles bottom navigation between fragments
- Manages file picker integration
- Coordinates permission requests for file access

#### MainViewModel

- Centralized state management using Android Architecture Components
- Handles API communication with Retrofit
- Manages file upload and search operations
- Provides reactive data updates to UI components

#### Network Layer

- **ApiService**: RESTful API definitions using Retrofit annotations
- **NetworkClient**: Configured HTTP client with logging and timeout settings
- **Base URL**: Configured for Android emulator (`10.0.2.2:8000`)

#### Fragments

- **DashboardFragment**: Welcome screen with feature overview
- **DocumentAnalysisFragment**: Main functionality for file upload and search
- **ReasoningFragment**: Detailed analysis breakdown and insights

## API Integration

### 🌐 Backend Compatibility

The app is designed to work with the existing backend API endpoints:

```kotlin
// Upload endpoints
@POST("upload")           // Multiple file upload
@POST("upload-single")    // Single file upload

// Query endpoint
@POST("query")            // Document search

// Management
@DELETE("documents")      // Clear all documents
```

### 📡 Network Configuration

- **Base URL**: `http://10.0.2.2:8000/` (Android emulator)
- **Timeouts**: 30 seconds for connect/read/write operations
- **Logging**: Full request/response logging for debugging
- **Content Type**: Multipart form data for file uploads, JSON for queries

## Setup Instructions

### 📋 Prerequisites

- Android Studio Arctic Fox or later
- Android SDK API 24+ (Android 7.0)
- Kotlin 1.9.0+
- Backend server running on `localhost:8000`

### 🚀 Installation Steps

1. **Clone/Extract the Project**

   ```bash
   cd java-migration
   ```

2. **Open in Android Studio**

   - File → Open → Select the `java-migration` folder
   - Wait for Gradle sync to complete

3. **Configure Backend Connection**

   - For Android Emulator: No changes needed (uses `10.0.2.2:8000`)
   - For Physical Device: Update `BASE_URL` in `NetworkClient.kt`:
     ```kotlin
     private const val BASE_URL = "http://YOUR_COMPUTER_IP:8000/"
     ```

4. **Build and Run**
   - Connect device or start emulator
   - Click "Run" in Android Studio
   - Grant storage permissions when prompted

### 🔧 Development Setup

#### Network Configuration Options

```kotlin
// For emulator (default)
private const val BASE_URL = "http://10.0.2.2:8000/"

// For physical device on same network
private const val BASE_URL = "http://192.168.1.XXX:8000/"

// For localhost testing with adb port forwarding
// Run: adb reverse tcp:8000 tcp:8000
private const val BASE_URL = "http://localhost:8000/"
```

#### Permissions

The app requires the following permissions:

- `READ_EXTERNAL_STORAGE`: To access and read PDF files
- `INTERNET`: To communicate with the backend API

## Technology Stack

### 🛠️ Core Technologies

- **Kotlin**: Modern Android development language
- **Android Architecture Components**: ViewModel, LiveData, ViewBinding
- **Material Design 3**: Modern UI components and theming
- **Retrofit 2**: Type-safe HTTP client for API communication
- **OkHttp**: HTTP client with logging and interceptor support
- **Gson**: JSON serialization/deserialization
- **Coroutines**: Asynchronous programming for network operations

### 📦 Key Dependencies

```gradle
// UI and Architecture
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
implementation 'androidx.navigation:navigation-fragment-ktx:2.7.5'
implementation 'com.google.android.material:material:1.10.0'

// Networking
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

// Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
```

## Usage Guide

### 📄 Document Upload

1. Navigate to "Documents" tab
2. Tap "Choose PDF Documents"
3. Select up to 3 PDF files
4. Review selected files and tap "Process Documents"
5. Wait for upload and processing completion

### 🔍 Searching Documents

1. After successful upload, enter your question in the search field
2. Tap "Ask" or press enter
3. View the comprehensive answer and explanation
4. Check cross-document analysis if multiple files were uploaded

### 🧠 Reasoning Analysis

1. Navigate to "Reasoning" tab after performing a search
2. View detailed query analysis including:
   - Query type and complexity assessment
   - Processing pipeline breakdown
   - Quality metrics and confidence scores
   - Technical metadata

## API Response Handling

### 📊 Data Models

```kotlin
data class SearchResult(
    val answer: String,
    val explanation: String,
    val crossDocumentAnalysis: String?,
    val sources: List<String>?
)

data class UploadedDocument(
    val filename: String,
    val chunksCreated: Int,
    val textLength: Int
)
```

### 🔄 State Management

The app uses reactive programming with LiveData to manage state:

- File selection state
- Upload progress and status
- Search results and loading states
- Error handling and user feedback

## Customization

### 🎨 Theming

The app uses Material Design 3 with custom colors defined in `colors.xml`:

- **Primary**: `#3498db` (Blue)
- **Secondary**: `#9b59b6` (Purple)
- **Background**: `#f5f7fa` (Light gray)

### 🔧 Configuration

Key configuration points:

- **API Base URL**: `NetworkClient.kt`
- **File Upload Limits**: `MainViewModel.kt`
- **UI Strings**: `strings.xml`
- **Colors and Themes**: `colors.xml`, `themes.xml`

## Testing

### 🧪 Testing Backend Connection

1. Ensure your backend server is running on `localhost:8000`
2. For emulator testing, the app automatically uses `10.0.2.2:8000`
3. For physical devices, update the IP address in `NetworkClient.kt`
4. Use adb port forwarding if needed: `adb reverse tcp:8000 tcp:8000`

### 📱 Device Testing

- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Recommended**: Test on both emulator and physical device

## Troubleshooting

### 🐛 Common Issues

#### Network Connection Problems

- Check if backend server is running
- Verify IP address configuration for physical devices
- Check firewall settings on development machine

#### File Upload Issues

- Ensure storage permissions are granted
- Check file size limitations
- Verify PDF file format

#### Build Issues

- Sync Gradle project in Android Studio
- Check internet connection for dependency downloads
- Clear cache: Build → Clean Project

### 📋 Debug Information

The app includes comprehensive logging:

- Network requests and responses
- File upload progress
- Error details and stack traces

## Migration Notes

### 🔄 From React to Android

This Android app provides equivalent functionality to the original React frontend:

#### Feature Parity

- ✅ Document upload (up to 3 PDFs)
- ✅ Natural language search
- ✅ Cross-document analysis
- ✅ Detailed reasoning insights
- ✅ Real-time status updates
- ✅ Error handling and validation

#### Architecture Improvements

- **Mobile-Optimized**: Native Android UI patterns
- **Offline Capability**: Local state management
- **Performance**: Native code execution
- **Integration**: Direct OS file system access

#### Design Adaptations

- Bottom navigation instead of sidebar
- Mobile-friendly touch interactions
- Material Design components
- Responsive layouts for different screen sizes

## Contributing

### 📝 Development Workflow

1. Fork the repository
2. Create feature branches
3. Follow Kotlin coding conventions
4. Add appropriate tests
5. Submit pull requests

### 🎯 Code Style

- Follow Android Kotlin style guide
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Maintain consistent formatting

## License

This project maintains the same license as the original React frontend.

---

## Contact

For questions about the Android migration or technical support, please refer to the main project documentation or create an issue in the repository.
