package com.loveratory.experiment.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 報名表單欄位設定。
 * 存於 Experiment 的 form_config JSONB 欄位中，定義受試者報名時需填寫的欄位。
 */
@Getter
@Setter
@Schema(description = "報名表單欄位設定")
public class FormConfig {

    @Schema(description = "表單欄位列表")
    private List<FormField> fields;

    /**
     * 表單欄位。
     * 定義單一報名表單欄位的顯示與必填屬性。
     */
    @Getter
    @Setter
    @Schema(description = "表單欄位")
    public static class FormField {

        @Schema(description = "欄位代碼", example = "email")
        private String key;

        @Schema(description = "欄位標籤", example = "Email")
        private String label;

        @Schema(description = "是否顯示")
        private boolean visible;

        @Schema(description = "是否必填")
        private boolean required;

        @Schema(description = "是否鎖定（不可更改）")
        private Boolean locked;
    }

    /**
     * 建立預設表單設定。
     * Email 必填鎖定，姓名必填，手機選填，備註選填，其餘隱藏。
     *
     * @return 預設表單設定
     */
    public static FormConfig createDefault() {
        FormConfig config = new FormConfig();
        config.setFields(List.of(
                createField("email", "Email", true, true, true),
                createField("name", "姓名", true, true, null),
                createField("phone", "手機", true, false, null),
                createField("student_id", "學號", false, false, null),
                createField("age", "年齡", false, false, null),
                createField("gender", "性別", false, false, null),
                createField("dominant_hand", "慣用手", false, false, null),
                createField("notes", "備註", true, false, null)
        ));
        return config;
    }

    private static FormField createField(String key, String label, boolean visible,
                                         boolean required, Boolean locked) {
        FormField field = new FormField();
        field.setKey(key);
        field.setLabel(label);
        field.setVisible(visible);
        field.setRequired(required);
        field.setLocked(locked);
        return field;
    }
}
