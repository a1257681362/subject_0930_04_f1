package com.evops.pipe;

import com.evops.pipe.dto.DispatchRequest;
import com.evops.pipe.dto.HazardRegisterRequest;
import com.evops.pipe.dto.RecordRequest;
import com.evops.pipe.dto.ReviewRequest;
import com.evops.pipe.dto.SegmentSaveRequest;
import com.evops.pipe.dto.WellSaveRequest;
import com.evops.pipe.entity.Hazard;
import com.evops.pipe.entity.InspectOrder;
import com.evops.pipe.entity.InspectRecord;
import com.evops.pipe.entity.PipeSegment;
import com.evops.pipe.entity.PipeWell;
import com.evops.pipe.service.HazardService;
import com.evops.pipe.service.InspectOrderService;
import com.evops.pipe.service.NetworkPermissionService;
import com.evops.pipe.service.PipeSegmentService;
import com.evops.pipe.service.PipeWellService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 管网巡检台账闭环：建档 -> 派检 -> 现场记录 -> 隐患登记 -> 复核。
 * 另验证：编号唯一、历史位置只增不删、权限收紧时管段停推/井室保留已确认事实。
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:pipetest;DB_CLOSE_DELAY=-1",
        "spring.sql.init.mode=always"
})
class PipeClosedLoopTest {

    @Autowired
    private PipeSegmentService segmentService;
    @Autowired
    private PipeWellService wellService;
    @Autowired
    private InspectOrderService orderService;
    @Autowired
    private HazardService hazardService;
    @Autowired
    private NetworkPermissionService permissionService;

    private final String suffix = Long.toString(System.nanoTime());

    @BeforeEach
    void resetPermission() {
        permissionService.restore("测试前重置为正常模式");
    }

    private SegmentSaveRequest segmentReq(String no) {
        SegmentSaveRequest req = new SegmentSaveRequest();
        req.setSegmentNo(no);
        req.setMaterial("球墨铸铁");
        req.setDiameterMm(600);
        req.setBuryDepthM(new BigDecimal("2.50"));
        req.setRoadName("人民路");
        return req;
    }

    private WellSaveRequest wellReq(String no, Long segmentId, String lng, String lat) {
        WellSaveRequest req = new WellSaveRequest();
        req.setWellNo(no);
        req.setSegmentId(segmentId);
        req.setCoordLng(new BigDecimal(lng));
        req.setCoordLat(new BigDecimal(lat));
        req.setCoverType("重型铸铁井盖");
        req.setRiskLevel("MEDIUM");
        return req;
    }

    @Test
    void closedLoopAndArchiveRules() {
        // 1. 建档：管段 + 井室
        PipeSegment segment = segmentService.create(segmentReq("SEG-" + suffix));
        PipeWell well = wellService.create(wellReq("WELL-" + suffix, segment.getId(), "120.123456", "30.123456"));
        assertEquals(PipeConstants.SEGMENT_FILED, segment.getStatus());
        assertEquals(PipeConstants.CONFIRM_UNCONFIRMED, well.getConfirmStatus());

        // 编号唯一：重复建档被拒绝
        assertThrows(IllegalArgumentException.class, () -> segmentService.create(segmentReq("SEG-" + suffix)));
        assertThrows(IllegalArgumentException.class,
                () -> wellService.create(wellReq("WELL-" + suffix, null, "120.0", "30.0")));

        // 2. 派检：人员 + 路线
        DispatchRequest dispatch = new DispatchRequest();
        dispatch.setInspectorName("张三");
        dispatch.setRouteDesc("人民路沿线井室");
        DispatchRequest.RouteItem stop = new DispatchRequest.RouteItem();
        stop.setWellId(well.getId());
        dispatch.setRoute(Collections.singletonList(stop));
        InspectOrder order = orderService.dispatch(dispatch);
        assertEquals(PipeConstants.ORDER_DISPATCHED, order.getStatus());

        // 3. 现场记录：发现问题，标记隐患
        RecordRequest recordReq = new RecordRequest();
        recordReq.setWellId(well.getId());
        recordReq.setFindingDesc("井室积水，盖板锈蚀");
        recordReq.setHasHazard(true);
        recordReq.setRecordedBy("张三");
        InspectRecord record = orderService.addRecord(order.getId(), recordReq);
        assertEquals(PipeConstants.ORDER_FIELD_RECORDED, orderService.mustGet(order.getId()).getStatus());

        // 存在待复核隐患时不能闭环
        HazardRegisterRequest hazardReq = new HazardRegisterRequest();
        hazardReq.setOrderId(order.getId());
        hazardReq.setRecordId(record.getId());
        hazardReq.setHazardDesc("盖板锈蚀存在坠落风险");
        hazardReq.setHazardLevel("MAJOR");
        Hazard hazard = hazardService.register(hazardReq);
        assertThrows(IllegalStateException.class, () -> orderService.close(order.getId()));

        // 5. 复核通过 -> 工单自动闭环
        ReviewRequest review = new ReviewRequest();
        review.setPass(true);
        review.setReviewComment("属实，纳入维修计划");
        review.setReviewedBy("李四");
        hazardService.review(hazard.getId(), review);
        assertEquals(PipeConstants.HAZARD_REVIEW_PASS, hazardService.mustGet(hazard.getId()).getStatus());
        assertEquals(PipeConstants.ORDER_CLOSED, orderService.mustGet(order.getId()).getStatus());

        // 历史位置只增不删：建档 1 条 + 修正 1 条
        WellSaveRequest moved = wellReq("WELL-" + suffix, segment.getId(), "120.654321", "30.654321");
        moved.setChangeReason("测绘修正");
        wellService.update(well.getId(), moved);
        assertEquals(2, wellService.locationHistory(well.getId()).size());

        // 确认后事实保留、不可篡改
        wellService.confirm(well.getId());
        assertThrows(IllegalStateException.class, () -> wellService.update(well.getId(), moved));
    }

