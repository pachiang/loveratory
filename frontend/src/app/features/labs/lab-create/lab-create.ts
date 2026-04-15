import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { LabService } from '../../../core/services/lab.service';

@Component({
  selector: 'app-lab-create',
  imports: [ReactiveFormsModule],
  styleUrl: './lab-create.scss',
  template: `
    <div class="page-header">
      <div>
        <h1>建立實驗室</h1>
        <div class="subtitle">填寫實驗室資訊以提交申請</div>
      </div>
    </div>

    <div class="card form-card">
      <form [formGroup]="form" (ngSubmit)="onSubmit()">
        <div class="form-group">
          <label for="name">實驗室名稱</label>
          <input
            id="name"
            type="text"
            formControlName="name"
            placeholder="例如：認知神經科學實驗室"
            (input)="onNameChange()"
          />
          @if (form.controls.name.touched && form.controls.name.hasError('required')) {
            <div class="error-text">請輸入實驗室名稱</div>
          }
        </div>

        <div class="form-group">
          <label for="code">實驗室代碼</label>
          <input
            id="code"
            type="text"
            formControlName="code"
            placeholder="例如：cognitive-neuro-lab"
          />
          <div class="hint-text">僅允許小寫英文字母、數字和連字號</div>
          @if (form.controls.code.touched && form.controls.code.hasError('required')) {
            <div class="error-text">請輸入實驗室代碼</div>
          }
          @if (form.controls.code.touched && form.controls.code.hasError('pattern')) {
            <div class="error-text">代碼格式不正確，僅允許小寫英文、數字和連字號</div>
          }
        </div>

        <div class="form-group">
          <label for="description">描述</label>
          <textarea
            id="description"
            formControlName="description"
            placeholder="簡述實驗室的研究方向與用途"
            rows="4"
          ></textarea>
        </div>

        <div class="form-actions">
          <button type="button" class="btn btn-secondary" (click)="onCancel()">取消</button>
          <button
            type="submit"
            class="btn btn-primary"
            [disabled]="form.invalid || submitting()"
          >
            @if (submitting()) {
              <span class="spinner"></span>
            }
            提交申請
          </button>
        </div>
      </form>
    </div>
  `,
})
export class LabCreate {
  private readonly labService = inject(LabService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  protected readonly submitting = signal(false);

  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
    code: ['', [Validators.required, Validators.pattern(/^[a-z0-9-]+$/)]],
    description: [''],
  });

  protected onNameChange(): void {
    const name = this.form.controls.name.value;
    const code = name
      .toLowerCase()
      .replace(/[^\w\s-]/g, '')
      .replace(/[\s_]+/g, '-')
      .replace(/^-+|-+$/g, '');
    if (!this.form.controls.code.dirty) {
      this.form.controls.code.setValue(code);
    }
  }

  protected onSubmit(): void {
    if (this.form.invalid) return;
    this.submitting.set(true);

    const { name, code, description } = this.form.getRawValue();
    this.labService.createLab({ name, code, description: description || undefined }).subscribe({
      next: (res) => {
        this.router.navigate(['/labs', res.data.labId]);
      },
      error: () => {
        this.submitting.set(false);
      },
    });
  }

  protected onCancel(): void {
    this.router.navigate(['/labs']);
  }
}
