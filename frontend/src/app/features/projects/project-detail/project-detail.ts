import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';

import { ProjectService } from '../../../core/services/project.service';
import { ExperimentService } from '../../../core/services/experiment.service';
import {
  ProjectDetailResponse,
  ProjectStatus,
  ExperimentSummaryResponse,
  ExperimentCreateRequest,
} from '../../../core/models';

@Component({
  selector: 'app-project-detail',
  imports: [ReactiveFormsModule, DatePipe, RouterLink],
  styleUrl: './project-detail.scss',
  template: `
    @if (loading()) {
      <div class="loading-center"><span class="spinner"></span></div>
    } @else if (project(); as p) {
      <div class="project-header">
        <div class="title-area">
          <h1>
            {{ p.name }}
            <span class="badge" [class]="statusBadgeClass()">{{ p.status }}</span>
          </h1>
        </div>
        <div class="status-actions">
          @if (!editing()) {
            <button class="btn btn-secondary btn-sm" (click)="startEdit()">編輯</button>
          }
          @if (canActivate()) {
            <button class="btn btn-primary btn-sm" (click)="changeStatus('ACTIVE')">啟用</button>
          }
          @if (canArchive()) {
            <button class="btn btn-secondary btn-sm" (click)="changeStatus('ARCHIVED')">封存</button>
          }
        </div>
      </div>

      <div class="meta-info">
        <span>建立者：{{ p.createdByName }}</span>
        <span>建立時間：{{ p.createdAt | date:'yyyy/MM/dd HH:mm' }}</span>
      </div>

      @if (editing()) {
        <div class="edit-section card">
          <form [formGroup]="editForm" (ngSubmit)="saveEdit()">
            <div class="form-group">
              <label>專案名稱</label>
              <input formControlName="name" />
            </div>
            <div class="form-group">
              <label>描述</label>
              <textarea formControlName="description" rows="3"></textarea>
            </div>
            <div class="edit-actions">
              <button type="submit" class="btn btn-primary btn-sm" [disabled]="saving() || editForm.invalid">儲存</button>
              <button type="button" class="btn btn-secondary btn-sm" (click)="cancelEdit()">取消</button>
            </div>
          </form>
        </div>
      } @else {
        @if (p.description) {
          <div class="card" style="margin-bottom: 28px;">
            <p>{{ p.description }}</p>
          </div>
        }
      }

      <!-- Investigators section -->
      <div class="section card">
        <div class="section-header">
          <h2>主持人</h2>
          <button class="btn btn-secondary btn-sm" (click)="showInvestigatorInput.set(!showInvestigatorInput())">
            新增主持人
          </button>
        </div>

        @if (showInvestigatorInput()) {
          <div class="investigator-add">
            <input
              placeholder="輸入使用者 ID"
              [value]="investigatorUserId()"
              (input)="investigatorUserId.set($any($event.target).value)"
            />
            <button class="btn btn-primary btn-sm" (click)="addInvestigator()">新增</button>
            <button class="btn btn-secondary btn-sm" (click)="showInvestigatorInput.set(false)">取消</button>
          </div>
        }

        <div class="investigator-list">
          @for (inv of p.investigators; track inv.userId) {
            <div class="investigator-item">
              <div class="investigator-info">
                <div class="investigator-name">{{ inv.name }}</div>
                <div class="investigator-email">{{ inv.email }}</div>
                <div class="investigator-date">加入於 {{ inv.addedAt | date:'yyyy/MM/dd' }}</div>
              </div>
              @if (canRemoveInvestigator()) {
                <button class="btn btn-danger btn-sm" (click)="removeInvestigator(inv.userId)">移除</button>
              }
            </div>
          } @empty {
            <div class="empty-state">
              <p>尚無主持人</p>
            </div>
          }
        </div>
      </div>

      <!-- Experiments section -->
      <div class="section card">
        <div class="section-header">
          <h2>實驗</h2>
          <button class="btn btn-primary btn-sm" (click)="openExperimentDialog()">建立實驗</button>
        </div>

        <div class="experiment-list">
          @for (exp of experiments(); track exp.experimentId) {
            <div class="experiment-item" (click)="navigateToExperiment(exp.experimentId)">
              <div class="experiment-info">
                <div class="experiment-name">
                  {{ exp.name }}
                  <span class="badge" [class]="experimentStatusBadge(exp.status)">{{ exp.status }}</span>
                </div>
                <div class="experiment-meta">
                  {{ exp.slug }} &middot; {{ exp.durationMinutes }} 分鐘
                </div>
              </div>
            </div>
          } @empty {
            <div class="empty-state">
              <h3>尚無實驗</h3>
              <p>點擊「建立實驗」開始新增</p>
            </div>
          }
        </div>
      </div>

      <!-- Create Experiment Dialog -->
      @if (showExperimentDialog()) {
        <div class="dialog-backdrop" (click)="closeExperimentDialog()">
          <div class="dialog dialog-form" (click)="$event.stopPropagation()">
            <h3>建立實驗</h3>
            <form [formGroup]="experimentForm" (ngSubmit)="createExperiment()">
              <div class="form-group">
                <label>實驗名稱</label>
                <input formControlName="name" (input)="onExperimentNameInput()" />
              </div>
              <div class="form-group">
                <label>Slug</label>
                <input formControlName="slug" />
                <div class="slug-preview">公開連結：/e/{{ experimentForm.get('slug')?.value }}</div>
              </div>
              <div class="form-group">
                <label>描述</label>
                <textarea formControlName="description" rows="2"></textarea>
              </div>
              <div class="form-group">
                <label>地點</label>
                <input formControlName="location" />
              </div>
              <div class="form-group">
                <label>時長（分鐘）</label>
                <input type="number" formControlName="durationMinutes" />
              </div>
              <div class="form-group">
                <label>每時段人數上限</label>
                <input type="number" formControlName="maxParticipantsPerSlot" />
              </div>
              <div class="dialog-actions">
                <button type="button" class="btn btn-secondary" (click)="closeExperimentDialog()">取消</button>
                <button type="submit" class="btn btn-primary" [disabled]="saving() || experimentForm.invalid">建立</button>
              </div>
            </form>
          </div>
        </div>
      }
    }
  `,
})
export class ProjectDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly projectService = inject(ProjectService);
  private readonly experimentService = inject(ExperimentService);

  protected readonly project = signal<ProjectDetailResponse | null>(null);
  protected readonly experiments = signal<ExperimentSummaryResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly editing = signal(false);
  protected readonly showExperimentDialog = signal(false);
  protected readonly showInvestigatorInput = signal(false);
  protected readonly investigatorUserId = signal('');

  protected readonly labId = signal('');
  protected readonly projectId = signal('');

  protected readonly editForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    description: [''],
  });

  protected readonly experimentForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    slug: ['', Validators.required],
    description: [''],
    location: [''],
    durationMinutes: [30, [Validators.required, Validators.min(1)]],
    maxParticipantsPerSlot: [1, [Validators.required, Validators.min(1)]],
  });

  protected readonly statusBadgeClass = computed(() => {
    const map: Record<ProjectStatus, string> = {
      DRAFT: 'badge-neutral',
      ACTIVE: 'badge-success',
      ARCHIVED: 'badge-warning',
    };
    return map[this.project()?.status ?? 'DRAFT'];
  });

  protected readonly canActivate = computed(() => this.project()?.status === 'DRAFT');
  protected readonly canArchive = computed(() => this.project()?.status === 'ACTIVE');
  protected readonly canRemoveInvestigator = computed(
    () => (this.project()?.investigators?.length ?? 0) > 1,
  );

  ngOnInit(): void {
    this.labId.set(this.route.snapshot.paramMap.get('labId') ?? '');
    this.projectId.set(this.route.snapshot.paramMap.get('projectId') ?? '');
    this.loadProject();
    this.loadExperiments();
  }

  protected loadProject(): void {
    this.loading.set(true);
    this.projectService.getProjectDetail(this.projectId()).subscribe({
      next: (res) => {
        this.project.set(res.data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  protected loadExperiments(): void {
    this.experimentService.getExperiments(this.projectId()).subscribe({
      next: (res) => this.experiments.set(res.data),
    });
  }

  protected startEdit(): void {
    const p = this.project();
    if (!p) return;
    this.editForm.patchValue({ name: p.name, description: p.description });
    this.editing.set(true);
  }

  protected cancelEdit(): void {
    this.editing.set(false);
  }

  protected saveEdit(): void {
    if (this.editForm.invalid) return;
    const p = this.project();
    if (!p) return;
    this.saving.set(true);
    const { name, description } = this.editForm.getRawValue();
    this.projectService
      .updateProject(this.projectId(), { name, description, status: p.status })
      .subscribe({
        next: (res) => {
          this.project.set(res.data);
          this.editing.set(false);
          this.saving.set(false);
        },
        error: () => this.saving.set(false),
      });
  }

  protected changeStatus(status: ProjectStatus): void {
    const p = this.project();
    if (!p) return;
    this.projectService
      .updateProject(this.projectId(), { name: p.name, description: p.description, status })
      .subscribe({
        next: (res) => this.project.set(res.data),
      });
  }

  protected addInvestigator(): void {
    const userId = this.investigatorUserId().trim();
    if (!userId) return;
    this.projectService.addInvestigator(this.projectId(), { userId }).subscribe({
      next: () => {
        this.investigatorUserId.set('');
        this.showInvestigatorInput.set(false);
        this.loadProject();
      },
    });
  }

  protected removeInvestigator(userId: string): void {
    if (!confirm('確定要移除此主持人嗎？')) return;
    this.projectService.removeInvestigator(this.projectId(), userId).subscribe({
      next: () => this.loadProject(),
    });
  }

  protected openExperimentDialog(): void {
    this.experimentForm.reset({
      name: '',
      slug: '',
      description: '',
      location: '',
      durationMinutes: 30,
      maxParticipantsPerSlot: 1,
    });
    this.showExperimentDialog.set(true);
  }

  protected closeExperimentDialog(): void {
    this.showExperimentDialog.set(false);
  }

  protected onExperimentNameInput(): void {
    const name = this.experimentForm.get('name')?.value ?? '';
    const slug = name
      .toLowerCase()
      .replace(/[^a-z0-9\u4e00-\u9fff]+/g, '-')
      .replace(/^-|-$/g, '');
    this.experimentForm.patchValue({ slug });
  }

  protected createExperiment(): void {
    if (this.experimentForm.invalid) return;
    this.saving.set(true);
    const value = this.experimentForm.getRawValue();
    const req: ExperimentCreateRequest = {
      name: value.name,
      slug: value.slug,
      description: value.description || undefined,
      location: value.location || undefined,
      durationMinutes: value.durationMinutes,
      maxParticipantsPerSlot: value.maxParticipantsPerSlot,
    };
    this.experimentService.createExperiment(this.projectId(), req).subscribe({
      next: () => {
        this.showExperimentDialog.set(false);
        this.saving.set(false);
        this.loadExperiments();
      },
      error: () => this.saving.set(false),
    });
  }

  protected navigateToExperiment(experimentId: string): void {
    this.router.navigate(['/experiments', experimentId]);
  }

  protected experimentStatusBadge(status: string): string {
    const map: Record<string, string> = {
      DRAFT: 'badge-neutral',
      OPEN: 'badge-success',
      CLOSED: 'badge-warning',
      ARCHIVED: 'badge-error',
    };
    return map[status] ?? 'badge-neutral';
  }
}
