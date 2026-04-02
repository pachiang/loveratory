package com.loveratory.experiment.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 通知設定。
 * 存於 Experiment 的 notification_config JSONB 欄位中，定義報名確認與實驗提醒的通知行為。
 */
@Getter
@Setter
@Schema(description = "通知設定")
public class NotificationConfig {

    @Schema(description = "是否啟用通知")
    private boolean enabled;

    @Schema(description = "報名成功時發送確認信")
    private boolean onRegistration;

    @Schema(description = "實驗提醒天數列表（實驗前 N 天）", example = "[1, 2]")
    private List<Integer> reminders;

    /**
     * 建立預設通知設定。
     * 預設啟用通知、報名時發送確認信、實驗前 1 天提醒。
     *
     * @return 預設通知設定
     */
    public static NotificationConfig createDefault() {
        NotificationConfig config = new NotificationConfig();
        config.setEnabled(true);
        config.setOnRegistration(true);
        config.setReminders(List.of(1));
        return config;
    }
}
