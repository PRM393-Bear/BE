package com.example.PRM.OrganizationDetailServiceImplTest;

import com.example.PRM.dto.request.organizationDetail.OrganizationDetailReq;
import com.example.PRM.dto.response.org.OrganizationDetailRes;
import com.example.PRM.entity.OrganizationDetail;
import com.example.PRM.entity.Role;
import com.example.PRM.entity.User;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.ForbiddenException;
import com.example.PRM.exception.IllegalArgumentException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.OrganizationDetailMapper;
import com.example.PRM.repository.OrganizationDetailRepository;
import com.example.PRM.repository.UserRepository;
import com.example.PRM.serviceImpl.EmailServiceImpl;
import com.example.PRM.serviceImpl.OrganizationDetailServiceImpl;
import com.example.PRM.status_enum.VerificationOrganizationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrganizationDetailServiceImpl.
 * Covers the full flow: create, update, delete, get, approve, reject,
 * nearby search, and lookup by user.
 *
 * NOTE: Field/method names on entities & DTOs are inferred from usage in
 * OrganizationDetailServiceImpl. Adjust getters/setters/builders to match
 * your actual classes if they differ (e.g. if User/OrganizationDetail use
 * Lombok @Builder instead of plain setters).
 */
@ExtendWith(MockitoExtension.class)
class OrganizationDetailServiceImplTest {

    @Mock
    private OrganizationDetailRepository organizationDetailRepository;

    @Mock
    private OrganizationDetailMapper organizationDetailMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailServiceImpl emailService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private OrganizationDetailServiceImpl organizationDetailService;

    private User user;
    private User staffUser;
    private OrganizationDetail organizationDetail;
    private OrganizationDetailReq req;
    private OrganizationDetailRes res;
    private UUID orgId;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID();

        Role memberRole = new Role();
        memberRole.setRoleName("MEMBER");

        Role staffRole = new Role();
        staffRole.setRoleName("STAFF");

        user = new User();
        user.setUserId(UUID.randomUUID());
        user.setUserName("john_doe");
        user.setEmail("john@example.com");
        user.setRole(memberRole);

        staffUser = new User();
        staffUser.setUserId(UUID.randomUUID());
        staffUser.setUserName("staff_user");
        staffUser.setEmail("staff@example.com");
        staffUser.setRole(staffRole);

        organizationDetail = new OrganizationDetail();
        organizationDetail.setUser(user);
        organizationDetail.setStatus(VerificationOrganizationStatus.PENDING);

        req = new OrganizationDetailReq();
        req.setOrgName("My Org");
        req.setAddress("123 Street");
        req.setDescription("A great organization");
        req.setLatitude(BigDecimal.valueOf(10.0));
        req.setLongitude(BigDecimal.valueOf(20.0));

