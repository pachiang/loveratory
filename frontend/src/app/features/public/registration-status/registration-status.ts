import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DatePipe } from '@angular/common';
import { RegistrationService } from '../../../core/services/registration.service';
import { PublicRegistrationResponse, RegistrationStatus } from '../../../core/models';

@Component({
  selector: 'app-registration-status',
  standalone: true,
  imports: [DatePipe],
  styleUrl: './registration-status.scss',
  template: `
    <div class="public-page">
      <div class="brand">Loveratory</div>

      @if (loading()) {
        <div class="loading-center"><span class="spinner"></span></div>
      } @else if (errorMessage()) {
        <div class="card error-card">
          <h2>無法載入報名資訊</h2>
          <p>{{ errorMessage() }}</p>
        </div>
      } @else if (registration()) {
        <div class="card">
          <h2 class="page-title">報名狀態</h2>

          <div class="detail-list">
            <div class="detail-item">
              <span class="detail-label">實驗名稱</span>
              <span class="detail-value">{{ registration()!.experimentName }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">地點</span>
              <span class="detail-value">{{ registration()!.location }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">時段</span>
              <span class="detail-value">
                {{ registration()!.slotStartTime | date:'yyyy/MM/dd HH:mm' }}
                -
                {{ registration()!.slotEndTime | date:'HH:mm' }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">報名時間</span>
              <span class="detail-value">{{ registration()!.registeredAt | date:'yyyy/MM/dd HH:mm' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">狀態</span>
              <span class="badge" [class]="statusBadgeClass(registration()!.status)">
                {{ statusLabel(registration()!.status) }}
              </span>
            </div>
          </div>

          @if (registration()!.status === 'CONFIRMED') {
            <div class="cancel-section">
              @if (showCancelConfirm()) {
                <div class="cancel-confirm">
                  <p>確定要取消報名嗎？此操作無法復原。</p>
                  <div class="cancel-actions">
                    <button class="btn btn-secondary" (click)="showCancelConfirm.set(false)">
                      返回
                    </button>
                    <button
                      class="btn btn-danger"
                      [disabled]="cancelling()"
                      (click)="confirmCancel()"
                    >
                      @if (cancelling()) {
                        <span class="spinner"></span>
                      }
                      確認取消
                    </button>
                  </div>
                </div>
              } @else {
                <button class="btn btn-danger btn-full" (click)="showCancelConfirm.set(true)">
                  取消報名
                </button>
              }
            </div>
          }
        </div>
      }
    </div>
  `,
})
export class RegistrationStatusComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly registrationService = inject(RegistrationService);

  protected readonly registration = signal<PublicRegistrationResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal('');
  protected readonly showCancelConfirm = signal(false);
  protected readonly cancelling = signal(false);

  ngOnInit(): void {
    const cancelToken = this.route.snapshot.paramMap.get('cancelToken') ?? '';
    this.loadRegistration(cancelToken);
  }

  protected statusBadgeClass(status: RegistrationStatus): string {
    const map: Record<RegistrationStatus, string> = {
      CONFIRMED: 'badge-success',
      CANCELLED: 'badge-error',
      NO_SHOW: 'badge-warning',
    };
    return map[status];
  }

  protected statusLabel(status: RegistrationStatus): string {
    const map: Record<RegistrationStatus, string> = {
      CONFIRMED: '已確認',
      CANCELLED: '已取消',
      NO_SHOW: '未出席',
    };
    return map[status];
  }

  protected confirmCancel(): void {
    const cancelToken = this.route.snapshot.paramMap.get('cancelToken') ?? '';
    this.cancelling.set(true);

    this.registrationService.cancelRegistration(cancelToken).subscribe({
      next: () => {
        this.cancelling.set(false);
        this.showCancelConfirm.set(false);
        this.loadRegistration(cancelToken);
      },
      error: () => {
        this.cancelling.set(false);
      },
    });
  }

  private loadRegistration(cancelToken: string): void {
    this.loading.set(true);
    this.registrationService.getRegistrationByToken(cancelToken).subscribe({
      next: (res) => {
        this.registration.set(res.data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('找不到此報名紀錄或連結已失效');
        this.loading.set(false);
      },
    });
  }
}
