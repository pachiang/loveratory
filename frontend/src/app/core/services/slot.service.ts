import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ApiResponse,
  SlotBatchCreateRequest,
  SlotCreateRequest,
  SlotUpdateRequest,
  TimeSlotResponse,
} from '../models';

const API_EXPERIMENTS = '/api/v1/experiments';
const API_SLOTS = '/api/v1/slots';

@Injectable({ providedIn: 'root' })
export class SlotService {
  private readonly http = inject(HttpClient);

  getSlots(experimentId: string): Observable<ApiResponse<TimeSlotResponse[]>> {
    return this.http.get<ApiResponse<TimeSlotResponse[]>>(
      `${API_EXPERIMENTS}/${experimentId}/slots`,
    );
  }

  createSlots(
    experimentId: string,
    reqs: SlotCreateRequest[],
  ): Observable<ApiResponse<TimeSlotResponse[]>> {
    return this.http.post<ApiResponse<TimeSlotResponse[]>>(
      `${API_EXPERIMENTS}/${experimentId}/slots`,
      reqs,
    );
  }

  createSlotsBatch(
    experimentId: string,
    req: SlotBatchCreateRequest,
  ): Observable<ApiResponse<TimeSlotResponse[]>> {
    return this.http.post<ApiResponse<TimeSlotResponse[]>>(
      `${API_EXPERIMENTS}/${experimentId}/slots/batch`,
      req,
    );
  }

  updateSlot(
    slotId: string,
    req: SlotUpdateRequest,
  ): Observable<ApiResponse<TimeSlotResponse>> {
    return this.http.put<ApiResponse<TimeSlotResponse>>(
      `${API_SLOTS}/${slotId}`,
      req,
    );
  }

  cancelSlot(slotId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${API_SLOTS}/${slotId}`);
  }
}
