import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { LabService } from '../../../core/services/lab.service';
import { ProjectService } from '../../../core/services/project.service';
import {
  LabDetailResponse,
  LabStatus,
  ProjectSummaryResponse,
  ProjectStatus,
} from '../../../core/models';

@Component({
  selector: 'app-lab-detail',
  imports: [DatePipe, ReactiveFormsModule, RouterLink],
  styleUrl: './lab-detail.scss',
  template: `
    @if (loading()) {
      <div class="loading-center"><span class="spinner"></span></div>
    } @else if (lab()) {
      <div class="page-header">
        <div>
          <h1>{{ lab()!.name }}</h1>
          <div class="subtitle">{{ lab()!.code }}</div>
        </div>
      </div>

      <div class="tabs">
        <a class="tab active" [routerLink]="['/labs', labId]">專案</a>
        <a class="tab" [routerLink]="['/labs', labId, 'members']">成員</a>
        <a class="tab" [routerLink]="['/labs', labId, 'invitations']">邀請</a>
      </div>

      <div class="card info-card">
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">狀態</span>
            <span class="badge" [class]="statusBadgeClass(lab()!.status)">
              {{ statusLabel(lab()!.status) }}
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">代碼</span>
            <span class="info-value mono">{{ lab()!.code }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">建立時間</span>
            <span class="info-value">{{ lab()!.createdAt | date:'yyyy/MM/dd HH:mm' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">申請人</span>
            <span class="info-value">{{ lab()!.appliedByName }}</span>
          </div>
        </div>
        @if (lab()!.description) {
          <div class="info-description">
            <span class="info-label">描述</span>
            <p>{{ lab()!.description }}</p>
          </div>
        }
      </div>

      @if (lab()!.status === 'REJECTED') {
        <div class="card rejected-card">
          <h3>審核未通過</h3>
          @if (lab()!.reviewNote) {
            <p class="review-note">{{ lab()!.reviewNote }}</p>
          }
          <button class="btn btn-primary" (click)="reapply()">重新申請</button>
        </div>
      }

      @if (lab()!.status === 'APPROVED') {
        <div class="section-header">
          <h2>專案列表</h2>
          <button class="btn btn-primary btn-sm" (click)="showProjectForm.set(true)">
            建立專案
          </button>
        </div>

        @if (showProjectForm()) {
          <div class="dialog-backdrop" (click)="showProjectForm.set(false)">
            <div class="dialog" (click)="$event.stopPropagation()">
              <h3>建立專案</h3>
              <form [formGroup]="projectForm" (ngSubmit)="onCreateProject()">
                <div class="form-group">
                  <label for="projectName">專案名稱</label>
                  <input id="projectName" type="text" formControlName="name" placeholder="輸入專案名稱" />
                  @if (projectForm.controls.name.touched && projectForm.controls.name.hasError('required')) {
                    <div class="error-text">請輸入專案名稱</div>
                  }
                </div>
                <div class="form-group">
                  <label for="projectDescription">描述</label>
                  <textarea id="projectDescription" formControlName="description" placeholder="輸入專案描述" rows="3"></textarea>
                </div>
                <div class="dialog-actions">
                  <button type="button" class="btn btn-secondary" (click)="showProjectForm.set(false)">取消</button>
                  <button type="submit" class="btn btn-primary" [disabled]="projectForm.invalid || creatingProject()">
                    @if (creatingProject()) {
                      <span class="spinner"></span>
                    }
                    建立
                  </button>
                </div>
              </form>
            </div>
          </div>
        }

        @if (loadingProjects()) {
          <div class="loading-center"><span class="spinner"></span></div>
        } @else if (projects().length === 0) {
          <div class="empty-state">
            <h3>尚無專案</h3>
            <p>點擊「建立專案」開始建立你的第一個專案</p>
          </div>
        } @else {
          <div class="card">
            <div class="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>專案名稱</th>
                    <th>狀態</th>
                    <th>建立時間</th>
                  </tr>
                </thead>
                <tbody>
                  @for (project of projects(); track project.projectId) {
                    <tr class="clickable-row" (click)="navigateToProject(project.projectId)">
                      <td>{{ project.name }}</td>
                      <td>
                        <span class="badge" [class]="projectStatusBadgeClass(project.status)">
                          {{ projectStatusLabel(project.status) }}
                        </span>
                      </td>
                      <td>{{ project.createdAt | date:'yyyy/MM/dd' }}</td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          </div>
        }
      }
    }
  `,
})
export class LabDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly labService = inject(LabService);
  private readonly projectService = inject(ProjectService);
  private readonly fb = inject(FormBuilder);

  protected labId = '';

  protected readonly lab = signal<LabDetailResponse | null>(null);
  protected readonly projects = signal<ProjectSummaryResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadingProjects = signal(false);
  protected readonly showProjectForm = signal(false);
  protected readonly creatingProject = signal(false);

  protected readonly projectForm = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    description: [''],
  });

  ngOnInit(): void {
    this.labId = this.route.snapshot.paramMap.get('labId')!;
    this.loadLab();
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

  protected projectStatusBadgeClass(status: ProjectStatus): string {
    const map: Record<ProjectStatus, string> = {
      DRAFT: 'badge-neutral',
      ACTIVE: 'badge-success',
      ARCHIVED: 'badge-warning',
    };
    return map[status];
  }

  protected projectStatusLabel(status: ProjectStatus): string {
    const map: Record<ProjectStatus, string> = {
      DRAFT: '草稿',
      ACTIVE: '進行中',
      ARCHIVED: '已封存',
    };
    return map[status];
  }

  protected navigateToProject(projectId: string): void {
    this.router.navigate(['/labs', this.labId, 'projects', projectId]);
  }

  protected reapply(): void {
    this.router.navigate(['/labs', this.labId, 'reapply']);
  }

  protected onCreateProject(): void {
    if (this.projectForm.invalid) return;
    this.creatingProject.set(true);

    const { name, description } = this.projectForm.getRawValue();
    this.projectService
      .createProject(this.labId, { name, description: description || undefined })
      .subscribe({
        next: (res) => {
          this.creatingProject.set(false);
          this.showProjectForm.set(false);
          this.projectForm.reset();
          this.router.navigate(['/labs', this.labId, 'projects', res.data.projectId]);
        },
        error: () => {
          this.creatingProject.set(false);
        },
      });
  }

  private loadLab(): void {
    this.loading.set(true);
    this.labService.getLabDetail(this.labId).subscribe({
      next: (res) => {
        this.lab.set(res.data);
        this.loading.set(false);
        if (res.data.status === 'APPROVED') {
          this.loadProjects();
        }
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }

  private loadProjects(): void {
    this.loadingProjects.set(true);
    this.projectService.getProjects(this.labId).subscribe({
      next: (res) => {
        this.projects.set(res.data);
        this.loadingProjects.set(false);
      },
      error: () => {
        this.loadingProjects.set(false);
      },
    });
  }
}
