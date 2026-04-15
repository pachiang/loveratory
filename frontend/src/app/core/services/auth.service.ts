import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {
  ApiResponse,
  BootstrapAdminRequest,
  TokenRefreshRequest,
  UserLoginRequest,
  UserLoginResponse,
  UserRegisterRequest,
  UserRole,
} from '../models';

const API = '/api/v1/auth';

const STORAGE_KEY = {
  ACCESS_TOKEN: 'access_token',
  REFRESH_TOKEN: 'refresh_token',
  USER: 'user',
} as const;

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  login(req: UserLoginRequest): Observable<ApiResponse<UserLoginResponse>> {
    return this.http.post<ApiResponse<UserLoginResponse>>(`${API}/login`, req);
  }

  register(req: UserRegisterRequest): Observable<ApiResponse<UserLoginResponse>> {
    return this.http.post<ApiResponse<UserLoginResponse>>(`${API}/register`, req);
  }

  refreshToken(req: TokenRefreshRequest): Observable<ApiResponse<UserLoginResponse>> {
    return this.http.post<ApiResponse<UserLoginResponse>>(`${API}/refresh`, req);
  }

  bootstrapAdmin(req: BootstrapAdminRequest): Observable<ApiResponse<UserLoginResponse>> {
    return this.http.post<ApiResponse<UserLoginResponse>>(`${API}/bootstrap`, req);
  }

  saveAuth(resp: UserLoginResponse): void {
    localStorage.setItem(STORAGE_KEY.ACCESS_TOKEN, resp.accessToken);
    localStorage.setItem(STORAGE_KEY.REFRESH_TOKEN, resp.refreshToken);
    localStorage.setItem(STORAGE_KEY.USER, JSON.stringify(resp));
  }

  getAccessToken(): string | null {
    return localStorage.getItem(STORAGE_KEY.ACCESS_TOKEN);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(STORAGE_KEY.REFRESH_TOKEN);
  }

  getCurrentUser(): UserLoginResponse | null {
    const raw = localStorage.getItem(STORAGE_KEY.USER);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as UserLoginResponse;
    } catch {
      return null;
    }
  }

  isLoggedIn(): boolean {
    return !!this.getAccessToken();
  }

  isAdmin(): boolean {
    const user = this.getCurrentUser();
    return user?.role === 'SYSTEM_ADMIN';
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEY.ACCESS_TOKEN);
    localStorage.removeItem(STORAGE_KEY.REFRESH_TOKEN);
    localStorage.removeItem(STORAGE_KEY.USER);
  }
}
