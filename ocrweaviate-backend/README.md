# OCRWeaviate Backend - Spring Boot

A complete Spring Boot backend for PDF document processing with OCR, vector search, and AI-powered responses.

## 🚀 Features

- **PDF Upload & OCR**: Process PDF documents using Tesseract OCR
- **Vector Storage**: Store document chunks in Weaviate vector database
- **Semantic Search**: Find relevant content using BM25 and vector similarity
- **AI Responses**: Generate intelligent answers using Gemini 2.5 Pro API
- **Cross-Document Analysis**: Analyze and compare information across multiple documents
- **RESTful API**: Complete REST API with JSON responses
- **Error Handling**: Comprehensive error handling and logging

## 🛠️ Tech Stack

- **Java 17+**
- **Spring Boot 3.2.0**
- **Maven** for dependency management
- **Tesseract OCR** via Tess4J for text extraction
- **Apache PDFBox** for PDF processing
- **Weaviate** vector database
- **Google Gemini API** for AI responses
- **WebFlux** for HTTP client operations

## 📋 Prerequisites

1. **Java 17 or higher**
2. **Maven 3.6+**
3. **Tesseract OCR** installed on your system
4. **Weaviate** instance running (local or cloud)
5. **Google API Key** for Gemini

### Installing Tesseract

**Windows:**

```bash
# Download from: https://github.com/UB-Mannheim/tesseract/wiki
# Or using Chocolatey:
choco install tesseract
```

**macOS:**

```bash
brew install tesseract
```

**Linux (Ubuntu/Debian):**

```bash
sudo apt update
sudo apt install tesseract-ocr
```

### Setting up Weaviate

**Option 1: Docker (Recommended)**

```bash
docker run -d \
  --name weaviate \
  -p 8080:8080 \
  -e QUERY_DEFAULTS_LIMIT=25 \
  -e AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED=true \
  -e PERSISTENCE_DATA_PATH='/var/lib/weaviate' \
  -e DEFAULT_VECTORIZER_MODULE='text2vec-transformers' \
  -e ENABLE_MODULES='text2vec-transformers' \
  -e TRANSFORMERS_INFERENCE_API='http://t2v-transformers:8080' \
  cr.weaviate.io/semitechnologies/weaviate:1.22.4
```

**Option 2: Weaviate Cloud Services**

