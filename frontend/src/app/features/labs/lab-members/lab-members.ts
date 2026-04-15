import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LabService } from '../../../core/services/lab.service';
import { AuthService } from '../../../core/services/auth.service';
import { LabMemberResponse, LabMemberRole } from '../../../core/models';

@Component({
  selector: 'app-lab-members',
  imports: [DatePipe, FormsModule, RouterLink],
  styleUrl: './lab-members.scss',
  template: `
    <div class="page-header">
      <div>
        <h1>實驗室成員</h1>
      </div>
    </div>

    <div class="tabs">
      <a class="tab" [routerLink]="['/labs', labId]">專案</a>
      <a class="tab active" [routerLink]="['/labs', labId, 'members']">成員</a>
      <a class="tab" [routerLink]="['/labs', labId, 'invitations']">邀請</a>
    </div>

    @if (loading()) {
      <div class="loading-center"><span class="spinner"></span></div>
    } @else if (members().length === 0) {
      <div class="empty-state">
        <h3>尚無成員</h3>
        <p>透過邀請功能新增實驗室成員</p>
      </div>
    } @else {
      <div class="card">
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>姓名</th>
                <th>Email</th>
                <th>角色</th>
                <th>加入時間</th>
                @if (isAdmin()) {
                  <th>操作</th>
                }
              </tr>
            </thead>
            <tbody>
              @for (member of members(); track member.userId) {
                <tr>
                  <td>{{ member.name }}</td>
                  <td class="email-cell">{{ member.email }}</td>
                  <td>
                    @if (isAdmin() && member.userId !== currentUserId) {
                      <select
                        class="role-select"
                        [ngModel]="member.role"
                        (ngModelChange)="onRoleChange(member, $event)"
                      >
                        <option value="LAB_ADMIN">管理員</option>
                        <option value="LAB_MEMBER">成員</option>
                      </select>
                    } @else {
                      <span class="badge" [class]="roleBadgeClass(member.role)">
                        {{ roleLabel(member.role) }}
                      </span>
                    }
                  </td>
                  <td>{{ member.joinedAt | date:'yyyy/MM/dd' }}</td>
                  @if (isAdmin()) {
                    <td>
                      @if (member.userId !== currentUserId) {
                        <button
                          class="btn btn-danger btn-sm"
                          (click)="confirmRemove(member)"
                        >
                          移除
                        </button>
                      }
                    </td>
                  }
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>
    }

    @if (showRemoveDialog()) {
      <div class="dialog-backdrop" (click)="showRemoveDialog.set(false)">
        <div class="dialog" (click)="$event.stopPropagation()">
          <h3>確認移除成員</h3>
          <p>確定要將 {{ memberToRemove()?.name }} 從實驗室中移除嗎？此操作無法復原。</p>
          <div class="dialog-actions">
            <button class="btn btn-secondary" (click)="showRemoveDialog.set(false)">取消</button>
            <button
              class="btn btn-danger"
              [disabled]="removing()"
              (click)="onRemoveMember()"
            >
              @if (removing()) {
                <span class="spinner"></span>
              }
              移除
            </button>
          </div>
        </div>
      </div>
    }
  `,
})
export class LabMembers implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly labService = inject(LabService);
  private readonly authService = inject(AuthService);

  protected labId = '';
  protected currentUserId = '';

  protected readonly members = signal<LabMemberResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly showRemoveDialog = signal(false);
  protected readonly memberToRemove = signal<LabMemberResponse | null>(null);
  protected readonly removing = signal(false);

  private currentUserRole: LabMemberRole | null = null;

  ngOnInit(): void {
    this.labId = this.route.snapshot.paramMap.get('labId')!;
    this.currentUserId = this.authService.getCurrentUser()?.userId ?? '';
    this.loadMembers();
  }

  protected isAdmin(): boolean {
    return this.currentUserRole === 'LAB_ADMIN';
  }

  protected roleBadgeClass(role: LabMemberRole): string {
    const map: Record<LabMemberRole, string> = {
      LAB_ADMIN: 'badge-info',
      LAB_MEMBER: 'badge-neutral',
    };
    return map[role];
  }

  protected roleLabel(role: LabMemberRole): string {
    const map: Record<LabMemberRole, string> = {
      LAB_ADMIN: '管理員',
      LAB_MEMBER: '成員',
    };
    return map[role];
  }

  protected onRoleChange(member: LabMemberResponse, newRole: LabMemberRole): void {
    this.labService.updateMemberRole(this.labId, member.userId, { role: newRole }).subscribe({
      next: () => {
        this.loadMembers();
      },
    });
  }

  protected confirmRemove(member: LabMemberResponse): void {
    this.memberToRemove.set(member);
    this.showRemoveDialog.set(true);
  }

  protected onRemoveMember(): void {
    const member = this.memberToRemove();
    if (!member) return;

    this.removing.set(true);
    this.labService.removeMember(this.labId, member.userId).subscribe({
      next: () => {
        this.removing.set(false);
        this.showRemoveDialog.set(false);
        this.memberToRemove.set(null);
        this.loadMembers();
      },
      error: () => {
        this.removing.set(false);
      },
    });
  }

  private loadMembers(): void {
    this.loading.set(true);
    this.labService.getMembers(this.labId).subscribe({
      next: (res) => {
        this.members.set(res.data);
        const me = res.data.find((m) => m.userId === this.currentUserId);
        this.currentUserRole = me?.role ?? null;
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}
