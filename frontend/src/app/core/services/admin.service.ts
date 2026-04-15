import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ApiResponse,
  LabDetailResponse,
  LabRejectRequest,
  LabSummaryResponse,
  Page,
} from '../models';

const API = '/api/v1/admin/labs';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);

  getPendingLabs(
    page: number,
    size: number,
  ): Observable<ApiResponse<Page<LabSummaryResponse>>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<ApiResponse<Page<LabSummaryResponse>>>(
      `${API}/pending`,
      { params },
    );
  }

  getAllLabs(
    page: number,
    size: number,
  ): Observable<ApiResponse<Page<LabSummaryResponse>>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<ApiResponse<Page<LabSummaryResponse>>>(API, { params });
  }

  approveLab(labId: string): Observable<ApiResponse<LabDetailResponse>> {
    return this.http.put<ApiResponse<LabDetailResponse>>(
      `${API}/${labId}/approve`,
      null,
    );
  }

  rejectLab(
    labId: string,
    req: LabRejectRequest,
  ): Observable<ApiResponse<LabDetailResponse>> {
    return this.http.put<ApiResponse<LabDetailResponse>>(
      `${API}/${labId}/reject`,
      req,
    );
  }
}