        res = new OrganizationDetailRes();
    }

    // ------------------------------------------------------------------
    // createOrganizationDetail
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("createOrganizationDetail")
    class CreateOrganizationDetail {

        @Test
        @DisplayName("Tạo tổ chức thành công khi dữ liệu hợp lệ")
        void createSuccess() {
            when(organizationDetailMapper.toEntity(req)).thenReturn(organizationDetail);
            when(userDetails.getUsername()).thenReturn("john_doe");
            when(userRepository.findByUserName("john_doe")).thenReturn(Optional.of(user));
            when(organizationDetailMapper.toResponse(organizationDetail)).thenReturn(res);

            OrganizationDetailRes result = organizationDetailService.createOrganizationDetail(req, userDetails);

            assertNotNull(result);
            assertEquals(user, organizationDetail.getUser());
            verify(organizationDetailRepository).save(organizationDetail);
        }

        @Test
        @DisplayName("Set status PENDING khi có verificationDocs")
        void createSetsPendingStatusWhenVerificationDocsPresent() {
            req.setVerificationDocs(List.of("doc1.pdf"));
            when(organizationDetailMapper.toEntity(req)).thenReturn(organizationDetail);
            when(userDetails.getUsername()).thenReturn("john_doe");
            when(userRepository.findByUserName("john_doe")).thenReturn(Optional.of(user));
            when(organizationDetailMapper.toResponse(organizationDetail)).thenReturn(res);

            organizationDetailService.createOrganizationDetail(req, userDetails);

            assertEquals(VerificationOrganizationStatus.PENDING, organizationDetail.getStatus());
        }

        @Test
        @DisplayName("Ném lỗi khi orgName rỗng")
        void createThrowsWhenOrgNameBlank() {
            req.setOrgName("  ");
            assertThrows(IllegalArgumentException.class,
                    () -> organizationDetailService.createOrganizationDetail(req, userDetails));
            verifyNoInteractions(organizationDetailRepository);
        }

        @Test
        @DisplayName("Ném lỗi khi orgName null")
        void createThrowsWhenOrgNameNull() {
            req.setOrgName(null);
            assertThrows(IllegalArgumentException.class,
                    () -> organizationDetailService.createOrganizationDetail(req, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi address rỗng")
        void createThrowsWhenAddressBlank() {
            req.setAddress("");
            assertThrows(IllegalArgumentException.class,
                    () -> organizationDetailService.createOrganizationDetail(req, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi description rỗng")
        void createThrowsWhenDescriptionNull() {
            req.setDescription(null);
            assertThrows(IllegalArgumentException.class,
                    () -> organizationDetailService.createOrganizationDetail(req, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi latitude null")
        void createThrowsWhenLatitudeNull() {
            req.setLatitude(null);
            assertThrows(IllegalArgumentException.class,
                    () -> organizationDetailService.createOrganizationDetail(req, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi longitude null")
        void createThrowsWhenLongitudeNull() {
            req.setLongitude(null);
            assertThrows(IllegalArgumentException.class,
                    () -> organizationDetailService.createOrganizationDetail(req, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy user")
        void createThrowsWhenUserNotFound() {
            when(organizationDetailMapper.toEntity(req)).thenReturn(organizationDetail);
            when(userDetails.getUsername()).thenReturn("unknown");
            when(userRepository.findByUserName("unknown")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> organizationDetailService.createOrganizationDetail(req, userDetails));
            verify(organizationDetailRepository, never()).save(any());
        }

        // ---- Bổ sung để phủ các nhánh (branch) còn thiếu theo báo cáo JaCoCo ----

        @Test
        @DisplayName("Ném lỗi khi address null (nhánh null, khác với nhánh blank)")
        void createThrowsWhenAddressNull() {
            req.setAddress(null);
            assertThrows(IllegalArgumentException.class,
                    () -> organizationDetailService.createOrganizationDetail(req, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi description rỗng (blank, khác với nhánh null)")
        void createThrowsWhenDescriptionBlank() {
            req.setDescription("   ");
            assertThrows(IllegalArgumentException.class,
                    () -> organizationDetailService.createOrganizationDetail(req, userDetails));
        }

        @Test
        @DisplayName("Không set PENDING khi verificationDocs khác null nhưng rỗng")
        void createDoesNotSetPendingWhenVerificationDocsEmpty() {
            req.setVerificationDocs(List.of());
            OrganizationDetail freshEntity = new OrganizationDetail();
            // status không được set trước -> nếu nhánh PENDING bị nhảy vào nhầm, test sẽ fail
            when(organizationDetailMapper.toEntity(req)).thenReturn(freshEntity);
            when(userDetails.getUsername()).thenReturn("john_doe");
            when(userRepository.findByUserName("john_doe")).thenReturn(Optional.of(user));
            when(organizationDetailMapper.toResponse(freshEntity)).thenReturn(res);

            organizationDetailService.createOrganizationDetail(req, userDetails);

            assertNull(freshEntity.getStatus());
            verify(organizationDetailRepository).save(freshEntity);
        }
    }

    // ------------------------------------------------------------------
    // updateOrganizationDetail
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("updateOrganizationDetail")
    class UpdateOrganizationDetail {

        @Test
        @DisplayName("Cập nhật thành công khi user là chủ sở hữu")
        void updateSuccess() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
            when(userDetails.getUsername()).thenReturn("john_doe");
            when(organizationDetailMapper.toResponse(organizationDetail)).thenReturn(res);

            OrganizationDetailReq updateReq = new OrganizationDetailReq();
            updateReq.setOrgName("New Name");
            updateReq.setAddress("New Address");
            updateReq.setWebsiteUrl("https://example.com");
            updateReq.setDescription("New description");
            updateReq.setLatitude(BigDecimal.valueOf(15.0));
            updateReq.setLongitude(BigDecimal.valueOf(25.0));
            updateReq.setVerificationDocs(List.of("doc.pdf"));
            updateReq.setAcceptedTypes(List.of("cash"));

            OrganizationDetailRes result = organizationDetailService.updateOrganizationDetail(orgId, updateReq, userDetails);

            assertNotNull(result);
            assertEquals("New Name", organizationDetail.getOrgName());
            assertEquals("New Address", organizationDetail.getAddress());
            assertEquals("New description", organizationDetail.getDescription());
            verify(organizationDetailRepository).save(organizationDetail);
        }

        @Test
        @DisplayName("Bỏ qua latitude/longitude không hợp lệ (<= 0)")
        void updateIgnoresNonPositiveLatLong() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
            when(userDetails.getUsername()).thenReturn("john_doe");
            when(organizationDetailMapper.toResponse(organizationDetail)).thenReturn(res);

            BigDecimal originalLat = organizationDetail.getLatitude();
            OrganizationDetailReq updateReq = new OrganizationDetailReq();
            updateReq.setLatitude(BigDecimal.ZERO);
            updateReq.setLongitude(BigDecimal.valueOf(-5));

            organizationDetailService.updateOrganizationDetail(orgId, updateReq, userDetails);

            assertEquals(originalLat, organizationDetail.getLatitude());
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy organization detail")
        void updateThrowsWhenNotFound() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> organizationDetailService.updateOrganizationDetail(orgId, req, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi user không phải chủ sở hữu")
        void updateThrowsWhenNotOwner() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
            when(userDetails.getUsername()).thenReturn("someone_else");

            assertThrows(ForbiddenException.class,
                    () -> organizationDetailService.updateOrganizationDetail(orgId, req, userDetails));
            verify(organizationDetailRepository, never()).save(any());
        }

        // ---- Bổ sung để phủ các nhánh (branch) còn thiếu theo báo cáo JaCoCo ----

        @Test
        @DisplayName("Cập nhật avtOrg khi được truyền vào (nhánh true của avtOrg != null)")
        void updateSetsAvtOrgWhenProvided() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
            when(userDetails.getUsername()).thenReturn("john_doe");
            when(organizationDetailMapper.toResponse(organizationDetail)).thenReturn(res);

            OrganizationDetailReq updateReq = new OrganizationDetailReq();
            updateReq.setAvtOrg("https://example.com/avatar.png");

            organizationDetailService.updateOrganizationDetail(orgId, updateReq, userDetails);

            assertEquals("https://example.com/avatar.png", organizationDetail.getAvtOrg());
        }

        @Test
        @DisplayName("Bỏ qua latitude/longitude khi null (nhánh false của != null)")
        void updateIgnoresNullLatLong() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
            when(userDetails.getUsername()).thenReturn("john_doe");
            when(organizationDetailMapper.toResponse(organizationDetail)).thenReturn(res);

            BigDecimal originalLat = organizationDetail.getLatitude();
            BigDecimal originalLng = organizationDetail.getLongitude();
            OrganizationDetailReq updateReq = new OrganizationDetailReq();
            updateReq.setLatitude(null);
            updateReq.setLongitude(null);

            organizationDetailService.updateOrganizationDetail(orgId, updateReq, userDetails);

            assertEquals(originalLat, organizationDetail.getLatitude());
            assertEquals(originalLng, organizationDetail.getLongitude());
        }

        @Test
        @DisplayName("Bỏ qua verificationDocs khi khác null nhưng rỗng")
        void updateIgnoresEmptyVerificationDocs() {
            organizationDetail.setVerificationDocs(List.of("old-doc.pdf"));
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
            when(userDetails.getUsername()).thenReturn("john_doe");
            when(organizationDetailMapper.toResponse(organizationDetail)).thenReturn(res);

            OrganizationDetailReq updateReq = new OrganizationDetailReq();
            updateReq.setVerificationDocs(List.of());

            organizationDetailService.updateOrganizationDetail(orgId, updateReq, userDetails);

            assertEquals(List.of("old-doc.pdf"), organizationDetail.getVerificationDocs());
        }

        @Test
        @DisplayName("Bỏ qua acceptedTypes khi khác null nhưng rỗng")
        void updateIgnoresEmptyAcceptedTypes() {
            organizationDetail.setAcceptedTypes(List.of("cash"));
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
            when(userDetails.getUsername()).thenReturn("john_doe");
            when(organizationDetailMapper.toResponse(organizationDetail)).thenReturn(res);

            OrganizationDetailReq updateReq = new OrganizationDetailReq();
            updateReq.setAcceptedTypes(List.of());

            organizationDetailService.updateOrganizationDetail(orgId, updateReq, userDetails);

            assertEquals(List.of("cash"), organizationDetail.getAcceptedTypes());
        }
    }

    // ------------------------------------------------------------------
    // deleteOrganizationDetail
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("deleteOrganizationDetail")
    class DeleteOrganizationDetail {

        @Test
        @DisplayName("Xóa thành công khi user là chủ sở hữu")
        void deleteSuccess() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
            when(userDetails.getUsername()).thenReturn("john_doe");
            when(organizationDetailMapper.toResponse(organizationDetail)).thenReturn(res);

            OrganizationDetailRes result = organizationDetailService.deleteOrganizationDetail(orgId, userDetails);

            assertNotNull(result);
            verify(organizationDetailRepository).delete(organizationDetail);
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy organization detail")
        void deleteThrowsWhenNotFound() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> organizationDetailService.deleteOrganizationDetail(orgId, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi user không phải chủ sở hữu")
        void deleteThrowsWhenNotOwner() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
            when(userDetails.getUsername()).thenReturn("someone_else");

            assertThrows(ForbiddenException.class,
                    () -> organizationDetailService.deleteOrganizationDetail(orgId, userDetails));
            verify(organizationDetailRepository, never()).delete(any());
        }
    }

    // ------------------------------------------------------------------
    // getOrganizationDetail
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("getOrganizationDetail")
    class GetOrganizationDetail {

        @Test
        @DisplayName("Trả về organization detail khi tìm thấy")
        void getSuccess() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
            when(organizationDetailMapper.toResponse(organizationDetail)).thenReturn(res);

            OrganizationDetailRes result = organizationDetailService.getOrganizationDetail(orgId);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy")
        void getThrowsWhenNotFound() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> organizationDetailService.getOrganizationDetail(orgId));
        }
    }

    // ------------------------------------------------------------------
    // getAllOrganizations / getPendingOrganizations
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("getAllOrganizations & getPendingOrganizations")
    class GetLists {

        @Test
        @DisplayName("Trả về danh sách tất cả tổ chức")
        void getAllSuccess() {
            when(organizationDetailRepository.findAll()).thenReturn(List.of(organizationDetail));
            when(organizationDetailMapper.toResponse(organizationDetail)).thenReturn(res);

            List<OrganizationDetailRes> result = organizationDetailService.getAllOrganizations();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Trả về danh sách rỗng khi không có tổ chức nào")
        void getAllEmpty() {
            when(organizationDetailRepository.findAll()).thenReturn(List.of());

            List<OrganizationDetailRes> result = organizationDetailService.getAllOrganizations();

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Trả về danh sách tổ chức đang chờ duyệt")
        void getPendingSuccess() {
            when(organizationDetailRepository.findByStatus(VerificationOrganizationStatus.PENDING))
                    .thenReturn(List.of(organizationDetail));
            when(organizationDetailMapper.toResponse(organizationDetail)).thenReturn(res);

            List<OrganizationDetailRes> result = organizationDetailService.getPendingOrganizations();

            assertEquals(1, result.size());
        }
    }

    // ------------------------------------------------------------------
    // approveOrganization
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("approveOrganization")
    class ApproveOrganization {

        @Test
        @DisplayName("Duyệt thành công khi staff hợp lệ và org đang PENDING")
        void approveSuccess() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
            when(userDetails.getUsername()).thenReturn("staff_user");
            when(userRepository.findByUserName("staff_user")).thenReturn(Optional.of(staffUser));
            when(organizationDetailMapper.toResponse(organizationDetail)).thenReturn(res);

            OrganizationDetailRes result = organizationDetailService.approveOrganization(orgId, userDetails);

            assertNotNull(result);
            assertEquals(VerificationOrganizationStatus.APPROVED, organizationDetail.getStatus());
            assertNotNull(organizationDetail.getApprovedAt());
            assertEquals(staffUser.getUserId().toString(), organizationDetail.getApprovedBy());
            verify(organizationDetailRepository).save(organizationDetail);
            verify(emailService).sendApprovalEmail(user.getEmail());
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy tổ chức")
        void approveThrowsWhenNotFound() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> organizationDetailService.approveOrganization(orgId, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi tổ chức không ở trạng thái PENDING")
        void approveThrowsWhenNotPending() {
            organizationDetail.setStatus(VerificationOrganizationStatus.APPROVED);
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));

            assertThrows(IllegalArgumentException.class,
                    () -> organizationDetailService.approveOrganization(orgId, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy staff user")
        void approveThrowsWhenStaffNotFound() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
            when(userDetails.getUsername()).thenReturn("ghost");
            when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> organizationDetailService.approveOrganization(orgId, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi user không có role STAFF")
        void approveThrowsWhenNotStaffRole() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
            when(userDetails.getUsername()).thenReturn("john_doe");
            when(userRepository.findByUserName("john_doe")).thenReturn(Optional.of(user));

            assertThrows(IllegalArgumentException.class,
                    () -> organizationDetailService.approveOrganization(orgId, userDetails));
            verify(emailService, never()).sendApprovalEmail(anyString());
        }
    }

    // ------------------------------------------------------------------
    // rejectOrganization
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("rejectOrganization")
    class RejectOrganization {

        private final String reason = "Thiếu giấy tờ xác minh";

        @Test
        @DisplayName("Từ chối thành công khi staff hợp lệ và org đang PENDING")
        void rejectSuccess() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
            when(userDetails.getUsername()).thenReturn("staff_user");
            when(userRepository.findByUserName("staff_user")).thenReturn(Optional.of(staffUser));
            when(organizationDetailMapper.toResponse(organizationDetail)).thenReturn(res);

            OrganizationDetailRes result = organizationDetailService.rejectOrganization(orgId, userDetails, reason);

            assertNotNull(result);
            assertEquals(VerificationOrganizationStatus.REJECTED, organizationDetail.getStatus());
            assertNotNull(organizationDetail.getRejectedAt());
            assertEquals(staffUser.getUserId().toString(), organizationDetail.getRejectedBy());
            assertEquals(reason, organizationDetail.getRejectedReason());
            verify(emailService).sendRejectEmail(user.getEmail(), reason);
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy tổ chức")
        void rejectThrowsWhenNotFound() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> organizationDetailService.rejectOrganization(orgId, userDetails, reason));
        }

        @Test
        @DisplayName("Ném lỗi khi tổ chức không ở trạng thái PENDING")
        void rejectThrowsWhenNotPending() {
            organizationDetail.setStatus(VerificationOrganizationStatus.REJECTED);
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));

            assertThrows(BadRequestException.class,
                    () -> organizationDetailService.rejectOrganization(orgId, userDetails, reason));
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy staff user")
        void rejectThrowsWhenStaffNotFound() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
            when(userDetails.getUsername()).thenReturn("ghost");
            when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> organizationDetailService.rejectOrganization(orgId, userDetails, reason));
        }

        @Test
        @DisplayName("Ném lỗi khi user không có role STAFF")
        void rejectThrowsWhenNotStaffRole() {
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
            when(userDetails.getUsername()).thenReturn("john_doe");
            when(userRepository.findByUserName("john_doe")).thenReturn(Optional.of(user));

            assertThrows(IllegalArgumentException.class,
                    () -> organizationDetailService.rejectOrganization(orgId, userDetails, reason));
            verify(emailService, never()).sendRejectEmail(anyString(), anyString());
        }
    }

    // ------------------------------------------------------------------
    // getNearbyOrganizations
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("getNearbyOrganizations")
    class GetNearbyOrganizations {

        @Test
        @DisplayName("Trả về danh sách tổ chức gần đó với bán kính hợp lệ")
        void nearbySuccessWithGivenRadius() {
            BigDecimal lat = BigDecimal.valueOf(10.0);
            BigDecimal lng = BigDecimal.valueOf(20.0);
            when(organizationDetailRepository.findNearbyOrganizations(lat, lng, 5.0))
                    .thenReturn(List.of(organizationDetail));
            when(organizationDetailMapper.toResponse(organizationDetail)).thenReturn(res);

            List<OrganizationDetailRes> result = organizationDetailService.getNearbyOrganizations(lat, lng, 5.0);

            assertEquals(1, result.size());
            verify(organizationDetailRepository).findNearbyOrganizations(lat, lng, 5.0);
        }

        @Test
        @DisplayName("Dùng bán kính mặc định 50.0 khi radius <= 0")
        void nearbyUsesDefaultRadiusWhenNonPositive() {
            BigDecimal lat = BigDecimal.valueOf(10.0);
            BigDecimal lng = BigDecimal.valueOf(20.0);
            when(organizationDetailRepository.findNearbyOrganizations(lat, lng, 50.0))
                    .thenReturn(List.of());

            organizationDetailService.getNearbyOrganizations(lat, lng, 0);

            verify(organizationDetailRepository).findNearbyOrganizations(lat, lng, 50.0);
        }

        @Test
        @DisplayName("Ném lỗi khi latitude null")
        void nearbyThrowsWhenLatitudeNull() {
            assertThrows(BadRequestException.class,
                    () -> organizationDetailService.getNearbyOrganizations(null, BigDecimal.TEN, 10));
        }

        @Test
        @DisplayName("Ném lỗi khi longitude null")
        void nearbyThrowsWhenLongitudeNull() {
            assertThrows(BadRequestException.class,
                    () -> organizationDetailService.getNearbyOrganizations(BigDecimal.TEN, null, 10));
        }
    }

    // ------------------------------------------------------------------
    // getOrganizationDetailByUserId
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("getOrganizationDetailByUserId")
    class GetOrganizationDetailByUserId {

        @Test
        @DisplayName("Trả về organization detail của user hiện tại")
        void getByUserIdSuccess() {
            when(userDetails.getUsername()).thenReturn("john_doe");
            when(userRepository.findByUserName("john_doe")).thenReturn(Optional.of(user));
            when(organizationDetailRepository.findByUser_UserId(user.getUserId()))
                    .thenReturn(Optional.of(organizationDetail));
            when(organizationDetailMapper.toResponse(organizationDetail)).thenReturn(res);

            OrganizationDetailRes result = organizationDetailService.getOrganizationDetailByUserId(userDetails);

            assertNotNull(result);
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy user")
        void getByUserIdThrowsWhenUserNotFound() {
            when(userDetails.getUsername()).thenReturn("ghost");
            when(userRepository.findByUserName("ghost")).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> organizationDetailService.getOrganizationDetailByUserId(userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi user chưa có organization detail")
        void getByUserIdThrowsWhenOrgNotFound() {
            when(userDetails.getUsername()).thenReturn("john_doe");
            when(userRepository.findByUserName("john_doe")).thenReturn(Optional.of(user));
            when(organizationDetailRepository.findByUser_UserId(user.getUserId()))
                    .thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> organizationDetailService.getOrganizationDetailByUserId(userDetails));
        }
    }
}