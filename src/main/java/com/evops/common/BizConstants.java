package com.evops.common;

/** 巡检台账业务常量 */
public final class BizConstants {

    private BizConstants() {}

    /** 巡检对象类型 */
    public static final String TARGET_PIPE = "PIPE";
    public static final String TARGET_MANHOLE = "MANHOLE";

    /** 档案状态：草稿 / 已确认 */
    public static final String ARCHIVE_DRAFT = "DRAFT";
    public static final String ARCHIVE_CONFIRMED = "CONFIRMED";

    /** 工单状态：已派检 / 巡检中 / 已完成 */
    public static final String ORDER_ASSIGNED = "ASSIGNED";
    public static final String ORDER_IN_PROGRESS = "IN_PROGRESS";
    public static final String ORDER_COMPLETED = "COMPLETED";

    /** 隐患状态：已登记 / 已整改待复核 / 复核通过闭环 / 复核驳回重新整改 */
    public static final String HAZARD_REGISTERED = "REGISTERED";
    public static final String HAZARD_RECTIFIED = "RECTIFIED";
    public static final String HAZARD_REVIEWED = "REVIEWED";
    public static final String HAZARD_REJECTED = "REJECTED";

    /** 隐患来源：现场记录转登记 / 人工登记 */
    public static final String SOURCE_FIELD = "FIELD";
    public static final String SOURCE_MANUAL = "MANUAL";

    /** 系统配置键：管网权限收紧 */
    public static final String CFG_PERMISSION_RESTRICTED = "network.permission.restricted";
}
