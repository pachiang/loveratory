import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ApiResponse,
  ExperimentCreateRequest,
  ExperimentDetailResponse,
  ExperimentStatusUpdateRequest,
  ExperimentSummaryResponse,
  ExperimentUpdateRequest,
  FormConfig,
  NotificationConfig,
} from '../models';

const API_PROJECTS = '/api/v1/projects';
const API_EXPERIMENTS = '/api/v1/experiments';

@Injectable({ providedIn: 'root' })
export class ExperimentService {
  private readonly http = inject(HttpClient);

  getExperiments(projectId: string): Observable<ApiResponse<ExperimentSummaryResponse[]>> {
    return this.http.get<ApiResponse<ExperimentSummaryResponse[]>>(
      `${API_PROJECTS}/${projectId}/experiments`,
    );
  }

  getExperimentDetail(
    experimentId: string,
  ): Observable<ApiResponse<ExperimentDetailResponse>> {
    return this.http.get<ApiResponse<ExperimentDetailResponse>>(
      `${API_EXPERIMENTS}/${experimentId}`,
    );
  }

  createExperiment(
    projectId: string,
    req: ExperimentCreateRequest,
  ): Observable<ApiResponse<ExperimentDetailResponse>> {
    return this.http.post<ApiResponse<ExperimentDetailResponse>>(
      `${API_PROJECTS}/${projectId}/experiments`,
      req,
    );
  }

  updateExperiment(
    experimentId: string,
    req: ExperimentUpdateRequest,
  ): Observable<ApiResponse<ExperimentDetailResponse>> {
    return this.http.put<ApiResponse<ExperimentDetailResponse>>(
      `${API_EXPERIMENTS}/${experimentId}`,
      req,
    );
  }

  updateStatus(
    experimentId: string,
    req: ExperimentStatusUpdateRequest,
  ): Observable<ApiResponse<ExperimentDetailResponse>> {
    return this.http.put<ApiResponse<ExperimentDetailResponse>>(
      `${API_EXPERIMENTS}/${experimentId}/status`,
      req,
    );
  }

  updateFormConfig(
    experimentId: string,
    config: FormConfig,
  ): Observable<ApiResponse<ExperimentDetailResponse>> {
    return this.http.put<ApiResponse<ExperimentDetailResponse>>(
      `${API_EXPERIMENTS}/${experimentId}/form-config`,
      config,
    );
  }

  updateNotificationConfig(
    experimentId: string,
    config: NotificationConfig,
  ): Observable<ApiResponse<ExperimentDetailResponse>> {
    return this.http.put<ApiResponse<ExperimentDetailResponse>>(
      `${API_EXPERIMENTS}/${experimentId}/notification-config`,
      config,
    );
  }
}
