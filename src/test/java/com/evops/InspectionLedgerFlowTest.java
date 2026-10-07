package com.evops;

import com.evops.entity.FieldRecord;
import com.evops.entity.HiddenDanger;
import com.evops.entity.InspectionOrder;
import com.evops.entity.InspectionRoute;
import com.evops.entity.Inspector;
import com.evops.entity.Manhole;
import com.evops.entity.ManholeLocationHistory;
import com.evops.entity.PipeSegment;
import com.evops.entity.RouteItem;
import com.evops.mapper.ManholeLocationHistoryMapper;
import com.evops.service.HiddenDangerService;
import com.evops.service.InspectionOrderService;
import com.evops.service.InspectionRouteService;
import com.evops.service.InspectorService;
import com.evops.service.ManholeService;
import com.evops.service.PipeSegmentService;
import com.evops.service.SysConfigService;
import com.evops.common.BizConstants;
import com.evops.common.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 城市地下管网巡检台账闭环集成测试：
 * 建档 -> 派检 -> 现场记录 -> 隐患登记 -> 整改复核闭环，
 * 并验证编号唯一、历史位置不可删除、权限收紧规则。
 */
@SpringBootTest
class InspectionLedgerFlowTest {

    @Autowired private PipeSegmentService pipeSegmentService;
    @Autowired private ManholeService manholeService;
    @Autowired private InspectorService inspectorService;
    @Autowired private InspectionRouteService routeService;
    @Autowired private InspectionOrderService orderService;
    @Autowired private HiddenDangerService hazardService;
    @Autowired private SysConfigService sysConfigService;
    @Autowired private ManholeLocationHistoryMapper historyMapper;

    private PipeSegment newPipe(String no) {
        PipeSegment pipe = new PipeSegment();
        pipe.setSegmentNo(no);
        pipe.setMaterial("球墨铸铁");
        pipe.setDiameterMm(400);
        pipe.setBurialDepthM(new BigDecimal("1.80"));
        pipe.setRoadName("人民大道");
        return pipeSegmentService.create(pipe);
    }

    private Manhole newManhole(String no, String lng, String lat) {
        Manhole manhole = new Manhole();
        manhole.setManholeNo(no);
        manhole.setLng(new BigDecimal(lng));
        manhole.setLat(new BigDecimal(lat));
        manhole.setCoverType("铸铁盖板");
        manhole.setRiskLevel("MEDIUM");
        return manholeService.create(manhole);
    }

