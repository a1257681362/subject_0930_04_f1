package com.evops.pipe;

/**
 * 管网台账状态常量。
 */
public final class PipeConstants {

    private PipeConstants() {
    }

    /** 管网权限模式：正常 */
    public static final String MODE_NORMAL = "NORMAL";
    /** 管网权限模式：收紧 */
    public static final String MODE_TIGHTENED = "TIGHTENED";

    /** 井室确认状态：未确认 */
    public static final String CONFIRM_UNCONFIRMED = "UNCONFIRMED";
    /** 井室确认状态：已确认（已确认事实不可篡改） */
    public static final String CONFIRM_CONFIRMED = "CONFIRMED";

    /** 工单状态：已派检 */
    public static final String ORDER_DISPATCHED = "DISPATCHED";
    /** 工单状态：现场记录已提交 */
    public static final String ORDER_FIELD_RECORDED = "FIELD_RECORDED";
    /** 工单状态：复核闭环 */
    public static final String ORDER_CLOSED = "CLOSED";

    /** 隐患状态：已登记 */
    public static final String HAZARD_REGISTERED = "REGISTERED";
    /** 隐患状态：复核通过 */
    public static final String HAZARD_REVIEW_PASS = "REVIEW_PASS";
    /** 隐患状态：复核退回 */
    public static final String HAZARD_REVIEW_REJECT = "REVIEW_REJECT";

    /** 管段档案状态：已建档 */
    public static final String SEGMENT_FILED = "FILED";
}
