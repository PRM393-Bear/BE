package com.example.PRM.DonationEventServiceImplTest;

import com.example.PRM.dto.request.donationEvent.DonationEventFilterReq;
import com.example.PRM.dto.request.donationEvent.DonationEventReq;
import com.example.PRM.dto.response.donationEvent.DonationEventLogRes;
import com.example.PRM.dto.response.donationEvent.DonationEventRes;
import com.example.PRM.entity.DonationEvent;
import com.example.PRM.entity.OrganizationDetail;
import com.example.PRM.exception.BadRequestException;
import com.example.PRM.exception.NotFoundException;
import com.example.PRM.mapper.DonationEventMapper;
import com.example.PRM.repository.DonationEventRepository;
import com.example.PRM.repository.OrganizationDetailRepository;
import com.example.PRM.serviceImpl.DonationEventServiceImpl;
import com.example.PRM.status_enum.EventStatus;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DonationEventServiceImpl.
 * Covers the full flow: create, update, delete, get all, filter search,
 * get by org, cancel/complete/ongoing status transitions.
 *
 * NOTE: Field/method names on entities & DTOs are inferred from usage in
 * DonationEventServiceImpl. Adjust getters/setters/builders to match your
 * actual classes if they differ (e.g. if entities use Lombok @Builder
 * instead of plain setters).
 */
@ExtendWith(MockitoExtension.class)
class DonationEventServiceImplTest {

    @Mock
    private DonationEventRepository donationEventRepository;

    @Mock
    private DonationEventMapper donationEventMapper;

    @Mock
    private OrganizationDetailRepository organizationDetailRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private DonationEventServiceImpl donationEventService;

    private UUID donationEventId;
    private UUID orgId;
    private DonationEvent donationEvent;
    private OrganizationDetail organizationDetail;
    private DonationEventReq req;
    private DonationEventLogRes logRes;
    private DonationEventRes res;

    @BeforeEach
    void setUp() {
        donationEventId = UUID.randomUUID();
        orgId = UUID.randomUUID();

        organizationDetail = new OrganizationDetail();

        donationEvent = new DonationEvent();
        donationEvent.setOrganizationDetail(organizationDetail);
        donationEvent.setStatus(EventStatus.UPCOMING);

        req = new DonationEventReq();

        logRes = new DonationEventLogRes();
        res = new DonationEventRes();
    }

    // ------------------------------------------------------------------
    // createDonationEvent
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("createDonationEvent")
    class CreateDonationEvent {

        @Test
        @DisplayName("Tạo sự kiện thành công khi org tồn tại")
        void createSuccess() {
            when(donationEventMapper.toEntity(req)).thenReturn(donationEvent);
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.of(organizationDetail));
            when(donationEventMapper.toResponseLog(donationEvent)).thenReturn(logRes);

            DonationEventLogRes result = donationEventService.createDonationEvent(req, orgId, userDetails);

            assertNotNull(result);
            assertEquals(organizationDetail, donationEvent.getOrganizationDetail());
            verify(donationEventRepository).save(donationEvent);
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy organization")
        void createThrowsWhenOrgNotFound() {
            when(donationEventMapper.toEntity(req)).thenReturn(donationEvent);
            when(organizationDetailRepository.findById(orgId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> donationEventService.createDonationEvent(req, orgId, userDetails));
            verify(donationEventRepository, never()).save(any());
        }
    }

    // ------------------------------------------------------------------
    // updateDonationEvent
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("updateDonationEvent")
    class UpdateDonationEvent {

