import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ApiResponse,
  InvestigatorAddRequest,
  InvestigatorResponse,
  ProjectCreateRequest,
  ProjectDetailResponse,
  ProjectSummaryResponse,
  ProjectUpdateRequest,
} from '../models';

const API_LABS = '/api/v1/labs';
const API_PROJECTS = '/api/v1/projects';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly http = inject(HttpClient);

  getProjects(labId: string): Observable<ApiResponse<ProjectSummaryResponse[]>> {
    return this.http.get<ApiResponse<ProjectSummaryResponse[]>>(
      `${API_LABS}/${labId}/projects`,
    );
  }

  getProjectDetail(projectId: string): Observable<ApiResponse<ProjectDetailResponse>> {
    return this.http.get<ApiResponse<ProjectDetailResponse>>(
      `${API_PROJECTS}/${projectId}`,
    );
  }

  createProject(
    labId: string,
    req: ProjectCreateRequest,
  ): Observable<ApiResponse<ProjectDetailResponse>> {
    return this.http.post<ApiResponse<ProjectDetailResponse>>(
      `${API_LABS}/${labId}/projects`,
      req,
    );
  }

  updateProject(
    projectId: string,
    req: ProjectUpdateRequest,
  ): Observable<ApiResponse<ProjectDetailResponse>> {
    return this.http.put<ApiResponse<ProjectDetailResponse>>(
      `${API_PROJECTS}/${projectId}`,
      req,
    );
  }

  archiveProject(projectId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${API_PROJECTS}/${projectId}`);
  }

  getInvestigators(projectId: string): Observable<ApiResponse<InvestigatorResponse[]>> {
    return this.http.get<ApiResponse<InvestigatorResponse[]>>(
      `${API_PROJECTS}/${projectId}/investigators`,
    );
  }

  addInvestigator(
    projectId: string,
    req: InvestigatorAddRequest,
  ): Observable<ApiResponse<InvestigatorResponse>> {
    return this.http.post<ApiResponse<InvestigatorResponse>>(
      `${API_PROJECTS}/${projectId}/investigators`,
      req,
    );
  }

  removeInvestigator(projectId: string, userId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${API_PROJECTS}/${projectId}/investigators/${userId}`,
    );
  }
}
