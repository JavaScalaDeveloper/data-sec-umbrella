package com.arelore.data.sec.umbrella.server.core.enums;

import org.springframework.util.StringUtils;

/**
 * MySQL 库/表资产的人工打标枚举：用于覆盖自动识别结果，未设置（空）表示沿用系统默认、不参与人工结论。
 *
 * @author 黄佳豪
 */
public enum ManualReviewLabelEnum {

    /**
     * 忽略：人工确认无需按敏感处理
     */
    IGNORE("IGNORE", "忽略"),

    /**
     * 误报：自动识别有误
     */
    FALSE_POSITIVE("FALSE_POSITIVE", "误报"),

    /**
     * 敏感：人工确认为敏感
     */
    SENSITIVE("SENSITIVE", "敏感");

    private final String code;
    private final String labelZh;

    ManualReviewLabelEnum(String code, String labelZh) {
        this.code = code;
        this.labelZh = labelZh;
    }

    public String getCode() {
        return code;
    }

    public String getLabelZh() {
        return labelZh;
    }

    /**
     * 将请求中的字符串规范为库中存储的 code；空串视为清空（返回 null）。
     *
     * @param raw 前端或调用方传入的取值
     * @return 标准 code，或 null 表示恢复为默认（未打标）
     * @throws IllegalArgumentException 非空且无法识别时
     */
    public static String normalizeToStoredValue(String raw) {
        if (raw == null || !StringUtils.hasText(raw.trim())) {
            return null;
        }
        String t = raw.trim();
        for (ManualReviewLabelEnum e : values()) {
            if (e.code.equalsIgnoreCase(t)) {
                return e.code;
            }
        }
        throw new IllegalArgumentException("无效的人工打标取值: " + raw);
    }
}
