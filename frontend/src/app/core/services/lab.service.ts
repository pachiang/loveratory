import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ApiResponse,
  InvitationAccessResponse,
  LabCreateRequest,
  LabDetailResponse,
  LabInvitationCreateRequest,
  LabInvitationResponse,
  LabMemberResponse,
  LabMemberRoleUpdateRequest,
  LabSummaryResponse,
  LabUpdateRequest,
  UserLoginRequest,
  UserLoginResponse,
  UserRegisterRequest,
} from '../models';

const API_LABS = '/api/v1/labs';
const API_INVITATIONS = '/api/v1/invitations';

@Injectable({ providedIn: 'root' })
export class LabService {
  private readonly http = inject(HttpClient);

  getMyLabs(): Observable<ApiResponse<LabSummaryResponse[]>> {
    return this.http.get<ApiResponse<LabSummaryResponse[]>>(API_LABS);
  }

  getLabDetail(labId: string): Observable<ApiResponse<LabDetailResponse>> {
    return this.http.get<ApiResponse<LabDetailResponse>>(`${API_LABS}/${labId}`);
  }

  createLab(req: LabCreateRequest): Observable<ApiResponse<LabDetailResponse>> {
    return this.http.post<ApiResponse<LabDetailResponse>>(API_LABS, req);
  }

  updateLab(labId: string, req: LabUpdateRequest): Observable<ApiResponse<LabDetailResponse>> {
    return this.http.put<ApiResponse<LabDetailResponse>>(`${API_LABS}/${labId}`, req);
  }

  reapplyLab(labId: string, req: LabCreateRequest): Observable<ApiResponse<LabDetailResponse>> {
    return this.http.post<ApiResponse<LabDetailResponse>>(`${API_LABS}/${labId}/reapply`, req);
  }

  getMembers(labId: string): Observable<ApiResponse<LabMemberResponse[]>> {
    return this.http.get<ApiResponse<LabMemberResponse[]>>(`${API_LABS}/${labId}/members`);
  }

  updateMemberRole(
    labId: string,
    userId: string,
    req: LabMemberRoleUpdateRequest,
  ): Observable<ApiResponse<void>> {
    return this.http.put<ApiResponse<void>>(
      `${API_LABS}/${labId}/members/${userId}/role`,
      req,
    );
  }

  removeMember(labId: string, userId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${API_LABS}/${labId}/members/${userId}`);
  }

  getInvitations(labId: string): Observable<ApiResponse<LabInvitationResponse[]>> {
    return this.http.get<ApiResponse<LabInvitationResponse[]>>(
      `${API_LABS}/${labId}/invitations`,
    );
  }

  createInvitation(
    labId: string,
    req: LabInvitationCreateRequest,
  ): Observable<ApiResponse<LabInvitationResponse>> {
    return this.http.post<ApiResponse<LabInvitationResponse>>(
      `${API_LABS}/${labId}/invitations`,
      req,
    );
  }

  revokeInvitation(labId: string, invitationId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(
      `${API_LABS}/${labId}/invitations/${invitationId}`,
    );
  }

  getInvitationByToken(token: string): Observable<ApiResponse<LabInvitationResponse>> {
    return this.http.get<ApiResponse<LabInvitationResponse>>(
      `${API_INVITATIONS}/${token}`,
    );
  }

  acceptInvitation(token: string): Observable<ApiResponse<InvitationAccessResponse>> {
    return this.http.post<ApiResponse<InvitationAccessResponse>>(
      `${API_INVITATIONS}/${token}/accept`,
      null,
    );
  }

  registerAndAccept(
    token: string,
    req: UserRegisterRequest,
  ): Observable<ApiResponse<InvitationAccessResponse>> {
    return this.http.post<ApiResponse<InvitationAccessResponse>>(
      `${API_INVITATIONS}/${token}/register`,
      req,
    );
  }

  loginAndAccept(
    token: string,
    req: UserLoginRequest,
  ): Observable<ApiResponse<InvitationAccessResponse>> {
    return this.http.post<ApiResponse<InvitationAccessResponse>>(
      `${API_INVITATIONS}/${token}/login`,
      req,
    );
  }
}
