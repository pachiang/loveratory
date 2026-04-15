import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { LabService } from '../../../core/services/lab.service';
import { LabInvitationResponse, LabInvitationStatus } from '../../../core/models';

@Component({
  selector: 'app-lab-invitations',
  imports: [DatePipe, ReactiveFormsModule, RouterLink],
  styleUrl: './lab-invitations.scss',
  template: `
    <div class="page-header">
      <div>
        <h1>邀請管理</h1>
      </div>
      <button class="btn btn-primary" (click)="showInviteForm.set(true)">發送邀請</button>
    </div>

    <div class="tabs">
      <a class="tab" [routerLink]="['/labs', labId]">專案</a>
      <a class="tab" [routerLink]="['/labs', labId, 'members']">成員</a>
      <a class="tab active" [routerLink]="['/labs', labId, 'invitations']">邀請</a>
    </div>

    @if (showInviteForm()) {
      <div class="card invite-form-card">
        <h3>發送邀請</h3>
        <form [formGroup]="inviteForm" (ngSubmit)="onSendInvitation()">
          <div class="form-group">
            <label for="email">Email</label>
            <input
              id="email"
              type="email"
              formControlName="email"
              placeholder="輸入受邀者的 Email"
            />
            @if (inviteForm.controls.email.touched && inviteForm.controls.email.hasError('required')) {
              <div class="error-text">請輸入 Email</div>
            }
            @if (inviteForm.controls.email.touched && inviteForm.controls.email.hasError('email')) {
              <div class="error-text">Email 格式不正確</div>
            }
          </div>
          <div class="invite-form-actions">
            <button type="button" class="btn btn-secondary btn-sm" (click)="showInviteForm.set(false)">取消</button>
            <button type="submit" class="btn btn-primary btn-sm" [disabled]="inviteForm.invalid || sending()">
              @if (sending()) {
                <span class="spinner"></span>
              }
              發送
            </button>
          </div>
        </form>
      </div>
    }

    @if (loading()) {
      <div class="loading-center"><span class="spinner"></span></div>
    } @else if (invitations().length === 0) {
      <div class="empty-state">
        <h3>尚無邀請</h3>
        <p>點擊「發送邀請」邀請新成員加入實驗室</p>
      </div>
    } @else {
      <div class="card">
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Email</th>
                <th>狀態</th>
                <th>邀請者</th>
                <th>到期時間</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              @for (inv of invitations(); track inv.invitationId) {
                <tr>
                  <td>{{ inv.email }}</td>
                  <td>
                    <span class="badge" [class]="statusBadgeClass(inv.status)">
                      {{ statusLabel(inv.status) }}
                    </span>
                  </td>
                  <td>{{ inv.invitedByName }}</td>
                  <td>{{ inv.expiresAt | date:'yyyy/MM/dd HH:mm' }}</td>
                  <td>
                    @if (inv.status === 'PENDING') {
                      <button
                        class="btn btn-danger btn-sm"
                        [disabled]="revoking() === inv.invitationId"
                        (click)="onRevoke(inv)"
                      >
                        @if (revoking() === inv.invitationId) {
                          <span class="spinner"></span>
                        }
                        撤銷
                      </button>
                    }
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>
    }
  `,
})
export class LabInvitations implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly labService = inject(LabService);
  private readonly fb = inject(FormBuilder);

  protected labId = '';

  protected readonly invitations = signal<LabInvitationResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly showInviteForm = signal(false);
  protected readonly sending = signal(false);
  protected readonly revoking = signal<string | null>(null);

  protected readonly inviteForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
  });

  ngOnInit(): void {
    this.labId = this.route.snapshot.paramMap.get('labId')!;
    this.loadInvitations();
  }

  protected statusBadgeClass(status: LabInvitationStatus): string {
    const map: Record<LabInvitationStatus, string> = {
      PENDING: 'badge-warning',
      ACCEPTED: 'badge-success',
      EXPIRED: 'badge-neutral',
    };
    return map[status];
  }

  protected statusLabel(status: LabInvitationStatus): string {
    const map: Record<LabInvitationStatus, string> = {
      PENDING: '待接受',
      ACCEPTED: '已接受',
      EXPIRED: '已過期',
    };
    return map[status];
  }

  protected onSendInvitation(): void {
    if (this.inviteForm.invalid) return;
    this.sending.set(true);

    const { email } = this.inviteForm.getRawValue();
    this.labService.createInvitation(this.labId, { email }).subscribe({
      next: () => {
        this.sending.set(false);
        this.showInviteForm.set(false);
        this.inviteForm.reset();
        this.loadInvitations();
      },
      error: () => {
        this.sending.set(false);
      },
    });
  }

  protected onRevoke(inv: LabInvitationResponse): void {
    this.revoking.set(inv.invitationId);
    this.labService.revokeInvitation(this.labId, inv.invitationId).subscribe({
      next: () => {
        this.revoking.set(null);
        this.loadInvitations();
      },
      error: () => {
        this.revoking.set(null);
      },
    });
  }

  private loadInvitations(): void {
    this.loading.set(true);
    this.labService.getInvitations(this.labId).subscribe({
      next: (res) => {
        this.invitations.set(res.data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}