    @Test
    void reviewRejectAndNoHazardClose() {
        PipeSegment segment = segmentService.create(segmentReq("SEG-R-" + suffix));
        PipeWell well = wellService.create(wellReq("WELL-R-" + suffix, segment.getId(), "121.0", "31.0"));

        // 复核退回 -> 工单退回待重检
        DispatchRequest dispatch = new DispatchRequest();
        dispatch.setInspectorName("王五");
        DispatchRequest.RouteItem stop = new DispatchRequest.RouteItem();
        stop.setWellId(well.getId());
        dispatch.setRoute(Collections.singletonList(stop));
        InspectOrder order = orderService.dispatch(dispatch);

        RecordRequest recordReq = new RecordRequest();
        recordReq.setWellId(well.getId());
        recordReq.setFindingDesc("井壁渗漏");
        recordReq.setHasHazard(true);
        recordReq.setRecordedBy("王五");
        InspectRecord record = orderService.addRecord(order.getId(), recordReq);

        HazardRegisterRequest hazardReq = new HazardRegisterRequest();
        hazardReq.setOrderId(order.getId());
        hazardReq.setRecordId(record.getId());
        hazardReq.setHazardDesc("井壁渗漏扩大风险");
        Hazard hazard = hazardService.register(hazardReq);

        ReviewRequest reject = new ReviewRequest();
        reject.setPass(false);
        reject.setReviewComment("描述不清，退回补充");
        reject.setReviewedBy("赵六");
        hazardService.review(hazard.getId(), reject);
        assertEquals(PipeConstants.HAZARD_REVIEW_REJECT, hazardService.mustGet(hazard.getId()).getStatus());
        assertEquals(PipeConstants.ORDER_DISPATCHED, orderService.mustGet(order.getId()).getStatus());

        // 无隐患工单：现场记录完成后可直接复核闭环
        DispatchRequest dispatch2 = new DispatchRequest();
        dispatch2.setInspectorName("王五");
        DispatchRequest.RouteItem stop2 = new DispatchRequest.RouteItem();
        stop2.setSegmentId(segment.getId());
        dispatch2.setRoute(Collections.singletonList(stop2));
        InspectOrder order2 = orderService.dispatch(dispatch2);

        RecordRequest clean = new RecordRequest();
        clean.setSegmentId(segment.getId());
        clean.setFindingDesc("管段外观正常");
        clean.setHasHazard(false);
        clean.setRecordedBy("王五");
        orderService.addRecord(order2.getId(), clean);
        orderService.close(order2.getId());
        assertEquals(PipeConstants.ORDER_CLOSED, orderService.mustGet(order2.getId()).getStatus());
    }

    @Test
    void tightenedPermissionStopsSegmentAndKeepsConfirmedWellFacts() {
        PipeSegment segment = segmentService.create(segmentReq("SEG-P-" + suffix));
        PipeWell well = wellService.create(wellReq("WELL-P-" + suffix, segment.getId(), "122.0", "32.0"));
        wellService.confirm(well.getId());

        // 权限收紧
        permissionService.tighten("上级保密要求");

        // 管段档案停止推进
        assertThrows(IllegalStateException.class, () -> segmentService.create(segmentReq("SEG-P2-" + suffix)));
        assertThrows(IllegalStateException.class,
                () -> segmentService.update(segment.getId(), segmentReq("SEG-P-" + suffix)));

        // 井室档案禁止变更
        assertThrows(IllegalStateException.class,
                () -> wellService.create(wellReq("WELL-P2-" + suffix, null, "122.1", "32.1")));
        assertThrows(IllegalStateException.class,
                () -> wellService.update(well.getId(), wellReq("WELL-P-" + suffix, null, "122.2", "32.2")));

        // 已确认事实保留可查，历史位置未丢失
        PipeWell kept = wellService.mustGet(well.getId());
        assertEquals(PipeConstants.CONFIRM_CONFIRMED, kept.getConfirmStatus());
        assertEquals(0, new BigDecimal("122.0").compareTo(kept.getCoordLng()));
        assertEquals(1, wellService.locationHistory(well.getId()).size());

        // 恢复正常后可继续推进
        permissionService.restore("保密期结束");
        assertDoesNotThrow(() -> segmentService.create(segmentReq("SEG-P3-" + suffix)));
    }
}