        @Test
        @DisplayName("Cập nhật thành công tất cả field khi có giá trị")
        void updateSuccessAllFields() {
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
            when(donationEventMapper.toResponseLog(donationEvent)).thenReturn(logRes);

            DonationEventReq updateReq = new DonationEventReq();
            updateReq.setTitle("New Title");
            updateReq.setDescription("New Description");
            updateReq.setStartDate(OffsetDateTime.now());
            updateReq.setEndDate(OffsetDateTime.now().plusDays(1));
            updateReq.setLatitude(BigDecimal.valueOf(10.0));
            updateReq.setLongitude(BigDecimal.valueOf(20.0));
            updateReq.setAcceptedTypes(List.of("clothes", "books"));
            updateReq.setTargetQuantity(100);
            updateReq.setStatus(EventStatus.ONGOING);
            updateReq.setBannerUrl("https://example.com/banner.png");

            DonationEventLogRes result = donationEventService.updateDonationEvent(donationEventId, updateReq, userDetails);

            assertNotNull(result);
            assertEquals("New Title", donationEvent.getTitle());
            assertEquals("New Description", donationEvent.getDescription());
            assertEquals(BigDecimal.valueOf(10.0), donationEvent.getLatitude());
            assertEquals(BigDecimal.valueOf(20.0), donationEvent.getLongitude());
            assertEquals(List.of("clothes", "books"), donationEvent.getAcceptedTypes());
            assertEquals(100, donationEvent.getTargetQuantity());
            assertEquals(EventStatus.ONGOING, donationEvent.getStatus());
            assertEquals("https://example.com/banner.png", donationEvent.getBannerUrl());
            // acceptedTypes không rỗng -> đồng bộ sang organizationDetail
            assertEquals(List.of("clothes", "books"), organizationDetail.getAcceptedTypes());
            verify(donationEventRepository).save(donationEvent);
        }

        @Test
        @DisplayName("Không cập nhật field nào khi request rỗng (tất cả null)")
        void updateSkipsAllWhenFieldsNull() {
            String originalTitle = "Original";
            donationEvent.setTitle(originalTitle);
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
            when(donationEventMapper.toResponseLog(donationEvent)).thenReturn(logRes);

            DonationEventReq emptyReq = new DonationEventReq();

            donationEventService.updateDonationEvent(donationEventId, emptyReq, userDetails);

            assertEquals(originalTitle, donationEvent.getTitle());
            // acceptedTypes của organizationDetail không bị thay đổi (giữ nguyên giá trị khởi tạo mặc định)
            assertTrue(organizationDetail.getAcceptedTypes() == null
                    || organizationDetail.getAcceptedTypes().isEmpty());
        }

        @Test
        @DisplayName("Không đồng bộ acceptedTypes sang organizationDetail khi list rỗng")
        void updateDoesNotSyncOrgAcceptedTypesWhenEmpty() {
            organizationDetail.setAcceptedTypes(List.of("old-type"));
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
            when(donationEventMapper.toResponseLog(donationEvent)).thenReturn(logRes);

            DonationEventReq updateReq = new DonationEventReq();
            updateReq.setAcceptedTypes(List.of());

            donationEventService.updateDonationEvent(donationEventId, updateReq, userDetails);

            // acceptedTypes trên donationEvent vẫn được set (vì != null)
            assertEquals(List.of(), donationEvent.getAcceptedTypes());
            // nhưng organizationDetail giữ nguyên vì list rỗng
            assertEquals(List.of("old-type"), organizationDetail.getAcceptedTypes());
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy donation event")
        void updateThrowsWhenNotFound() {
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> donationEventService.updateDonationEvent(donationEventId, req, userDetails));
            verify(donationEventRepository, never()).save(any());
        }
    }

    // ------------------------------------------------------------------
    // deleteDonationEvent
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("deleteDonationEvent")
    class DeleteDonationEvent {

        @Test
        @DisplayName("Xóa thành công khi tìm thấy")
        void deleteSuccess() {
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
            when(donationEventMapper.toResponseLog(donationEvent)).thenReturn(logRes);

            DonationEventLogRes result = donationEventService.deleteDonationEvent(donationEventId, userDetails);

            assertNotNull(result);
            verify(donationEventRepository).delete(donationEvent);
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy donation event")
        void deleteThrowsWhenNotFound() {
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> donationEventService.deleteDonationEvent(donationEventId, userDetails));
            verify(donationEventRepository, never()).delete(any());
        }
    }

    // ------------------------------------------------------------------
    // getAllDonationEvents
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("getAllDonationEvents")
    class GetAllDonationEvents {

        @Test
        @DisplayName("Trả về danh sách tất cả sự kiện")
        void getAllSuccess() {
            when(donationEventRepository.findAll()).thenReturn(List.of(donationEvent));
            when(donationEventMapper.toResponse(donationEvent)).thenReturn(res);

            List<DonationEventRes> result = donationEventService.getAllDonationEvents();

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Trả về danh sách rỗng khi không có sự kiện nào")
        void getAllEmpty() {
            when(donationEventRepository.findAll()).thenReturn(List.of());

            List<DonationEventRes> result = donationEventService.getAllDonationEvents();

            assertTrue(result.isEmpty());
        }
    }

