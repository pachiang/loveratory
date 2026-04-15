import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';
import { LabSummaryResponse, LabStatus, Page } from '../../../core/models';

@Component({
  selector: 'app-admin-labs',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule],
  styleUrl: './admin-labs.scss',
  template: `
    <div class="page-header">
      <div>
        <h1>實驗室管理</h1>
        <div class="subtitle">審核與管理所有實驗室</div>
      </div>
    </div>

    <div class="tabs">
      <button
        class="tab"
        [class.active]="activeTab() === 'pending'"
        (click)="switchTab('pending')"
      >
        待審核
        @if (pendingTotal() > 0) {
          <span class="tab-count">{{ pendingTotal() }}</span>
        }
      </button>
      <button
        class="tab"
        [class.active]="activeTab() === 'all'"
        (click)="switchTab('all')"
      >
        所有實驗室
      </button>
    </div>

    @if (activeTab() === 'pending') {
      @if (loadingPending()) {
        <div class="loading-center"><span class="spinner"></span></div>
      } @else if (pendingLabs().length === 0) {
        <div class="empty-state">
          <h3>沒有待審核的實驗室</h3>
          <p>目前沒有新的實驗室申請需要審核</p>
        </div>
      } @else {
        <div class="card">
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>名稱</th>
                  <th>代碼</th>
                  <th>申請人</th>
                  <th>申請日期</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                @for (lab of pendingLabs(); track lab.labId) {
                  <tr>
                    <td class="lab-name-cell">{{ lab.name }}</td>
                    <td class="lab-code-cell">{{ lab.code }}</td>
                    <td>-</td>
                    <td>{{ lab.createdAt | date:'yyyy/MM/dd' }}</td>
                    <td class="action-cell">
                      <button
                        class="btn btn-sm btn-approve"
                        [disabled]="processing()"
                        (click)="approveLab(lab.labId)"
                      >
                        核准
                      </button>
                      <button
                        class="btn btn-sm btn-danger"
                        (click)="openRejectDialog(lab.labId)"
                      >
                        拒絕
                      </button>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>

          @if (pendingTotalPages() > 1) {
            <div class="pagination">
              <button
                class="btn btn-sm btn-secondary"
                [disabled]="pendingPage() === 0"
                (click)="goToPendingPage(pendingPage() - 1)"
              >
                上一頁
              </button>
              <span class="page-info">
                {{ pendingPage() + 1 }} / {{ pendingTotalPages() }}
              </span>
              <button
                class="btn btn-sm btn-secondary"
                [disabled]="pendingPage() >= pendingTotalPages() - 1"
                (click)="goToPendingPage(pendingPage() + 1)"
              >
                下一頁
              </button>
            </div>
          }
        </div>
      }
    }

    @if (activeTab() === 'all') {
      @if (loadingAll()) {
        <div class="loading-center"><span class="spinner"></span></div>
      } @else if (allLabs().length === 0) {
        <div class="empty-state">
          <h3>尚無實驗室</h3>
          <p>目前系統中沒有任何實驗室</p>
        </div>
      } @else {
        <div class="card">
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>名稱</th>
                  <th>代碼</th>
                  <th>狀態</th>
                  <th>建立日期</th>
                </tr>
              </thead>
              <tbody>
                @for (lab of allLabs(); track lab.labId) {
                  <tr>
                    <td class="lab-name-cell">{{ lab.name }}</td>
                    <td class="lab-code-cell">{{ lab.code }}</td>
                    <td>
                      <span class="badge" [class]="statusBadgeClass(lab.status)">
                        {{ statusLabel(lab.status) }}
                      </span>
                    </td>
                    <td>{{ lab.createdAt | date:'yyyy/MM/dd' }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>

          @if (allTotalPages() > 1) {
            <div class="pagination">
              <button
                class="btn btn-sm btn-secondary"
                [disabled]="allPage() === 0"
                (click)="goToAllPage(allPage() - 1)"
              >
                上一頁
              </button>
              <span class="page-info">
                {{ allPage() + 1 }} / {{ allTotalPages() }}
              </span>
              <button
                class="btn btn-sm btn-secondary"
                [disabled]="allPage() >= allTotalPages() - 1"
                (click)="goToAllPage(allPage() + 1)"
              >
                下一頁
              </button>
            </div>
          }
        </div>
      }
    }

    @if (showRejectDialog()) {
      <div class="dialog-backdrop" (click)="closeRejectDialog()">
        <div class="dialog" (click)="$event.stopPropagation()">
          <h3>拒絕實驗室申請</h3>
          <p>請提供拒絕原因，將會通知申請人。</p>
          <form [formGroup]="rejectForm" (ngSubmit)="confirmReject()">
            <div class="form-group">
              <label for="reviewNote">拒絕原因</label>
              <textarea
                id="reviewNote"
                formControlName="reviewNote"
                rows="3"
                placeholder="請說明拒絕的原因"
              ></textarea>
              @if (rejectForm.controls.reviewNote.touched && rejectForm.controls.reviewNote.hasError('required')) {
                <div class="error-text">請輸入拒絕原因</div>
              }
            </div>
            <div class="dialog-actions">
              <button type="button" class="btn btn-secondary" (click)="closeRejectDialog()">
                取消
              </button>
              <button
                type="submit"
                class="btn btn-danger"
                [disabled]="rejectForm.invalid || processing()"
              >
                @if (processing()) {
                  <span class="spinner"></span>
                }
                確認拒絕
              </button>
            </div>
          </form>
        </div>
      </div>
    }
  `,
})
export class AdminLabsComponent implements OnInit {
  private readonly adminService = inject(AdminService);
  private readonly fb = inject(FormBuilder);

