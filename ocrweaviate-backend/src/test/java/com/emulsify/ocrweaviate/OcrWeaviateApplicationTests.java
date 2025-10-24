package com.emulsify.ocrweaviate;

import com.emulsify.ocrweaviate.service.DocumentProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(properties = {
    "weaviate.api.url=http://localhost:8080",
    "gemini.api.key=test-key",
    "ocr.temp.directory=${java.io.tmpdir}/test-ocr"
})
class OcrWeaviateApplicationTests {

    @Autowired
    private DocumentProcessingService documentProcessingService;

    @Test
    void contextLoads() {
        assertNotNull(documentProcessingService);
    }

    @Test
    void healthCheckTest() {
        // Test that health check returns status information
        var healthStatus = documentProcessingService.getHealthStatus();
        assertNotNull(healthStatus);
        assertNotNull(healthStatus.get("status"));
    }
}