    // ------------------------------------------------------------------
    // getAllByOrgId
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("getAllByOrgId")
    class GetAllByOrgId {

        @Test
        @DisplayName("Trả về danh sách sự kiện theo orgId")
        void getAllByOrgIdSuccess() {
            when(donationEventRepository.findByOrganizationDetail_Id(orgId)).thenReturn(List.of(donationEvent));
            when(donationEventMapper.toResponse(donationEvent)).thenReturn(res);

            List<DonationEventRes> result = donationEventService.getAllByOrgId(orgId);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Trả về danh sách rỗng khi org không có sự kiện nào")
        void getAllByOrgIdEmpty() {
            when(donationEventRepository.findByOrganizationDetail_Id(orgId)).thenReturn(List.of());

            List<DonationEventRes> result = donationEventService.getAllByOrgId(orgId);

            assertTrue(result.isEmpty());
        }
    }

    // ------------------------------------------------------------------
    // getAllByFilter
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("getAllByFilter")
    class GetAllByFilter {

        private DonationEvent makeEvent(String location, List<String> acceptedTypes,
                                        BigDecimal lat, BigDecimal lng) {
            DonationEvent e = new DonationEvent();
            e.setLocation(location);
            e.setAcceptedTypes(acceptedTypes);
            e.setLatitude(lat);
            e.setLongitude(lng);
            return e;
        }

        // ---- validateDistanceFilter branches ----

        @Test
        @DisplayName("Không ném lỗi khi không truyền field khoảng cách nào")
        void filterNoDistanceFieldsNoError() {
            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            when(donationEventRepository.findAll()).thenReturn(List.of());

            assertDoesNotThrow(() -> donationEventService.getAllByFilter(filterReq));
        }

        @Test
        @DisplayName("Ném lỗi khi chỉ truyền latitude mà thiếu longitude/maxDistanceKm")
        void filterThrowsWhenOnlyLatitudeProvided() {
            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            filterReq.setLatitude(BigDecimal.valueOf(10.0));

            assertThrows(BadRequestException.class,
                    () -> donationEventService.getAllByFilter(filterReq));
            verifyNoInteractions(donationEventRepository);
        }

        @Test
        @DisplayName("Ném lỗi khi chỉ truyền longitude mà thiếu latitude/maxDistanceKm")
        void filterThrowsWhenOnlyLongitudeProvided() {
            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            filterReq.setLongitude(BigDecimal.valueOf(20.0));

            assertThrows(BadRequestException.class,
                    () -> donationEventService.getAllByFilter(filterReq));
        }

        @Test
        @DisplayName("Ném lỗi khi chỉ truyền maxDistanceKm mà thiếu lat/lng")
        void filterThrowsWhenOnlyMaxDistanceProvided() {
            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            filterReq.setMaxDistanceKm(5.0);

            assertThrows(BadRequestException.class,
                    () -> donationEventService.getAllByFilter(filterReq));
        }

        @Test
        @DisplayName("Không ném lỗi khi truyền đủ cả 3 field khoảng cách")
        void filterNoErrorWhenAllDistanceFieldsProvided() {
            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            filterReq.setLatitude(BigDecimal.valueOf(10.0));
            filterReq.setLongitude(BigDecimal.valueOf(20.0));
            filterReq.setMaxDistanceKm(100.0);
            when(donationEventRepository.findAll()).thenReturn(List.of());

            assertDoesNotThrow(() -> donationEventService.getAllByFilter(filterReq));
        }

        @Test
        @DisplayName("Ném lỗi khi có latitude và longitude nhưng thiếu maxDistanceKm")
        void filterThrowsWhenLatAndLngProvidedButMaxMissing() {
            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            filterReq.setLatitude(BigDecimal.valueOf(10.0));
            filterReq.setLongitude(BigDecimal.valueOf(20.0));

            assertThrows(BadRequestException.class,
                    () -> donationEventService.getAllByFilter(filterReq));
            verifyNoInteractions(donationEventRepository);
        }

        // ---- itemType filter branches ----

