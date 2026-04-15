import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';

import { SlotService } from '../../../core/services/slot.service';
import { ExperimentService } from '../../../core/services/experiment.service';
import {
  TimeSlotResponse,
  TimeSlotStatus,
  ExperimentDetailResponse,
  SlotCreateRequest,
  SlotBatchCreateRequest,
} from '../../../core/models';

interface SlotGroup {
  date: string;
  slots: TimeSlotResponse[];
}

@Component({
  selector: 'app-experiment-slots',
  imports: [ReactiveFormsModule, DatePipe],
  styleUrl: './experiment-slots.scss',
  template: `
    <div class="tabs">
      <button class="tab" (click)="navigateToSettings()">實驗設定</button>
      <button class="tab active">時段管理</button>
      <button class="tab" (click)="navigateToRegistrations()">報名管理</button>
    </div>

    @if (experiment(); as exp) {
      <div class="page-header">
        <div>
          <h1>{{ exp.name }} — 時段管理</h1>
        </div>
      </div>
    }

    <!-- Create Slot Section -->
    <div class="card section">
      <div class="section-header">
        <h2>新增時段</h2>
        <div class="mode-toggle">
          <button
            class="btn btn-sm"
            [class.btn-primary]="createMode() === 'single'"
            [class.btn-secondary]="createMode() !== 'single'"
            (click)="setCreateMode('single')"
          >
            單一時段
          </button>
          <button
            class="btn btn-sm"
            [class.btn-primary]="createMode() === 'batch'"
            [class.btn-secondary]="createMode() !== 'batch'"
            (click)="setCreateMode('batch')"
          >
            批次建立
          </button>
        </div>
      </div>

      @if (createMode() === 'single') {
        <form [formGroup]="singleForm" (ngSubmit)="createSingleSlot()">
          <div class="form-row">
            <div class="form-group">
              <label>開始時間</label>
              <input type="datetime-local" formControlName="startTime" />
            </div>
            <div class="form-group">
              <label>結束時間</label>
              <input type="datetime-local" formControlName="endTime" />
            </div>
            <div class="form-group">
              <label>容量</label>
              <input type="number" formControlName="capacity" />
            </div>
          </div>
          <button type="submit" class="btn btn-primary btn-sm" [disabled]="saving() || singleForm.invalid">新增</button>
        </form>
      } @else {
        <form [formGroup]="batchForm" (ngSubmit)="createBatchSlots()">
          <div class="form-row">
            <div class="form-group">
              <label>開始日期</label>
              <input type="date" formControlName="startDate" />
            </div>
            <div class="form-group">
              <label>結束日期</label>
              <input type="date" formControlName="endDate" />
            </div>
          </div>
          <div class="form-group">
            <label>星期</label>
            <div class="days-of-week">
              @for (day of daysOfWeekOptions; track day.key) {
                <label class="day-checkbox">
                  <input type="checkbox" [formControlName]="day.key" />
                  {{ day.label }}
                </label>
              }
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>每日開始時間</label>
              <input type="time" formControlName="dailyStartTime" />
            </div>
            <div class="form-group">
              <label>每日結束時間</label>
              <input type="time" formControlName="dailyEndTime" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>時段時長（分鐘）</label>
              <input type="number" formControlName="durationMinutes" />
            </div>
            <div class="form-group">
              <label>休息時間（分鐘）</label>
              <input type="number" formControlName="breakMinutes" />
            </div>
            <div class="form-group">
              <label>容量</label>
              <input type="number" formControlName="capacity" />
            </div>
          </div>
          <button type="submit" class="btn btn-primary btn-sm" [disabled]="saving() || batchForm.invalid">批次建立</button>
        </form>
      }
    </div>

    <!-- Slots Table -->
    <div class="card section">
      <h2>時段列表</h2>

      @if (loading()) {
        <div class="loading-center"><span class="spinner"></span></div>
      } @else {
        @for (group of slotGroups(); track group.date) {
          <div class="slot-group">
            <h3 class="group-date">{{ group.date }}</h3>
            <div class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>開始時間</th>
                    <th>結束時間</th>
                    <th>容量</th>
                    <th>已報名</th>
                    <th>狀態</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  @for (slot of group.slots; track slot.slotId) {
                    @if (editingSlotId() === slot.slotId) {
                      <tr class="editing-row">
                        <td>
                          <input type="datetime-local" [formControl]="$any(editForm.get('startTime'))" />
                        </td>
                        <td>
                          <input type="datetime-local" [formControl]="$any(editForm.get('endTime'))" />
                        </td>
                        <td>
                          <input type="number" [formControl]="$any(editForm.get('capacity'))" style="width: 70px" />
                        </td>
                        <td>{{ slot.currentCount }}</td>
                        <td><span class="badge" [class]="slotStatusBadge(slot.status)">{{ slot.status }}</span></td>
                        <td>
                          <div class="action-buttons">
                            <button class="btn btn-primary btn-sm" (click)="saveEditSlot(slot.slotId)" [disabled]="saving()">儲存</button>
                            <button class="btn btn-secondary btn-sm" (click)="cancelEditSlot()">取消</button>
                          </div>
                        </td>
                      </tr>
                    } @else {
                      <tr>
                        <td>{{ slot.startTime | date:'HH:mm' }}</td>
                        <td>{{ slot.endTime | date:'HH:mm' }}</td>
                        <td>{{ slot.capacity }}</td>
                        <td>{{ slot.currentCount }}</td>
                        <td><span class="badge" [class]="slotStatusBadge(slot.status)">{{ slot.status }}</span></td>
                        <td>
                          @if (slot.status !== 'CANCELLED') {
                            <div class="action-buttons">
                              <button class="btn btn-secondary btn-sm" (click)="startEditSlot(slot)">編輯</button>
                              <button class="btn btn-danger btn-sm" (click)="cancelSlot(slot.slotId)">取消</button>
                            </div>
                          }
                        </td>
                      </tr>
                    }
                  }
                </tbody>
              </table>
            </div>
          </div>
        } @empty {
          <div class="empty-state">
            <h3>尚無時段</h3>
            <p>使用上方表單新增時段</p>
          </div>
        }
      }
    </div>
  `,
})
export class ExperimentSlots implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly slotService = inject(SlotService);
  private readonly experimentService = inject(ExperimentService);

  protected readonly experiment = signal<ExperimentDetailResponse | null>(null);
  protected readonly slots = signal<TimeSlotResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly createMode = signal<'single' | 'batch'>('single');
  protected readonly editingSlotId = signal<string | null>(null);

  protected readonly experimentId = signal('');

  protected readonly singleForm = this.fb.nonNullable.group({
    startTime: ['', Validators.required],
    endTime: ['', Validators.required],
    capacity: [1, [Validators.required, Validators.min(1)]],
  });

  protected readonly batchForm = this.fb.nonNullable.group({
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    monday: [false],
    tuesday: [false],
    wednesday: [false],
    thursday: [false],
    friday: [false],
    saturday: [false],
    sunday: [false],
    dailyStartTime: ['', Validators.required],
    dailyEndTime: ['', Validators.required],
    durationMinutes: [30, [Validators.required, Validators.min(1)]],
    breakMinutes: [0, Validators.min(0)],
    capacity: [1, [Validators.required, Validators.min(1)]],
  });

  protected readonly editForm = this.fb.nonNullable.group({
    startTime: ['', Validators.required],
    endTime: ['', Validators.required],
    capacity: [1, [Validators.required, Validators.min(1)]],
  });

  protected readonly daysOfWeekOptions = [
    { key: 'monday', label: '一' },
    { key: 'tuesday', label: '二' },
    { key: 'wednesday', label: '三' },
    { key: 'thursday', label: '四' },
    { key: 'friday', label: '五' },
    { key: 'saturday', label: '六' },
    { key: 'sunday', label: '日' },
  ] as const;

  protected readonly slotGroups = computed<SlotGroup[]>(() => {
    const grouped = new Map<string, TimeSlotResponse[]>();
    for (const slot of this.slots()) {
      const date = slot.startTime.substring(0, 10);
      if (!grouped.has(date)) {
        grouped.set(date, []);
      }
      grouped.get(date)!.push(slot);
    }
    return Array.from(grouped.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([date, slots]) => ({
        date,
        slots: slots.sort((a, b) => a.startTime.localeCompare(b.startTime)),
      }));
  });

  ngOnInit(): void {
    this.experimentId.set(this.route.snapshot.paramMap.get('experimentId') ?? '');
    this.loadExperiment();
    this.loadSlots();
  }

  protected loadExperiment(): void {
    this.experimentService.getExperimentDetail(this.experimentId()).subscribe({
      next: (res) => this.experiment.set(res.data),
    });
  }

  protected loadSlots(): void {
    this.loading.set(true);
    this.slotService.getSlots(this.experimentId()).subscribe({
      next: (res) => {
        this.slots.set(res.data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected setCreateMode(mode: 'single' | 'batch'): void {
    this.createMode.set(mode);
  }

  protected createSingleSlot(): void {
    if (this.singleForm.invalid) return;
    this.saving.set(true);
    const value = this.singleForm.getRawValue();
    const req: SlotCreateRequest = {
      startTime: value.startTime,
      endTime: value.endTime,
      capacity: value.capacity,
    };
    this.slotService.createSlots(this.experimentId(), [req]).subscribe({
      next: () => {
        this.saving.set(false);
        this.singleForm.reset({ startTime: '', endTime: '', capacity: 1 });
        this.loadSlots();
      },
      error: () => this.saving.set(false),
    });
  }

  protected createBatchSlots(): void {
    if (this.batchForm.invalid) return;
    this.saving.set(true);
    const value = this.batchForm.getRawValue();

    const dayMap: Record<string, string> = {
      monday: 'MONDAY',
      tuesday: 'TUESDAY',
      wednesday: 'WEDNESDAY',
      thursday: 'THURSDAY',
      friday: 'FRIDAY',
      saturday: 'SATURDAY',
      sunday: 'SUNDAY',
    };
    const daysOfWeek: string[] = [];
    for (const opt of this.daysOfWeekOptions) {
      if (value[opt.key]) {
        daysOfWeek.push(dayMap[opt.key]);
      }
    }

    const req: SlotBatchCreateRequest = {
      startDate: value.startDate,
      endDate: value.endDate,
      daysOfWeek,
      dailyStartTime: value.dailyStartTime,
      dailyEndTime: value.dailyEndTime,
      durationMinutes: value.durationMinutes,
      breakMinutes: value.breakMinutes || undefined,
      capacity: value.capacity,
    };

    this.slotService.createSlotsBatch(this.experimentId(), req).subscribe({
      next: () => {
        this.saving.set(false);
        this.loadSlots();
      },
      error: () => this.saving.set(false),
    });
  }

  protected startEditSlot(slot: TimeSlotResponse): void {
    this.editingSlotId.set(slot.slotId);
    this.editForm.patchValue({
      startTime: slot.startTime.substring(0, 16),
      endTime: slot.endTime.substring(0, 16),
      capacity: slot.capacity,
    });
  }

  protected cancelEditSlot(): void {
    this.editingSlotId.set(null);
  }

  protected saveEditSlot(slotId: string): void {
    if (this.editForm.invalid) return;
    this.saving.set(true);
    const value = this.editForm.getRawValue();
    this.slotService
      .updateSlot(slotId, {
        startTime: value.startTime,
        endTime: value.endTime,
        capacity: value.capacity,
      })
      .subscribe({
        next: () => {
          this.editingSlotId.set(null);
          this.saving.set(false);
          this.loadSlots();
        },
        error: () => this.saving.set(false),
      });
  }

  protected cancelSlot(slotId: string): void {
    if (!confirm('確定要取消此時段嗎？')) return;
    this.slotService.cancelSlot(slotId).subscribe({
      next: () => this.loadSlots(),
    });
  }

  protected slotStatusBadge(status: TimeSlotStatus): string {
    const map: Record<TimeSlotStatus, string> = {
      AVAILABLE: 'badge-success',
      FULL: 'badge-warning',
      CANCELLED: 'badge-error',
    };
    return map[status];
  }

  protected navigateToSettings(): void {
    this.router.navigate(['/experiments', this.experimentId()]);
  }

  protected navigateToRegistrations(): void {
    this.router.navigate(['/experiments', this.experimentId(), 'registrations']);
  }
}
