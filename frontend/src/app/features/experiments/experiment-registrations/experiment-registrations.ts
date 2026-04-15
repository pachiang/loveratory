import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DatePipe } from '@angular/common';

import { RegistrationService } from '../../../core/services/registration.service';
import { ExperimentService } from '../../../core/services/experiment.service';
import {
  RegistrationResponse,
  RegistrationStatus,
  ExperimentDetailResponse,
} from '../../../core/models';

@Component({
  selector: 'app-experiment-registrations',
  imports: [DatePipe],
  styleUrl: './experiment-registrations.scss',
  template: `
    <div class="tabs">
      <button class="tab" (click)="navigateToSettings()">實驗設定</button>
      <button class="tab" (click)="navigateToSlots()">時段管理</button>
      <button class="tab active">報名管理</button>
    </div>

    @if (experiment(); as exp) {
      <div class="page-header">
        <div>
          <h1>{{ exp.name }} — 報名管理</h1>
        </div>
      </div>
    }

    <!-- Summary counts -->
    <div class="summary-cards">
      <div class="summary-card">
        <span class="summary-value">{{ totalCount() }}</span>
        <span class="summary-label">總報名數</span>
      </div>
      <div class="summary-card summary-confirmed">
        <span class="summary-value">{{ confirmedCount() }}</span>
        <span class="summary-label">已確認</span>
      </div>
      <div class="summary-card summary-cancelled">
        <span class="summary-value">{{ cancelledCount() }}</span>
        <span class="summary-label">已取消</span>
      </div>
      <div class="summary-card summary-noshow">
        <span class="summary-value">{{ noShowCount() }}</span>
        <span class="summary-label">未到</span>
      </div>
    </div>

    <!-- Registrations Table -->
    <div class="card">
      @if (loading()) {
        <div class="loading-center"><span class="spinner"></span></div>
      } @else {
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Email</th>
                <th>姓名</th>
                <th>電話</th>
                <th>時段</th>
                <th>狀態</th>
                <th>報名時間</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              @for (reg of registrations(); track reg.registrationId) {
                <tr>
                  <td>{{ reg.participantEmail }}</td>
                  <td>{{ reg.participantName || '—' }}</td>
                  <td>{{ reg.participantPhone || '—' }}</td>
                  <td>{{ reg.slotId }}</td>
                  <td>
                    <span class="badge" [class]="statusBadgeClass(reg.status)">{{ statusLabel(reg.status) }}</span>
                  </td>
                  <td>{{ reg.registeredAt | date:'yyyy/MM/dd HH:mm' }}</td>
                  <td>
                    <select
                      class="status-select"
                      [value]="reg.status"
                      (change)="changeRegistrationStatus(reg.registrationId, $any($event.target).value)"
                    >
                      @for (opt of statusOptions; track opt) {
                        <option [value]="opt" [selected]="opt === reg.status">{{ statusLabel(opt) }}</option>
                      }
                    </select>
                  </td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="7">
                    <div class="empty-state">
                      <h3>尚無報名記錄</h3>
                    </div>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    </div>
  `,
})
export class ExperimentRegistrations implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly registrationService = inject(RegistrationService);
  private readonly experimentService = inject(ExperimentService);

  protected readonly experiment = signal<ExperimentDetailResponse | null>(null);
  protected readonly registrations = signal<RegistrationResponse[]>([]);
  protected readonly loading = signal(true);

  protected readonly experimentId = signal('');

  protected readonly statusOptions: RegistrationStatus[] = ['CONFIRMED', 'CANCELLED', 'NO_SHOW'];

  protected readonly totalCount = computed(() => this.registrations().length);
  protected readonly confirmedCount = computed(
    () => this.registrations().filter((r) => r.status === 'CONFIRMED').length,
  );
  protected readonly cancelledCount = computed(
    () => this.registrations().filter((r) => r.status === 'CANCELLED').length,
  );
  protected readonly noShowCount = computed(
    () => this.registrations().filter((r) => r.status === 'NO_SHOW').length,
  );

  ngOnInit(): void {
    this.experimentId.set(this.route.snapshot.paramMap.get('experimentId') ?? '');
    this.loadExperiment();
    this.loadRegistrations();
  }

  protected loadExperiment(): void {
    this.experimentService.getExperimentDetail(this.experimentId()).subscribe({
      next: (res) => this.experiment.set(res.data),
    });
  }

  protected loadRegistrations(): void {
    this.loading.set(true);
    this.registrationService.getRegistrations(this.experimentId()).subscribe({
      next: (res) => {
        this.registrations.set(res.data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected changeRegistrationStatus(registrationId: string, status: RegistrationStatus): void {
    this.registrationService.updateStatus(registrationId, { status }).subscribe({
      next: () => this.loadRegistrations(),
    });
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
      NO_SHOW: '未到',
    };
    return map[status];
  }

  protected navigateToSettings(): void {
    this.router.navigate(['/experiments', this.experimentId()]);
  }

  protected navigateToSlots(): void {
    this.router.navigate(['/experiments', this.experimentId(), 'slots']);
  }
}