        @Test
        @DisplayName("Không lọc theo itemType khi null")
        void filterItemTypeNullReturnsAll() {
            DonationEvent event = makeEvent(null, null, null, null);
            when(donationEventRepository.findAll()).thenReturn(List.of(event));
            when(donationEventMapper.toResponse(event)).thenReturn(res);

            DonationEventFilterReq filterReq = new DonationEventFilterReq();

            List<DonationEventRes> result = donationEventService.getAllByFilter(filterReq);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Không lọc theo itemType khi rỗng (blank)")
        void filterItemTypeBlankReturnsAll() {
            DonationEvent event = makeEvent(null, null, null, null);
            when(donationEventRepository.findAll()).thenReturn(List.of(event));
            when(donationEventMapper.toResponse(event)).thenReturn(res);

            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            filterReq.setItemType("   ");

            List<DonationEventRes> result = donationEventService.getAllByFilter(filterReq);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Lọc đúng event khi acceptedTypes chứa itemType")
        void filterItemTypeMatches() {
            DonationEvent event = makeEvent(null, List.of("clothes", "books"), null, null);
            when(donationEventRepository.findAll()).thenReturn(List.of(event));
            when(donationEventMapper.toResponse(event)).thenReturn(res);

            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            filterReq.setItemType("books");

            List<DonationEventRes> result = donationEventService.getAllByFilter(filterReq);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Loại bỏ event khi acceptedTypes null")
        void filterItemTypeExcludesWhenAcceptedTypesNull() {
            DonationEvent event = makeEvent(null, null, null, null);
            when(donationEventRepository.findAll()).thenReturn(List.of(event));

            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            filterReq.setItemType("books");

            List<DonationEventRes> result = donationEventService.getAllByFilter(filterReq);

            assertTrue(result.isEmpty());
            verify(donationEventMapper, never()).toResponse(any());
        }

        @Test
        @DisplayName("Loại bỏ event khi acceptedTypes không chứa itemType")
        void filterItemTypeExcludesWhenNoMatch() {
            DonationEvent event = makeEvent(null, List.of("books"), null, null);
            when(donationEventRepository.findAll()).thenReturn(List.of(event));

            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            filterReq.setItemType("clothes");

            List<DonationEventRes> result = donationEventService.getAllByFilter(filterReq);

            assertTrue(result.isEmpty());
        }

        // ---- city filter branches ----

        @Test
        @DisplayName("Không lọc theo city khi null")
        void filterCityNullReturnsAll() {
            DonationEvent event = makeEvent(null, null, null, null);
            when(donationEventRepository.findAll()).thenReturn(List.of(event));
            when(donationEventMapper.toResponse(event)).thenReturn(res);

            DonationEventFilterReq filterReq = new DonationEventFilterReq();

            List<DonationEventRes> result = donationEventService.getAllByFilter(filterReq);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Không lọc theo city khi rỗng (blank)")
        void filterCityBlankReturnsAll() {
            DonationEvent event = makeEvent(null, null, null, null);
            when(donationEventRepository.findAll()).thenReturn(List.of(event));
            when(donationEventMapper.toResponse(event)).thenReturn(res);

            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            filterReq.setCity("  ");

            List<DonationEventRes> result = donationEventService.getAllByFilter(filterReq);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Lọc đúng event khi location chứa city (không phân biệt hoa thường)")
        void filterCityMatchesCaseInsensitive() {
            DonationEvent event = makeEvent("123 Nguyen Van Cu, Ho Chi Minh City", null, null, null);
            when(donationEventRepository.findAll()).thenReturn(List.of(event));
            when(donationEventMapper.toResponse(event)).thenReturn(res);

            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            filterReq.setCity("ho chi minh");

            List<DonationEventRes> result = donationEventService.getAllByFilter(filterReq);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Loại bỏ event khi location null")
        void filterCityExcludesWhenLocationNull() {
            DonationEvent event = makeEvent(null, null, null, null);
            when(donationEventRepository.findAll()).thenReturn(List.of(event));

            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            filterReq.setCity("Hanoi");

            List<DonationEventRes> result = donationEventService.getAllByFilter(filterReq);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Loại bỏ event khi location không chứa city")
        void filterCityExcludesWhenNoMatch() {
            DonationEvent event = makeEvent("Da Nang", null, null, null);
            when(donationEventRepository.findAll()).thenReturn(List.of(event));

            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            filterReq.setCity("Hanoi");

            List<DonationEventRes> result = donationEventService.getAllByFilter(filterReq);

            assertTrue(result.isEmpty());
        }

        // ---- distance filter branches ----

        @Test
        @DisplayName("Bỏ qua lọc khoảng cách khi không truyền field khoảng cách nào")
        void filterDistanceSkippedWhenNoDistanceFields() {
            DonationEvent event = makeEvent(null, null, null, null);
            when(donationEventRepository.findAll()).thenReturn(List.of(event));
            when(donationEventMapper.toResponse(event)).thenReturn(res);

            DonationEventFilterReq filterReq = new DonationEventFilterReq();

            List<DonationEventRes> result = donationEventService.getAllByFilter(filterReq);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Loại bỏ event khi event thiếu latitude/longitude")
        void filterDistanceExcludesWhenEventLatLngNull() {
            DonationEvent event = makeEvent(null, null, null, null);
            when(donationEventRepository.findAll()).thenReturn(List.of(event));

            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            filterReq.setLatitude(BigDecimal.valueOf(10.0));
            filterReq.setLongitude(BigDecimal.valueOf(20.0));
            filterReq.setMaxDistanceKm(50.0);

            List<DonationEventRes> result = donationEventService.getAllByFilter(filterReq);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Loại bỏ event khi event có latitude nhưng thiếu longitude")
        void filterDistanceExcludesWhenEventLongitudeNull() {
            DonationEvent event = makeEvent(null, null, BigDecimal.valueOf(10.0), null);
            when(donationEventRepository.findAll()).thenReturn(List.of(event));

            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            filterReq.setLatitude(BigDecimal.valueOf(10.0));
            filterReq.setLongitude(BigDecimal.valueOf(20.0));
            filterReq.setMaxDistanceKm(50.0);

            List<DonationEventRes> result = donationEventService.getAllByFilter(filterReq);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Giữ lại event khi khoảng cách nằm trong bán kính cho phép")
        void filterDistanceIncludesWhenWithinRadius() {
            // Cùng tọa độ -> khoảng cách = 0
            DonationEvent event = makeEvent(null, null, BigDecimal.valueOf(10.7769), BigDecimal.valueOf(106.7009));
            when(donationEventRepository.findAll()).thenReturn(List.of(event));
            when(donationEventMapper.toResponse(event)).thenReturn(res);

            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            filterReq.setLatitude(BigDecimal.valueOf(10.7769));
            filterReq.setLongitude(BigDecimal.valueOf(106.7009));
            filterReq.setMaxDistanceKm(10.0);

            List<DonationEventRes> result = donationEventService.getAllByFilter(filterReq);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Loại bỏ event khi khoảng cách vượt quá bán kính cho phép")
        void filterDistanceExcludesWhenBeyondRadius() {
            // HCMC vs Hanoi, cách nhau ~1100km, maxDistanceKm rất nhỏ
            DonationEvent event = makeEvent(null, null, BigDecimal.valueOf(21.0285), BigDecimal.valueOf(105.8542));
            when(donationEventRepository.findAll()).thenReturn(List.of(event));

            DonationEventFilterReq filterReq = new DonationEventFilterReq();
            filterReq.setLatitude(BigDecimal.valueOf(10.7769));
            filterReq.setLongitude(BigDecimal.valueOf(106.7009));
            filterReq.setMaxDistanceKm(10.0);

            List<DonationEventRes> result = donationEventService.getAllByFilter(filterReq);

            assertTrue(result.isEmpty());
        }
    }

    // ------------------------------------------------------------------
    // cancelDonationEvent
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("cancelDonationEvent")
    class CancelDonationEvent {

        @Test
        @DisplayName("Hủy thành công khi trạng thái là UPCOMING")
        void cancelSuccessFromUpcoming() {
            donationEvent.setStatus(EventStatus.UPCOMING);
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
            when(donationEventMapper.toResponseLog(donationEvent)).thenReturn(logRes);

            DonationEventLogRes result = donationEventService.cancelDonationEvent(donationEventId, userDetails);

            assertNotNull(result);
            assertEquals(EventStatus.CANCELLED, donationEvent.getStatus());
            verify(donationEventRepository).save(donationEvent);
        }

        @Test
        @DisplayName("Hủy thành công khi trạng thái là ONGOING")
        void cancelSuccessFromOngoing() {
            donationEvent.setStatus(EventStatus.ONGOING);
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
            when(donationEventMapper.toResponseLog(donationEvent)).thenReturn(logRes);

            donationEventService.cancelDonationEvent(donationEventId, userDetails);

            assertEquals(EventStatus.CANCELLED, donationEvent.getStatus());
        }

        @Test
        @DisplayName("Ném lỗi khi sự kiện đã COMPLETED")
        void cancelThrowsWhenCompleted() {
            donationEvent.setStatus(EventStatus.COMPLETED);
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));

            assertThrows(IllegalStateException.class,
                    () -> donationEventService.cancelDonationEvent(donationEventId, userDetails));
            verify(donationEventRepository, never()).save(any());
        }

        @Test
        @DisplayName("Ném lỗi khi sự kiện đã CANCELLED")
        void cancelThrowsWhenAlreadyCancelled() {
            donationEvent.setStatus(EventStatus.CANCELLED);
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));

            assertThrows(IllegalStateException.class,
                    () -> donationEventService.cancelDonationEvent(donationEventId, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy sự kiện")
        void cancelThrowsWhenNotFound() {
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> donationEventService.cancelDonationEvent(donationEventId, userDetails));
        }
    }

    // ------------------------------------------------------------------
    // completeDonationEvent
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("completeDonationEvent")
    class CompleteDonationEvent {

        @Test
        @DisplayName("Hoàn thành thành công khi trạng thái là ONGOING")
        void completeSuccess() {
            donationEvent.setStatus(EventStatus.ONGOING);
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
            when(donationEventMapper.toResponseLog(donationEvent)).thenReturn(logRes);

            DonationEventLogRes result = donationEventService.completeDonationEvent(donationEventId, userDetails);

            assertNotNull(result);
            assertEquals(EventStatus.COMPLETED, donationEvent.getStatus());
            verify(donationEventRepository).save(donationEvent);
        }

        @Test
        @DisplayName("Ném lỗi khi trạng thái không phải ONGOING (UPCOMING)")
        void completeThrowsWhenUpcoming() {
            donationEvent.setStatus(EventStatus.UPCOMING);
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));

            assertThrows(IllegalStateException.class,
                    () -> donationEventService.completeDonationEvent(donationEventId, userDetails));
            verify(donationEventRepository, never()).save(any());
        }

        @Test
        @DisplayName("Ném lỗi khi trạng thái không phải ONGOING (CANCELLED)")
        void completeThrowsWhenCancelled() {
            donationEvent.setStatus(EventStatus.CANCELLED);
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));

            assertThrows(IllegalStateException.class,
                    () -> donationEventService.completeDonationEvent(donationEventId, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy sự kiện")
        void completeThrowsWhenNotFound() {
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> donationEventService.completeDonationEvent(donationEventId, userDetails));
        }
    }

    // ------------------------------------------------------------------
    // ongoingDonationEvent
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("ongoingDonationEvent")
    class OngoingDonationEvent {

        @Test
        @DisplayName("Chuyển sang ONGOING thành công khi trạng thái là UPCOMING")
        void ongoingSuccess() {
            donationEvent.setStatus(EventStatus.UPCOMING);
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));
            when(donationEventMapper.toResponseLog(donationEvent)).thenReturn(logRes);

            DonationEventLogRes result = donationEventService.ongoingDonationEvent(donationEventId, userDetails);

            assertNotNull(result);
            assertEquals(EventStatus.ONGOING, donationEvent.getStatus());
            verify(donationEventRepository).save(donationEvent);
        }

        @Test
        @DisplayName("Ném lỗi khi trạng thái không phải UPCOMING (ONGOING)")
        void ongoingThrowsWhenAlreadyOngoing() {
            donationEvent.setStatus(EventStatus.ONGOING);
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));

            assertThrows(IllegalStateException.class,
                    () -> donationEventService.ongoingDonationEvent(donationEventId, userDetails));
            verify(donationEventRepository, never()).save(any());
        }

        @Test
        @DisplayName("Ném lỗi khi trạng thái không phải UPCOMING (COMPLETED)")
        void ongoingThrowsWhenCompleted() {
            donationEvent.setStatus(EventStatus.COMPLETED);
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.of(donationEvent));

            assertThrows(IllegalStateException.class,
                    () -> donationEventService.ongoingDonationEvent(donationEventId, userDetails));
        }

        @Test
        @DisplayName("Ném lỗi khi không tìm thấy sự kiện")
        void ongoingThrowsWhenNotFound() {
            when(donationEventRepository.findById(donationEventId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> donationEventService.ongoingDonationEvent(donationEventId, userDetails));
        }
    }
}