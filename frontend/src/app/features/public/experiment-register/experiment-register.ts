import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { RegistrationService } from '../../../core/services/registration.service';
import {
  PublicExperimentResponse,
  PublicTimeSlotResponse,
  PublicRegistrationResponse,
  FormField,
} from '../../../core/models';

@Component({
  selector: 'app-experiment-register',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  styleUrl: './experiment-register.scss',
  template: `
    <div class="public-page">
      <div class="brand">Loveratory</div>

      @if (loading()) {
        <div class="loading-center"><span class="spinner"></span></div>
      } @else if (errorMessage()) {
        <div class="card error-card">
          <h2>無法載入實驗</h2>
          <p>{{ errorMessage() }}</p>
        </div>
      } @else if (submitted()) {
        <div class="card success-card">
          <div class="success-icon">&#10003;</div>
          <h2>報名成功</h2>
          <p class="success-subtitle">你已成功報名此實驗</p>

          <div class="detail-list">
            <div class="detail-item">
              <span class="detail-label">實驗名稱</span>
              <span class="detail-value">{{ registration()?.experimentName }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">地點</span>
              <span class="detail-value">{{ registration()?.location }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">時段</span>
              <span class="detail-value">
                {{ registration()?.slotStartTime | date:'yyyy/MM/dd HH:mm' }}
                -
                {{ registration()?.slotEndTime | date:'HH:mm' }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Email</span>
              <span class="detail-value">{{ registration()?.email }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">狀態</span>
              <span class="badge badge-success">已確認</span>
            </div>
          </div>

          <div class="cancel-link-section">
            <p>如需取消報名，請保存以下連結：</p>
            <a class="cancel-link" [href]="cancelUrl()">查看報名狀態 / 取消報名</a>
          </div>
        </div>
      } @else {
        <div class="card">
          <h1 class="experiment-title">{{ experiment()?.name }}</h1>

          @if (experiment()?.description) {
            <p class="experiment-desc">{{ experiment()?.description }}</p>
          }

          <div class="experiment-meta">
            @if (experiment()?.location) {
              <div class="meta-item">
                <span class="meta-label">地點</span>
                <span class="meta-value">{{ experiment()?.location }}</span>
              </div>
            }
            <div class="meta-item">
              <span class="meta-label">時長</span>
              <span class="meta-value">{{ experiment()?.durationMinutes }} 分鐘</span>
            </div>
          </div>
        </div>

        <div class="card slot-section">
          <h2 class="section-title">選擇時段</h2>

          @if (availableSlots().length === 0) {
            <div class="empty-state">
              <p>目前沒有可選的時段</p>
            </div>
          } @else {
            <div class="slot-list">
              @for (slot of availableSlots(); track slot.slotId) {
                <button
                  type="button"
                  class="slot-card"
                  [class.selected]="selectedSlotId() === slot.slotId"
                  [class.full]="slot.remainingSpots <= 0"
                  [disabled]="slot.remainingSpots <= 0"
                  (click)="selectSlot(slot.slotId)"
                >
                  <div class="slot-time">
                    {{ slot.startTime | date:'MM/dd (EEE)' }}
                    {{ slot.startTime | date:'HH:mm' }} - {{ slot.endTime | date:'HH:mm' }}
                  </div>
                  <div class="slot-spots">
                    @if (slot.remainingSpots > 0) {
                      剩餘 {{ slot.remainingSpots }} 個名額
                    } @else {
                      已額滿
                    }
                  </div>
                </button>
              }
            </div>
          }
        </div>

        @if (selectedSlotId()) {
          <div class="card form-section">
            <h2 class="section-title">填寫報名資料</h2>

            <form [formGroup]="form" (ngSubmit)="onSubmit()">
              @for (field of visibleFields(); track field.key) {
                <div class="form-group">
                  <label [for]="field.key">
                    {{ field.label }}
                    @if (field.required) {
                      <span class="required-mark">*</span>
                    }
                  </label>

                  @if (field.key === 'gender') {
                    <select [id]="field.key" [formControlName]="field.key">
                      <option value="">請選擇</option>
                      <option value="男">男</option>
                      <option value="女">女</option>
                      <option value="其他">其他</option>
                    </select>
                  } @else if (field.key === 'dominantHand') {
                    <select [id]="field.key" [formControlName]="field.key">
                      <option value="">請選擇</option>
                      <option value="左手">左手</option>
                      <option value="右手">右手</option>
                    </select>
                  } @else if (field.key === 'notes') {
                    <textarea
                      [id]="field.key"
                      [formControlName]="field.key"
                      rows="3"
                      placeholder="如有任何備註請填寫"
                    ></textarea>
                  } @else if (field.key === 'age') {
                    <input
                      [id]="field.key"
                      type="number"
                      [formControlName]="field.key"
                      min="1"
                      max="120"
                    />
                  } @else if (field.key === 'email') {
                    <input
                      [id]="field.key"
                      type="email"
                      [formControlName]="field.key"
                      placeholder="example@email.com"
                    />
                  } @else {
                    <input
                      [id]="field.key"
                      type="text"
                      [formControlName]="field.key"
                    />
                  }

                  @if (form.get(field.key)?.touched && form.get(field.key)?.hasError('required')) {
                    <div class="error-text">此欄位為必填</div>
                  }
                  @if (form.get(field.key)?.touched && form.get(field.key)?.hasError('email')) {
                    <div class="error-text">請輸入有效的 Email</div>
                  }
                </div>
              }

              <div class="form-actions">
                <button
                  type="submit"
                  class="btn btn-primary btn-lg"
                  [disabled]="form.invalid || submitting()"
                >
                  @if (submitting()) {
                    <span class="spinner"></span>
                  }
                  確認報名
                </button>
              </div>
            </form>
          </div>
        }
      }
    </div>
  `,
})
export class ExperimentRegisterComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly registrationService = inject(RegistrationService);

  protected readonly experiment = signal<PublicExperimentResponse | null>(null);
  protected readonly registration = signal<PublicRegistrationResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly submitting = signal(false);
  protected readonly submitted = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly selectedSlotId = signal('');

  protected form: FormGroup = this.fb.group({});

  protected readonly availableSlots = computed(() => {
    const exp = this.experiment();
    if (!exp) return [];
    return exp.availableSlots.filter(s => s.remainingSpots > 0)
      .concat(exp.availableSlots.filter(s => s.remainingSpots <= 0));
  });

  protected readonly visibleFields = computed(() => {
    const exp = this.experiment();
    if (!exp) return [];
    return exp.formConfig.fields.filter(f => f.visible);
  });

  protected readonly cancelUrl = computed(() => {
    const reg = this.registration();
    if (!reg) return '';
    return `${window.location.origin}/registration/${reg.cancelToken}`;
  });

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug') ?? '';
    this.loadExperiment(slug);
  }

  protected selectSlot(slotId: string): void {
    this.selectedSlotId.set(slotId);
  }

  protected onSubmit(): void {
    if (this.form.invalid || !this.selectedSlotId()) return;
    this.submitting.set(true);

    const slug = this.route.snapshot.paramMap.get('slug') ?? '';
    const formValue = this.form.getRawValue();

    this.registrationService.register(slug, {
      slotId: this.selectedSlotId(),
      email: formValue['email'] ?? '',
      name: formValue['name'] || undefined,
      phone: formValue['phone'] || undefined,
      studentId: formValue['studentId'] || undefined,
      age: formValue['age'] || undefined,
      gender: formValue['gender'] || undefined,
      dominantHand: formValue['dominantHand'] || undefined,
      notes: formValue['notes'] || undefined,
    }).subscribe({
      next: (res) => {
        this.registration.set(res.data);
        this.submitted.set(true);
        this.submitting.set(false);
      },
      error: () => {
        this.submitting.set(false);
      },
    });
  }

  private loadExperiment(slug: string): void {
    this.loading.set(true);
    this.registrationService.getPublicExperiment(slug).subscribe({
      next: (res) => {
        this.experiment.set(res.data);
        this.buildForm(res.data.formConfig.fields);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('找不到此實驗或實驗已關閉');
        this.loading.set(false);
      },
    });
  }

  private buildForm(fields: FormField[]): void {
    const group: Record<string, any> = {};
    for (const field of fields) {
      if (!field.visible) continue;
      const validators = [];
      if (field.required) validators.push(Validators.required);
      if (field.key === 'email') validators.push(Validators.email);
      group[field.key] = ['', validators];
    }
    this.form = this.fb.group(group);
  }
}
