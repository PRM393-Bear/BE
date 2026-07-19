package com.example.PRM.AuditLogServiceImplTest;

import com.example.PRM.serviceImpl.AuditLogServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuditLogServiceImpl.
 *
 * NOTE: `restTemplate` is a `final` field initialized inline
 * (`new RestTemplate()`), so it is NOT part of the Lombok
 * @RequiredArgsConstructor-generated constructor and can't be injected via
 * @InjectMocks. Same for the @Value-injected `auditServiceBaseUrl` and
 * `auditServiceApiKey` fields, since there's no Spring context here.
 * We instantiate the service directly and use ReflectionTestUtils to swap
 * in a mocked RestTemplate and set the @Value fields.
 *
 * The @Async annotation has no effect outside a Spring container/proxy, so
 * calling log(...) in these tests executes synchronously.
 */
@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    private static final String BASE_URL = "http://audit-service";
    private static final String API_KEY = "test-api-key";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest request;

    private AuditLogServiceImpl auditLogService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        auditLogService = new AuditLogServiceImpl();
        ReflectionTestUtils.setField(auditLogService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(auditLogService, "auditServiceBaseUrl", BASE_URL);
        ReflectionTestUtils.setField(auditLogService, "auditServiceApiKey", API_KEY);

        userId = UUID.randomUUID();

        lenient().when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());
    }

    @SuppressWarnings("unchecked")
    private HttpEntity<Map<String, String>> captureRequestEntity() {
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq(BASE_URL + "/api/audit-logs"), captor.capture(), eq(Void.class));
        return captor.getValue();
    }

    // ------------------------------------------------------------------
    // Gọi RestTemplate đúng URL / headers / body
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Gửi request tới audit-service")
    class SendRequest {

        @Test
        @DisplayName("Gọi đúng URL, header Content-Type và X-API-KEY")
        void sendsCorrectUrlAndHeaders() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4");
            when(request.getHeader("User-Agent")).thenReturn("JUnit-Agent");

            auditLogService.log("CREATE", "Post", "post-1", "desc", "SUCCESS",
                    userId, "john_doe", request);

            HttpEntity<Map<String, String>> entity = captureRequestEntity();
            HttpHeaders headers = entity.getHeaders();
            assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
            assertEquals(API_KEY, headers.getFirst("X-API-KEY"));
        }

        @Test
        @DisplayName("Body chứa đúng action, entity, entityId, username, sourceService")
        void bodyContainsCoreFields() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4");
            when(request.getHeader("User-Agent")).thenReturn("JUnit-Agent");

            auditLogService.log("CREATE", "Post", "post-1", "desc", "SUCCESS",
                    userId, "john_doe", request);

            Map<String, String> body = captureRequestEntity().getBody();
            assertNotNull(body);
            assertEquals("CREATE", body.get("action"));
            assertEquals("Post", body.get("entity"));
            assertEquals("post-1", body.get("entityId"));
            assertEquals("john_doe", body.get("username"));
            assertEquals("PRM", body.get("sourceService"));
            assertEquals(userId.toString(), body.get("userId"));
        }

        @Test
        @DisplayName("Detail được format đúng khi description khác null")
        void detailFormattedWithDescription() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4");
            when(request.getHeader("User-Agent")).thenReturn("JUnit-Agent");

            auditLogService.log("CREATE", "Post", "post-1", "some description", "SUCCESS",
                    userId, "john_doe", request);

            String detail = captureRequestEntity().getBody().get("detail");
            assertEquals("some description | status=SUCCESS | ip=1.2.3.4 | userAgent=JUnit-Agent", detail);
        }

        @Test
        @DisplayName("Detail dùng chuỗi rỗng khi description là null")
        void detailFormattedWithNullDescription() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4");
            when(request.getHeader("User-Agent")).thenReturn("JUnit-Agent");

            auditLogService.log("DELETE", "Post", "post-1", null, "FAILED",
                    userId, "john_doe", request);

            String detail = captureRequestEntity().getBody().get("detail");
            assertEquals(" | status=FAILED | ip=1.2.3.4 | userAgent=JUnit-Agent", detail);
        }

        @Test
        @DisplayName("Body chứa userId=null khi userId truyền vào là null")
        void bodyUserIdNullWhenUserIdArgumentNull() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4");
            when(request.getHeader("User-Agent")).thenReturn("JUnit-Agent");

            auditLogService.log("CREATE", "Post", "post-1", "desc", "SUCCESS",
                    null, "anonymous", request);

            Map<String, String> body = captureRequestEntity().getBody();
            assertNull(body.get("userId"));
        }
    }

    // ------------------------------------------------------------------
    // getClientIp (private, kiểm tra gián tiếp qua field "ip" trong detail)
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Xác định IP client")
    class ClientIpResolution {

        @Test
        @DisplayName("Dùng X-Forwarded-For khi có giá trị")
        void usesForwardedForHeaderWhenPresent() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("9.9.9.9");
            when(request.getHeader("User-Agent")).thenReturn(null);

            auditLogService.log("CREATE", "Post", "post-1", "d", "SUCCESS",
                    userId, "john_doe", request);

            String detail = captureRequestEntity().getBody().get("detail");
            assertTrue(detail.contains("ip=9.9.9.9"));
            verify(request, never()).getRemoteAddr();
        }

        @Test
        @DisplayName("Fallback sang remoteAddr khi X-Forwarded-For là null")
        void fallsBackToRemoteAddrWhenForwardedForNull() {
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("10.0.0.1");
            when(request.getHeader("User-Agent")).thenReturn(null);

            auditLogService.log("CREATE", "Post", "post-1", "d", "SUCCESS",
                    userId, "john_doe", request);

            String detail = captureRequestEntity().getBody().get("detail");
            assertTrue(detail.contains("ip=10.0.0.1"));
        }

        @Test
        @DisplayName("Fallback sang remoteAddr khi X-Forwarded-For là chuỗi rỗng")
        void fallsBackToRemoteAddrWhenForwardedForEmpty() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("");
            when(request.getRemoteAddr()).thenReturn("10.0.0.2");
            when(request.getHeader("User-Agent")).thenReturn(null);

            auditLogService.log("CREATE", "Post", "post-1", "d", "SUCCESS",
                    userId, "john_doe", request);

            String detail = captureRequestEntity().getBody().get("detail");
            assertTrue(detail.contains("ip=10.0.0.2"));
        }

        @Test
        @DisplayName("IP và userAgent là null khi request là null")
        void ipAndUserAgentNullWhenRequestNull() {
            auditLogService.log("CREATE", "Post", "post-1", "d", "SUCCESS",
                    userId, "john_doe", null);

            String detail = captureRequestEntity().getBody().get("detail");
            assertTrue(detail.contains("ip=null"));
            assertTrue(detail.contains("userAgent=null"));
        }
    }

    // ------------------------------------------------------------------
    // Xử lý lỗi khi gọi audit-service thất bại
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Xử lý lỗi")
    class ErrorHandling {

        @Test
        @DisplayName("Không ném lỗi ra ngoài khi RestTemplate throw exception")
        void doesNotPropagateExceptionWhenRestTemplateFails() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4");
            when(request.getHeader("User-Agent")).thenReturn("JUnit-Agent");
            when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Void.class)))
                    .thenThrow(new RuntimeException("audit-service down"));

            assertDoesNotThrow(() -> auditLogService.log("CREATE", "Post", "post-1", "desc", "SUCCESS",
                    userId, "john_doe", request));
        }
    }
}