  protected readonly activeTab = signal<'pending' | 'all'>('pending');

  protected readonly pendingLabs = signal<LabSummaryResponse[]>([]);
  protected readonly loadingPending = signal(true);
  protected readonly pendingPage = signal(0);
  protected readonly pendingTotal = signal(0);
  protected readonly pendingTotalPages = signal(0);

  protected readonly allLabs = signal<LabSummaryResponse[]>([]);
  protected readonly loadingAll = signal(true);
  protected readonly allPage = signal(0);
  protected readonly allTotalPages = signal(0);

  protected readonly processing = signal(false);
  protected readonly showRejectDialog = signal(false);
  protected readonly rejectingLabId = signal('');

  protected readonly rejectForm = this.fb.nonNullable.group({
    reviewNote: ['', [Validators.required]],
  });

  private readonly pageSize = 20;

  ngOnInit(): void {
    this.loadPendingLabs();
  }

  protected switchTab(tab: 'pending' | 'all'): void {
    this.activeTab.set(tab);
    if (tab === 'all' && this.allLabs().length === 0) {
      this.loadAllLabs();
    }
  }

  protected goToPendingPage(page: number): void {
    this.pendingPage.set(page);
    this.loadPendingLabs();
  }

  protected goToAllPage(page: number): void {
    this.allPage.set(page);
    this.loadAllLabs();
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

  protected approveLab(labId: string): void {
    this.processing.set(true);
    this.adminService.approveLab(labId).subscribe({
      next: () => {
        this.processing.set(false);
        this.loadPendingLabs();
        if (this.activeTab() === 'all' || this.allLabs().length > 0) {
          this.loadAllLabs();
        }
      },
      error: () => {
        this.processing.set(false);
      },
    });
  }

  protected openRejectDialog(labId: string): void {
    this.rejectingLabId.set(labId);
    this.rejectForm.reset();
    this.showRejectDialog.set(true);
  }

  protected closeRejectDialog(): void {
    this.showRejectDialog.set(false);
    this.rejectingLabId.set('');
  }

  protected confirmReject(): void {
    if (this.rejectForm.invalid) return;
    this.processing.set(true);

    const { reviewNote } = this.rejectForm.getRawValue();
    this.adminService.rejectLab(this.rejectingLabId(), { reviewNote }).subscribe({
      next: () => {
        this.processing.set(false);
        this.closeRejectDialog();
        this.loadPendingLabs();
        if (this.allLabs().length > 0) {
          this.loadAllLabs();
        }
      },
      error: () => {
        this.processing.set(false);
      },
    });
  }

  private loadPendingLabs(): void {
    this.loadingPending.set(true);
    this.adminService.getPendingLabs(this.pendingPage(), this.pageSize).subscribe({
      next: (res) => {
        this.pendingLabs.set(res.data.content);
        this.pendingTotal.set(res.data.totalElements);
        this.pendingTotalPages.set(res.data.totalPages);
        this.loadingPending.set(false);
      },
      error: () => {
        this.loadingPending.set(false);
      },
    });
  }

  private loadAllLabs(): void {
    this.loadingAll.set(true);
    this.adminService.getAllLabs(this.allPage(), this.pageSize).subscribe({
      next: (res) => {
        this.allLabs.set(res.data.content);
        this.allTotalPages.set(res.data.totalPages);
        this.loadingAll.set(false);
      },
      error: () => {
        this.loadingAll.set(false);
      },
    });
  }
}