    @Test
    void fullInspectionClosedLoop() {
        // 1. 建档：管段 + 井室
        PipeSegment pipe = newPipe("PS-LOOP-001");
        Manhole manhole = newManhole("MH-LOOP-001", "116.40400000", "39.91500000");
        pipeSegmentService.confirm(pipe.getId());
        manholeService.confirm(manhole.getId());
        assertEquals(BizConstants.ARCHIVE_CONFIRMED, pipeSegmentService.get(pipe.getId()).getStatus());
        assertEquals(BizConstants.ARCHIVE_CONFIRMED, manholeService.get(manhole.getId()).getStatus());

        // 2. 人员与路线
        Inspector inspector = new Inspector();
        inspector.setEmployeeNo("EMP-LOOP-001");
        inspector.setName("张巡");
        inspector.setPhone("13800000000");
        inspector = inspectorService.create(inspector);

        InspectionRoute route = new InspectionRoute();
        route.setRouteName("人民大道环线");
        route = routeService.create(route);
        routeService.addItem(route.getId(), BizConstants.TARGET_PIPE, pipe.getId(), 1);
        routeService.addItem(route.getId(), BizConstants.TARGET_MANHOLE, manhole.getId(), 2);
        List<RouteItem> items = routeService.listItems(route.getId());
        assertEquals(2, items.size());

        // 3. 派检
        InspectionOrder order = orderService.dispatch(route.getId(), inspector.getId(), null);
        assertNotNull(order.getOrderNo());
        assertEquals(BizConstants.ORDER_ASSIGNED, order.getStatus());

        // 4. 到场 + 现场记录（井室发现问题）
        orderService.start(order.getId());
        com.evops.entity.FieldRecord okRecord = recordField(order, pipe, "正常", "无渗漏", false);
        com.evops.entity.FieldRecord issueRecord =
                recordField(order, manhole, "盖板沉降", "井周路面下沉约3cm", true);
        assertNotNull(okRecord.getId());
        assertEquals(inspector.getId(), issueRecord.getInspectorId());

        // 未开始/已完成工单的记录约束
        InspectionOrder another = orderService.dispatch(route.getId(), inspector.getId(), null);
        assertThrows(BusinessException.class, () ->
                recordField(another, pipe, "x", "y", false));

        orderService.finish(order.getId());
        assertEquals(BizConstants.ORDER_COMPLETED, orderService.get(order.getId()).getStatus());
        assertThrows(BusinessException.class, () ->
                recordField(order, manhole, "x", "y", true));

        // 5. 现场问题转隐患登记
        HiddenDanger danger = hazardService.registerFromFieldRecord(issueRecord.getId(), "HIGH");
        assertEquals(BizConstants.HAZARD_REGISTERED, danger.getStatus());
        assertEquals(BizConstants.SOURCE_FIELD, danger.getSourceType());

        // 无问题记录不能转隐患
        Long finalOkRecordId = okRecord.getId();
        assertThrows(BusinessException.class, () ->
                hazardService.registerFromFieldRecord(finalOkRecordId, "LOW"));

        // 6. 整改 -> 复核驳回 -> 重新整改 -> 复核通过闭环
        final Long hazardId = danger.getId();
        final Long inspectorId = inspector.getId();
        hazardService.rectify(hazardId, inspectorId, "已换盖板并注浆加固");
        assertEquals(BizConstants.HAZARD_RECTIFIED, hazardService.get(hazardId).getStatus());

        HiddenDanger rejected = hazardService.review(
                hazardId, inspectorId, false, "加固未达标，重新处理");
        assertEquals(BizConstants.HAZARD_REJECTED, rejected.getStatus());

        // 驳回后必须重新整改才能再复核
        assertThrows(BusinessException.class, () ->
                hazardService.review(hazardId, inspectorId, true, "再试"));
        hazardService.rectify(hazardId, inspectorId, "重新注浆并复测合格");
        HiddenDanger closed = hazardService.review(
                hazardId, inspectorId, true, "复核合格，予以闭环");
        assertEquals(BizConstants.HAZARD_REVIEWED, closed.getStatus());

        // 闭环后不能重复整改
        assertThrows(BusinessException.class, () ->
                hazardService.rectify(hazardId, inspectorId, "再改"));
    }

    private FieldRecord recordField(InspectionOrder order, Object target,
                                    String condition, String content, boolean hasIssue) {
        if (target instanceof PipeSegment) {
            return fieldRecord(order.getId(), BizConstants.TARGET_PIPE,
                    ((PipeSegment) target).getId(), condition, content, hasIssue);
        }
        return fieldRecord(order.getId(), BizConstants.TARGET_MANHOLE,
                ((Manhole) target).getId(), condition, content, hasIssue);
    }

    @Autowired private com.evops.service.FieldRecordService fieldRecordService;

    private FieldRecord fieldRecord(Long orderId, String type, Long targetId,
                                    String condition, String content, boolean hasIssue) {
        return fieldRecordService.record(orderId, type, targetId, condition, content, hasIssue);
    }

    @Test
    void uniqueSegmentNoAndManholeNo() {
        newPipe("PS-UNIQ-001");
        assertThrows(BusinessException.class, () -> newPipe("PS-UNIQ-001"));

        newManhole("MH-UNIQ-001", "116.10000000", "39.10000000");
        assertThrows(BusinessException.class,
                () -> newManhole("MH-UNIQ-001", "116.20000000", "39.20000000"));
    }

