import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { LabService } from '../../../core/services/lab.service';
import { AuthService } from '../../../core/services/auth.service';
import { LabInvitationResponse, LabInvitationStatus } from '../../../core/models';

type InviteMode = 'initial' | 'login' | 'register';

@Component({
  selector: 'app-invitation-accept',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  styleUrl: './invitation-accept.scss',
  template: `
    <div class="public-page">
      <div class="brand">Loveratory</div>

      @if (loading()) {
        <div class="loading-center"><span class="spinner"></span></div>
      } @else if (errorMessage()) {
        <div class="card error-card">
          <h2>邀請無效</h2>
          <p>{{ errorMessage() }}</p>
        </div>
      } @else if (invitation()) {
        <div class="card">
          <h2 class="page-title">實驗室邀請</h2>

          <div class="detail-list">
            <div class="detail-item">
              <span class="detail-label">實驗室</span>
              <span class="detail-value">{{ invitation()!.labName }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">邀請人</span>
              <span class="detail-value">{{ invitation()!.invitedByName }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Email</span>
              <span class="detail-value">{{ invitation()!.email }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">狀態</span>
              <span class="badge" [class]="statusBadgeClass(invitation()!.status)">
                {{ statusLabel(invitation()!.status) }}
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">有效期限</span>
              <span class="detail-value">{{ invitation()!.expiresAt | date:'yyyy/MM/dd HH:mm' }}</span>
            </div>
          </div>

          @if (invitation()!.status === 'ACCEPTED') {
            <div class="status-message info">
              <p>此邀請已被接受</p>
            </div>
          } @else if (invitation()!.status === 'EXPIRED') {
            <div class="status-message warning">
              <p>此邀請已過期，請聯繫邀請人重新發送邀請</p>
            </div>
          } @else if (invitation()!.status === 'PENDING') {
            @if (isLoggedIn) {
              <div class="action-section">
                <button
                  class="btn btn-primary btn-full"
                  [disabled]="submitting()"
                  (click)="acceptDirectly()"
                >
                  @if (submitting()) {
                    <span class="spinner"></span>
                  }
                  接受邀請
                </button>
              </div>
            } @else {
              @if (mode() === 'initial') {
                <div class="action-section">
                  <div class="action-options">
                    <button class="btn btn-primary btn-full" (click)="mode.set('login')">
                      已有帳號？登入並加入
                    </button>
                    <button class="btn btn-secondary btn-full" (click)="mode.set('register')">
                      沒有帳號？註冊並加入
                    </button>
                  </div>
                </div>
              } @else if (mode() === 'login') {
                <div class="action-section">
                  <h3 class="form-title">登入</h3>
                  <form [formGroup]="loginForm" (ngSubmit)="onLogin()">
                    <div class="form-group">
                      <label for="loginEmail">Email</label>
                      <input
                        id="loginEmail"
                        type="email"
                        formControlName="email"
                        placeholder="example@email.com"
                      />
                      @if (loginForm.controls.email.touched && loginForm.controls.email.hasError('required')) {
                        <div class="error-text">請輸入 Email</div>
                      }
                    </div>
                    <div class="form-group">
                      <label for="loginPassword">密碼</label>
                      <input
                        id="loginPassword"
                        type="password"
                        formControlName="password"
                      />
                      @if (loginForm.controls.password.touched && loginForm.controls.password.hasError('required')) {
                        <div class="error-text">請輸入密碼</div>
                      }
                    </div>
                    <div class="form-actions">
                      <button type="button" class="btn btn-secondary" (click)="mode.set('initial')">
                        返回
                      </button>
                      <button
                        type="submit"
                        class="btn btn-primary"
                        [disabled]="loginForm.invalid || submitting()"
                      >
                        @if (submitting()) {
                          <span class="spinner"></span>
                        }
                        登入並加入
                      </button>
                    </div>
                  </form>
                </div>
              } @else if (mode() === 'register') {
                <div class="action-section">
                  <h3 class="form-title">註冊</h3>
                  <form [formGroup]="registerForm" (ngSubmit)="onRegister()">
                    <div class="form-group">
                      <label for="registerName">姓名</label>
                      <input
                        id="registerName"
                        type="text"
                        formControlName="name"
                      />
                      @if (registerForm.controls.name.touched && registerForm.controls.name.hasError('required')) {
                        <div class="error-text">請輸入姓名</div>
                      }
                    </div>
                    <div class="form-group">
                      <label for="registerEmail">Email</label>
                      <input
                        id="registerEmail"
                        type="email"
                        formControlName="email"
                        placeholder="example@email.com"
                      />
                      @if (registerForm.controls.email.touched && registerForm.controls.email.hasError('required')) {
                        <div class="error-text">請輸入 Email</div>
                      }
                    </div>
                    <div class="form-group">
                      <label for="registerPassword">密碼</label>
                      <input
                        id="registerPassword"
                        type="password"
                        formControlName="password"
                      />
                      @if (registerForm.controls.password.touched && registerForm.controls.password.hasError('required')) {
                        <div class="error-text">請輸入密碼</div>
                      }
                      @if (registerForm.controls.password.touched && registerForm.controls.password.hasError('minlength')) {
                        <div class="error-text">密碼至少需要 8 個字元</div>
                      }
                    </div>
                    <div class="form-actions">
                      <button type="button" class="btn btn-secondary" (click)="mode.set('initial')">
                        返回
                      </button>
                      <button
                        type="submit"
                        class="btn btn-primary"
                        [disabled]="registerForm.invalid || submitting()"
                      >
                        @if (submitting()) {
                          <span class="spinner"></span>
                        }
                        註冊並加入
                      </button>
                    </div>
                  </form>
                </div>
              }
            }
          }
        </div>
      }
    </div>
  `,
})
export class InvitationAcceptComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly labService = inject(LabService);
  private readonly authService = inject(AuthService);

  protected readonly invitation = signal<LabInvitationResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly mode = signal<InviteMode>('initial');
  protected readonly isLoggedIn = this.authService.isLoggedIn();

  protected readonly loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
  });

  protected readonly registerForm = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  ngOnInit(): void {
    const token = this.route.snapshot.paramMap.get('token') ?? '';
    this.loadInvitation(token);
  }

  protected statusBadgeClass(status: LabInvitationStatus): string {
    const map: Record<LabInvitationStatus, string> = {
      PENDING: 'badge-warning',
      ACCEPTED: 'badge-success',
      EXPIRED: 'badge-error',
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

  protected acceptDirectly(): void {
    const token = this.route.snapshot.paramMap.get('token') ?? '';
    this.submitting.set(true);

    this.labService.acceptInvitation(token).subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigate(['/labs']);
      },
      error: () => {
        this.submitting.set(false);
      },
    });
  }

  protected onLogin(): void {
    if (this.loginForm.invalid) return;
    const token = this.route.snapshot.paramMap.get('token') ?? '';
    this.submitting.set(true);

    const { email, password } = this.loginForm.getRawValue();
    this.labService.loginAndAccept(token, { email, password }).subscribe({
      next: (res) => {
        this.authService.saveAuth(res.data.auth);
        this.submitting.set(false);
        this.router.navigate(['/labs']);
      },
      error: () => {
        this.submitting.set(false);
      },
    });
  }

  protected onRegister(): void {
    if (this.registerForm.invalid) return;
    const token = this.route.snapshot.paramMap.get('token') ?? '';
    this.submitting.set(true);

    const { name, email, password } = this.registerForm.getRawValue();
    this.labService.registerAndAccept(token, { name, email, password }).subscribe({
      next: (res) => {
        this.authService.saveAuth(res.data.auth);
        this.submitting.set(false);
        this.router.navigate(['/labs']);
      },
      error: () => {
        this.submitting.set(false);
      },
    });
  }

  private loadInvitation(token: string): void {
    this.loading.set(true);
    this.labService.getInvitationByToken(token).subscribe({
      next: (res) => {
        this.invitation.set(res.data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('找不到此邀請或邀請連結已失效');
        this.loading.set(false);
      },
    });
  }
}
