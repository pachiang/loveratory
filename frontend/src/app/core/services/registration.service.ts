import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ApiResponse,
  ParticipantRegistrationRequest,
  PublicExperimentResponse,
  PublicRegistrationResponse,
  RegistrationResponse,
  RegistrationStatusUpdateRequest,
} from '../models';

const API_EXPERIMENTS = '/api/v1/experiments';
const API_REGISTRATIONS = '/api/v1/registrations';
const API_PUBLIC = '/api/v1/public';

@Injectable({ providedIn: 'root' })
export class RegistrationService {
  private readonly http = inject(HttpClient);

  getRegistrations(
    experimentId: string,
  ): Observable<ApiResponse<RegistrationResponse[]>> {
    return this.http.get<ApiResponse<RegistrationResponse[]>>(
      `${API_EXPERIMENTS}/${experimentId}/registrations`,
    );
  }

  updateStatus(
    registrationId: string,
    req: RegistrationStatusUpdateRequest,
  ): Observable<ApiResponse<RegistrationResponse>> {
    return this.http.put<ApiResponse<RegistrationResponse>>(
      `${API_REGISTRATIONS}/${registrationId}/status`,
      req,
    );
  }

  getPublicExperiment(slug: string): Observable<ApiResponse<PublicExperimentResponse>> {
    return this.http.get<ApiResponse<PublicExperimentResponse>>(
      `${API_PUBLIC}/experiments/${slug}`,
    );
  }

  register(
    slug: string,
    req: ParticipantRegistrationRequest,
  ): Observable<ApiResponse<PublicRegistrationResponse>> {
    return this.http.post<ApiResponse<PublicRegistrationResponse>>(
      `${API_PUBLIC}/experiments/${slug}/register`,
      req,
    );
  }

  getRegistrationByToken(
    cancelToken: string,
  ): Observable<ApiResponse<PublicRegistrationResponse>> {
    return this.http.get<ApiResponse<PublicRegistrationResponse>>(
      `${API_PUBLIC}/registrations/${cancelToken}`,
    );
  }

  cancelRegistration(cancelToken: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${API_PUBLIC}/registrations/${cancelToken}`,
    );
  }
}
