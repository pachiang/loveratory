import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { DatePipe } from '@angular/common';
import { LabService } from '../../../core/services/lab.service';
import { LabSummaryResponse, LabMemberRole, LabStatus } from '../../../core/models';

@Component({
  selector: 'app-lab-list',
  imports: [DatePipe],
  styleUrl: './lab-list.scss',
  template: `
    <div class="page-header">
      <div>
        <h1>我的實驗室</h1>
        <div class="subtitle">管理你所屬的實驗室</div>
      </div>
      <button class="btn btn-primary" (click)="navigateToCreate()">建立實驗室</button>
    </div>

    @if (loading()) {
      <div class="loading-center"><span class="spinner"></span></div>
    } @else if (labs().length === 0) {
      <div class="empty-state">
        <h3>尚無實驗室</h3>
        <p>點擊「建立實驗室」開始建立你的第一個實驗室</p>
      </div>
    } @else {
      <div class="lab-grid">
        @for (lab of labs(); track lab.labId) {
          <div class="card lab-card" (click)="navigateToLab(lab.labId)">
            <div class="lab-card-header">
              <h3 class="lab-name">{{ lab.name }}</h3>
              <span class="badge" [class]="statusBadgeClass(lab.status)">
                {{ statusLabel(lab.status) }}
              </span>
            </div>
            <div class="lab-code">{{ lab.code }}</div>
            <div class="lab-card-footer">
              <span class="lab-role badge badge-info">{{ roleLabel(lab.myRole) }}</span>
              <span class="lab-date">{{ lab.createdAt | date:'yyyy/MM/dd' }}</span>
            </div>
          </div>
        }
      </div>
    }
  `,
})
export class LabList implements OnInit {
  private readonly labService = inject(LabService);
  private readonly router = inject(Router);

  protected readonly labs = signal<LabSummaryResponse[]>([]);
  protected readonly loading = signal(true);

  ngOnInit(): void {
    this.loadLabs();
  }

  protected navigateToCreate(): void {
    this.router.navigate(['/labs/create']);
  }

  protected navigateToLab(labId: string): void {
    this.router.navigate(['/labs', labId]);
  }

  protected statusBadgeClass(status: LabStatus): string {
    const map: Record<LabStatus, string> = {
      PENDING: 'badge-warning',
      APPROVED: 'badge-success',
      REJECTED: 'badge-error',
    };
    return map[status];
  }

  protected statusLabel(status: LabStatus): string {
    const map: Record<LabStatus, string> = {
      PENDING: '審核中',
      APPROVED: '已通過',
      REJECTED: '已駁回',
    };
    return map[status];
  }

  protected roleLabel(role: LabMemberRole): string {
    const map: Record<LabMemberRole, string> = {
      LAB_ADMIN: '管理員',
      LAB_MEMBER: '成員',
    };
    return map[role];
  }

  private loadLabs(): void {
    this.loading.set(true);
    this.labService.getMyLabs().subscribe({
      next: (res) => {
        this.labs.set(res.data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}