- Sign up at [Weaviate Cloud](https://console.weaviate.cloud/)
- Create a cluster and get your URL and API key

## ⚙️ Configuration

1. **Clone and navigate to the project:**

```bash
cd ocr/java-migration/ocrweaviate-backend
```

2. **Update `src/main/resources/application.properties`:**

```properties
# Weaviate Configuration
weaviate.api.url=http://localhost:8080
weaviate.api.key=YOUR_WEAVIATE_API_KEY_HERE
weaviate.collection.name=PolicyDocument

# Gemini AI Configuration
gemini.api.key=YOUR_GOOGLE_API_KEY_HERE
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent

# OCR Configuration (optional)
ocr.tesseract.datapath=/usr/share/tesseract-ocr/4.00/tessdata
ocr.tesseract.language=eng
```

3. **Environment Variables (Alternative):**

```bash
export WEAVIATE_URL=http://localhost:8080
export WEAVIATE_API_KEY=your-key
export GOOGLE_API_KEY=your-gemini-key
```

## 🏃 Running the Application

### Development Mode

```bash
# Build and run
mvn spring-boot:run

# Or build JAR and run
mvn clean package
java -jar target/ocrweaviate-backend-1.0.0.jar
```

### Production Mode

```bash
# Build for production
mvn clean package -Pproduction

# Run with specific profile
java -jar target/ocrweaviate-backend-1.0.0.jar --spring.profiles.active=production
```

The server will start on **http://localhost:8000**

## 📚 API Endpoints

### Health Check

```http
GET /
GET /health
```

### Document Upload

```http
POST /upload
Content-Type: multipart/form-data
Body: files (up to 3 PDF files)

POST /upload-single
Content-Type: multipart/form-data
Body: file (single PDF file)
```

### Query Documents

```http
POST /query
Content-Type: application/json

{
  "query": "What is the policy about remote work?"
}
```

### Document Management

```http
GET /documents        # List all documents
DELETE /documents     # Clear all documents
```

### Testing

```http
POST /test-upload     # Test file upload
```

## 🧪 Testing

### Run Tests

```bash
mvn test
```

### Manual Testing with cURL

**Upload a PDF:**

```bash
curl -X POST http://localhost:8000/upload \
  -F "files=@document.pdf"
```

**Query documents:**

```bash
curl -X POST http://localhost:8000/query \
  -H "Content-Type: application/json" \
  -d '{"query": "What are the main policies?"}'
```

**Health check:**

```bash
curl http://localhost:8000/health
```

## 📂 Project Structure

```
src/
├── main/
│   ├── java/com/emulsify/ocrweaviate/
│   │   ├── OcrWeaviateApplication.java     # Main application
│   │   ├── config/                         # Configuration classes
│   │   │   ├── CorsConfig.java
│   │   │   ├── WeaviateConfig.java
│   │   │   └── GeminiConfig.java
│   │   ├── controller/                     # REST controllers
│   │   │   └── DocumentController.java
│   │   ├── service/                        # Business logic
│   │   │   ├── OCRService.java
│   │   │   ├── WeaviateService.java
│   │   │   ├── GeminiService.java
│   │   │   └── DocumentProcessingService.java
│   │   ├── model/                          # Data models
│   │   │   ├── QueryRequest.java
│   │   │   ├── QueryResponse.java
│   │   │   ├── UploadResponse.java
│   │   │   └── WeaviateDocument.java
│   │   └── exception/                      # Exception handling
│   │       └── GlobalExceptionHandler.java
│   └── resources/
│       └── application.properties          # Configuration
└── test/
    └── java/com/emulsify/ocrweaviate/
        └── OcrWeaviateApplicationTests.java
```

## 🔧 Configuration Options

### File Upload Limits

```properties
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

### Text Processing

```properties
text.chunk.size=500
text.chunk.overlap=50
text.min.chunk.length=30
```

### Timeouts

```properties
weaviate.timeout.seconds=30
gemini.timeout.seconds=60
```

### CORS

```properties
cors.allowed-origins=http://localhost:3000,http://localhost:3001
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
```

## 🐛 Troubleshooting

### Common Issues

1. **Tesseract not found:**

   - Install Tesseract OCR
   - Set `ocr.tesseract.datapath` in properties

2. **Weaviate connection failed:**

   - Ensure Weaviate is running on port 8080
   - Check network connectivity
   - Verify API key if using cloud

3. **Gemini API errors:**

   - Verify your Google API key
   - Check API quota and billing
   - Ensure internet connectivity

4. **Memory issues with large PDFs:**
   - Increase JVM heap size: `-Xmx2g`
   - Reduce chunk size in properties

### Logs

Check application logs for detailed error information:

```bash
tail -f logs/application.log
```

## 🚀 Deployment

### Docker (Optional)

```dockerfile
FROM openjdk:17-jdk-slim

# Install Tesseract
RUN apt-get update && apt-get install -y tesseract-ocr

COPY target/ocrweaviate-backend-1.0.0.jar app.jar
EXPOSE 8000
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Build and run:

```bash
docker build -t ocrweaviate-backend .
docker run -p 8000:8000 ocrweaviate-backend
```

## 📝 Migration from Python

This Spring Boot application provides identical functionality to the original Python/FastAPI backend:

- ✅ PDF upload and OCR processing
- ✅ Weaviate vector storage
- ✅ Cross-document analysis
- ✅ Gemini AI integration
- ✅ Same API endpoints and response formats
- ✅ Error handling and logging
- ✅ CORS support

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For issues and questions:

1. Check the troubleshooting section
2. Review application logs
3. Create an issue in the repository