    @Test
    void manholeLocationHistoryIsAppendOnly() {
        Manhole manhole = newManhole("MH-HIST-001", "116.40400000", "39.91500000");
        // 修正坐标两次
        manhole.setLng(new BigDecimal("116.40410000"));
        manhole.setLat(new BigDecimal("39.91510000"));
        manholeService.update(manhole, "实测纠偏");
        manhole.setLng(new BigDecimal("116.40420000"));
        manhole.setLat(new BigDecimal("39.91520000"));
        manholeService.update(manhole, "再次纠偏");

        List<ManholeLocationHistory> history =
                manholeService.listLocationHistory(manhole.getId());
        assertEquals(3, history.size(), "建档坐标 + 两次修正应留存3条历史");
        assertEquals("116.40400000", history.get(0).getLng().toPlainString());
        assertEquals("116.40420000", history.get(2).getLng().toPlainString());

        // 应用层不提供历史删除路径；直接物理删除视为非法操作，这里验证数据仍可被追溯，
        // 且历史表中最早的位置事实依然存在（历史位置不能被删除）
        assertNotNull(historyMapper.selectById(history.get(0).getId()));
    }

    @Test
    void permissionRestrictionFreezesArchives() {
        // 正常状态下完成建档与确认
        PipeSegment pipe = newPipe("PS-LOCK-001");
        Manhole confirmedManhole = newManhole("MH-LOCK-001", "116.40400000", "39.91500000");
        manholeService.confirm(confirmedManhole.getId());
        Manhole draftManhole = newManhole("MH-LOCK-002", "116.41000000", "39.92000000");

        // 收紧权限
        sysConfigService.setPermissionRestricted(true);
        assertTrue(sysConfigService.isPermissionRestricted());
        try {
            // 管段档案停止推进：禁止新建 / 变更 / 确认
            assertThrows(BusinessException.class, () -> newPipe("PS-LOCK-002"));
            assertThrows(BusinessException.class, () ->
                    pipeSegmentService.confirm(pipe.getId()));
            pipe.setRoadName("篡改道路");
            assertThrows(BusinessException.class, () -> pipeSegmentService.update(pipe));

            // 井室已确认事实冻结：禁止变更、移位、新增确认
            confirmedManhole.setRiskLevel("HIGH");
            assertThrows(BusinessException.class, () ->
                    manholeService.update(confirmedManhole, null));
            confirmedManhole.setLng(new BigDecimal("116.99999999"));
            confirmedManhole.setLat(new BigDecimal("39.99999999"));
            assertThrows(BusinessException.class, () ->
                    manholeService.update(confirmedManhole, "非法移位"));
            assertThrows(BusinessException.class, () ->
                    manholeService.confirm(draftManhole.getId()));

            // 已确认事实原样保留
            Manhole kept = manholeService.get(confirmedManhole.getId());
            assertEquals("MEDIUM", kept.getRiskLevel());
            assertEquals("116.40400000", kept.getLng().toPlainString());
            assertEquals(BizConstants.ARCHIVE_CONFIRMED, kept.getStatus());

            // 历史位置条数不因非法移位增加
            assertEquals(1, manholeService.listLocationHistory(confirmedManhole.getId()).size());
        } finally {
            sysConfigService.setPermissionRestricted(false);
        }

        // 放开后管段可继续推进
        assertFalse(sysConfigService.isPermissionRestricted());
        pipeSegmentService.confirm(pipe.getId());
        assertEquals(BizConstants.ARCHIVE_CONFIRMED,
                pipeSegmentService.get(pipe.getId()).getStatus());
    }

    @Test
    void hazardCannotReviewBeforeRectify() {
        Manhole manhole = newManhole("MH-HAZ-001", "116.10000000", "39.10000000");
        HiddenDanger danger = hazardService.registerManually(
                BizConstants.TARGET_MANHOLE, manhole.getId(), "井盖缺失", "HIGH");
        // 未整改不能复核
        assertThrows(BusinessException.class, () ->
                hazardService.review(danger.getId(), 1L, true, "直接闭环"));
        // 人工登记来源
        assertEquals(BizConstants.SOURCE_MANUAL, danger.getSourceType());
    }
}
