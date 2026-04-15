import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';

import { ExperimentService } from '../../../core/services/experiment.service';
import {
  ExperimentDetailResponse,
  ExperimentStatus,
  FormField,
  NotificationConfig,
} from '../../../core/models';

@Component({
  selector: 'app-experiment-detail',
  imports: [ReactiveFormsModule, DatePipe],
  styleUrl: './experiment-detail.scss',
  template: `
    @if (loading()) {
      <div class="loading-center"><span class="spinner"></span></div>
    } @else if (experiment(); as exp) {
      <div class="tabs">
        <button class="tab active">實驗設定</button>
        <button class="tab" (click)="navigateToSlots()">時段管理</button>
        <button class="tab" (click)="navigateToRegistrations()">報名管理</button>
      </div>

      <div class="experiment-header">
        <div class="title-area">
          <h1>
            {{ exp.name }}
            <span class="badge" [class]="statusBadgeClass()">{{ exp.status }}</span>
          </h1>
        </div>
        <div class="header-actions">
          @if (!editing()) {
            <button class="btn btn-secondary btn-sm" (click)="startEdit()">編輯</button>
          }
          @for (status of nextStatuses(); track status) {
            <button class="btn btn-primary btn-sm" (click)="changeStatus(status)">
              {{ statusActionLabels[status] }}
            </button>
          }
        </div>
      </div>

      <div class="meta-info">
        <span>建立者：{{ exp.createdByName }}</span>
        <span>建立時間：{{ exp.createdAt | date:'yyyy/MM/dd HH:mm' }}</span>
        <span>公開連結：<code>{{ publicLink() }}</code></span>
      </div>

      @if (editing()) {
        <div class="card section">
          <form [formGroup]="editForm" (ngSubmit)="saveEdit()">
            <div class="form-group">
              <label>實驗名稱</label>
              <input formControlName="name" />
            </div>
            <div class="form-group">
              <label>描述</label>
              <textarea formControlName="description" rows="3"></textarea>
            </div>
            <div class="form-group">
              <label>地點</label>
              <input formControlName="location" />
            </div>
            <div class="form-group">
              <label>時長（分鐘）</label>
              <input type="number" formControlName="durationMinutes" />
            </div>
            <div class="form-group">
              <label>每時段人數上限</label>
              <input type="number" formControlName="maxParticipantsPerSlot" />
            </div>
            <div class="edit-actions">
              <button type="submit" class="btn btn-primary btn-sm" [disabled]="saving() || editForm.invalid">儲存</button>
              <button type="button" class="btn btn-secondary btn-sm" (click)="cancelEdit()">取消</button>
            </div>
          </form>
        </div>
      } @else {
        <div class="card section">
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">描述</span>
              <span class="info-value">{{ exp.description || '—' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">地點</span>
              <span class="info-value">{{ exp.location || '—' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">時長</span>
              <span class="info-value">{{ exp.durationMinutes }} 分鐘</span>
            </div>
            <div class="info-item">
              <span class="info-label">每時段人數上限</span>
              <span class="info-value">{{ exp.maxParticipantsPerSlot }}</span>
            </div>
          </div>
        </div>
      }

      <!-- Form Config -->
      <div class="card section">
        <h2>表單欄位設定</h2>
        <div class="form-fields-config">
          @for (field of formFields(); track field.key; let i = $index) {
            <div class="field-config-row">
              <span class="field-label">{{ field.label }} ({{ field.key }})</span>
              <div class="field-toggles">
                <label class="toggle-label">
                  <input
                    type="checkbox"
                    [checked]="field.visible"
                    [disabled]="field.locked"
                    (change)="toggleFormFieldVisible(i)"
                  />
                  顯示
                </label>
                <label class="toggle-label">
                  <input
                    type="checkbox"
                    [checked]="field.required"
                    [disabled]="field.locked || !field.visible"
                    (change)="toggleFormFieldRequired(i)"
                  />
                  必填
                </label>
              </div>
            </div>
          } @empty {
            <div class="empty-state"><p>尚無欄位設定</p></div>
          }
        </div>
      </div>

      <!-- Notification Config -->
      <div class="card section">
        <h2>通知設定</h2>
        <form [formGroup]="notificationForm">
          <div class="toggle-row">
            <label class="toggle-label">
              <input type="checkbox" formControlName="enabled" (change)="saveNotificationConfig()" />
              啟用通知
            </label>
          </div>
          <div class="toggle-row">
            <label class="toggle-label">
              <input type="checkbox" formControlName="onRegistration" (change)="saveNotificationConfig()" />
              報名時發送通知
            </label>
          </div>
        </form>
        <div class="reminder-section">
          <label class="reminder-label">提醒天數</label>
          <div class="chips">
            @for (day of reminders(); track day) {
              <span class="chip">
                {{ day }} 天前
                <button class="chip-remove" (click)="removeReminder(day)">&times;</button>
              </span>
            }
          </div>
          <div class="reminder-add">
            <input
              type="number"
              placeholder="天數"
              [value]="reminderInput()"
              (input)="reminderInput.set($any($event.target).value)"
              (keydown.enter)="addReminder(); $event.preventDefault()"
            />
            <button class="btn btn-secondary btn-sm" (click)="addReminder()">新增</button>
          </div>
        </div>
      </div>

      <!-- Settings -->
      <div class="card section">
        <h2>進階設定</h2>
        <form [formGroup]="settingsForm">
          <div class="toggle-row">
            <label class="toggle-label">
              <input type="checkbox" formControlName="allowDuplicateEmail" (change)="saveSettings()" />
              允許重複 Email 報名
            </label>
          </div>
          <div class="toggle-row">
            <label class="toggle-label">
              <input type="checkbox" formControlName="allowParticipantCancel" (change)="saveSettings()" />
              允許參與者自行取消
            </label>
          </div>
        </form>
      </div>
    }
  `,
})
export class ExperimentDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly experimentService = inject(ExperimentService);

  protected readonly experiment = signal<ExperimentDetailResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly editing = signal(false);
  protected readonly reminderInput = signal('');

  protected readonly experimentId = signal('');

  protected readonly editForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    description: [''],
    location: [''],
    durationMinutes: [30, [Validators.required, Validators.min(1)]],
    maxParticipantsPerSlot: [1, [Validators.required, Validators.min(1)]],
  });

  protected readonly settingsForm = this.fb.nonNullable.group({
    allowDuplicateEmail: [false],
    allowParticipantCancel: [false],
  });

  protected readonly notificationForm = this.fb.nonNullable.group({
    enabled: [false],
    onRegistration: [false],
  });

  protected readonly statusBadgeClass = computed(() => {
    const map: Record<ExperimentStatus, string> = {
      DRAFT: 'badge-neutral',
      OPEN: 'badge-success',
      CLOSED: 'badge-warning',
      ARCHIVED: 'badge-error',
    };
    return map[this.experiment()?.status ?? 'DRAFT'];
  });

  protected readonly nextStatuses = computed<ExperimentStatus[]>(() => {
    const map: Record<ExperimentStatus, ExperimentStatus[]> = {
      DRAFT: ['OPEN'],
      OPEN: ['CLOSED'],
      CLOSED: ['ARCHIVED'],
      ARCHIVED: [],
    };
    return map[this.experiment()?.status ?? 'DRAFT'] ?? [];
  });

  protected readonly statusActionLabels: Record<ExperimentStatus, string> = {
    DRAFT: '',
    OPEN: '開放報名',
    CLOSED: '關閉報名',
    ARCHIVED: '封存',
  };

  protected readonly publicLink = computed(() => {
    const exp = this.experiment();
    return exp ? `/e/${exp.slug}` : '';
  });

  protected readonly formFields = signal<FormField[]>([]);
  protected readonly reminders = signal<number[]>([]);

  ngOnInit(): void {
    this.experimentId.set(this.route.snapshot.paramMap.get('experimentId') ?? '');
    this.loadExperiment();
  }

  protected loadExperiment(): void {
    this.loading.set(true);
    this.experimentService.getExperimentDetail(this.experimentId()).subscribe({
      next: (res) => {
        this.experiment.set(res.data);
        this.formFields.set(res.data.formConfig?.fields ?? []);
        this.reminders.set(res.data.notificationConfig?.reminders ?? []);
        this.notificationForm.patchValue({
          enabled: res.data.notificationConfig?.enabled ?? false,
          onRegistration: res.data.notificationConfig?.onRegistration ?? false,
        });
        this.settingsForm.patchValue({
          allowDuplicateEmail: res.data.allowDuplicateEmail,
          allowParticipantCancel: res.data.allowParticipantCancel,
        });
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected startEdit(): void {
    const exp = this.experiment();
    if (!exp) return;
    this.editForm.patchValue({
      name: exp.name,
      description: exp.description,
      location: exp.location,
      durationMinutes: exp.durationMinutes,
      maxParticipantsPerSlot: exp.maxParticipantsPerSlot,
    });
    this.editing.set(true);
  }

  protected cancelEdit(): void {
    this.editing.set(false);
  }

  protected saveEdit(): void {
    if (this.editForm.invalid) return;
    const exp = this.experiment();
    if (!exp) return;
    this.saving.set(true);
    const value = this.editForm.getRawValue();
    this.experimentService
      .updateExperiment(this.experimentId(), {
        name: value.name,
        description: value.description || undefined,
        location: value.location || undefined,
        durationMinutes: value.durationMinutes,
        maxParticipantsPerSlot: value.maxParticipantsPerSlot,
        allowDuplicateEmail: exp.allowDuplicateEmail,
        allowParticipantCancel: exp.allowParticipantCancel,
      })
      .subscribe({
        next: (res) => {
          this.experiment.set(res.data);
          this.editing.set(false);
          this.saving.set(false);
        },
        error: () => this.saving.set(false),
      });
  }

  protected changeStatus(status: ExperimentStatus): void {
    this.experimentService.updateStatus(this.experimentId(), { status }).subscribe({
      next: (res) => this.experiment.set(res.data),
    });
  }

  protected toggleFormFieldVisible(index: number): void {
    const fields = [...this.formFields()];
    fields[index] = { ...fields[index], visible: !fields[index].visible };
    this.formFields.set(fields);
    this.saveFormConfig();
  }

  protected toggleFormFieldRequired(index: number): void {
    const fields = [...this.formFields()];
    fields[index] = { ...fields[index], required: !fields[index].required };
    this.formFields.set(fields);
    this.saveFormConfig();
  }

  private saveFormConfig(): void {
    this.experimentService
      .updateFormConfig(this.experimentId(), { fields: this.formFields() })
      .subscribe({
        next: (res) => this.experiment.set(res.data),
      });
  }

  protected saveNotificationConfig(): void {
    const value = this.notificationForm.getRawValue();
    const config: NotificationConfig = {
      enabled: value.enabled,
      onRegistration: value.onRegistration,
      reminders: this.reminders(),
    };
    this.experimentService.updateNotificationConfig(this.experimentId(), config).subscribe({
      next: (res) => this.experiment.set(res.data),
    });
  }

  protected addReminder(): void {
    const days = parseInt(this.reminderInput(), 10);
    if (isNaN(days) || days <= 0) return;
    if (this.reminders().includes(days)) return;
    this.reminders.set([...this.reminders(), days].sort((a, b) => a - b));
    this.reminderInput.set('');
    this.saveNotificationConfig();
  }

  protected removeReminder(day: number): void {
    this.reminders.set(this.reminders().filter((d) => d !== day));
    this.saveNotificationConfig();
  }

  protected saveSettings(): void {
    const exp = this.experiment();
    if (!exp) return;
    const value = this.settingsForm.getRawValue();
    this.experimentService
      .updateExperiment(this.experimentId(), {
        name: exp.name,
        description: exp.description,
        location: exp.location,
        durationMinutes: exp.durationMinutes,
        maxParticipantsPerSlot: exp.maxParticipantsPerSlot,
        allowDuplicateEmail: value.allowDuplicateEmail,
        allowParticipantCancel: value.allowParticipantCancel,
      })
      .subscribe({
        next: (res) => this.experiment.set(res.data),
      });
  }

  protected navigateToSlots(): void {
    this.router.navigate(['/experiments', this.experimentId(), 'slots']);
  }

  protected navigateToRegistrations(): void {
    this.router.navigate(['/experiments', this.experimentId(), 'registrations']);
  }
}
