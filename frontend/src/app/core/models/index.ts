// API Response wrapper
export interface ApiResponse<T> {
  success: boolean;
  code: string;
  message: string | null;
  data: T;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// Auth
export interface UserLoginRequest { email: string; password: string; }
export interface UserRegisterRequest { email: string; password: string; name: string; }
export interface TokenRefreshRequest { refreshToken: string; }
export interface BootstrapAdminRequest { name: string; email: string; password: string; bootstrapSecret: string; }

export interface UserLoginResponse {
  accessToken: string;
  refreshToken: string;
  userId: string;
  email: string;
  name: string;
  role: UserRole;
}

export type UserRole = 'SYSTEM_ADMIN' | 'USER';

// Lab
export type LabStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
export type LabMemberRole = 'LAB_ADMIN' | 'LAB_MEMBER';
export type LabMemberStatus = 'ACTIVE' | 'REMOVED';
export type LabInvitationStatus = 'PENDING' | 'ACCEPTED' | 'EXPIRED';

export interface LabCreateRequest { name: string; code: string; description?: string; }
export interface LabUpdateRequest { name: string; description?: string; }
export interface LabRejectRequest { reviewNote: string; }
export interface LabInvitationCreateRequest { email: string; }
export interface LabMemberRoleUpdateRequest { role: LabMemberRole; }

export interface LabDetailResponse {
  labId: string;
  name: string;
  code: string;
  description: string;
  status: LabStatus;
  appliedBy: string;
  appliedByName: string;
  reviewedBy: string;
  reviewNote: string;
  approvedAt: string;
  createdAt: string;
}

export interface LabSummaryResponse {
  labId: string;
  name: string;
  code: string;
  status: LabStatus;
  myRole: LabMemberRole;
  createdAt: string;
}

export interface LabMemberResponse {
  userId: string;
  name: string;
  email: string;
  role: LabMemberRole;
  joinedAt: string;
}

export interface LabInvitationResponse {
  invitationId: string;
  labId: string;
  labName: string;
  email: string;
  status: LabInvitationStatus;
  invitedBy: string;
  invitedByName: string;
  expiresAt: string;
  acceptedAt: string;
  createdAt: string;
}

export interface InvitationAccessResponse {
  auth: UserLoginResponse;
  invitation: LabInvitationResponse;
}

// Project
export type ProjectStatus = 'DRAFT' | 'ACTIVE' | 'ARCHIVED';
export type ProjectInvestigatorStatus = 'ACTIVE' | 'REMOVED';

export interface ProjectCreateRequest { name: string; description?: string; }
export interface ProjectUpdateRequest { name: string; description?: string; status: ProjectStatus; }
export interface InvestigatorAddRequest { userId: string; }

export interface ProjectDetailResponse {
  projectId: string;
  labId: string;
  name: string;
  description: string;
  status: ProjectStatus;
  createdBy: string;
  createdByName: string;
  createdAt: string;
  investigators: InvestigatorResponse[];
}

export interface ProjectSummaryResponse {
  projectId: string;
  name: string;
  status: ProjectStatus;
  createdAt: string;
}

export interface InvestigatorResponse {
  userId: string;
  name: string;
  email: string;
  addedAt: string;
}

// Experiment
export type ExperimentStatus = 'DRAFT' | 'OPEN' | 'CLOSED' | 'ARCHIVED';

export interface ExperimentCreateRequest {
  name: string;
  description?: string;
  location?: string;
  durationMinutes: number;
  maxParticipantsPerSlot: number;
  slug: string;
  allowDuplicateEmail?: boolean;
  allowParticipantCancel?: boolean;
}

export interface ExperimentUpdateRequest {
  name: string;
  description?: string;
  location?: string;
  durationMinutes: number;
  maxParticipantsPerSlot: number;
  allowDuplicateEmail?: boolean;
  allowParticipantCancel?: boolean;
}

export interface ExperimentStatusUpdateRequest { status: ExperimentStatus; }

export interface FormField {
  key: string;
  label: string;
  visible: boolean;
  required: boolean;
  locked?: boolean;
}
export interface FormConfig { fields: FormField[]; }

export interface NotificationConfig {
  enabled: boolean;
  onRegistration: boolean;
  reminders: number[];
}

export interface ExperimentDetailResponse {
  experimentId: string;
  projectId: string;
  name: string;
  description: string;
  location: string;
  durationMinutes: number;
  maxParticipantsPerSlot: number;
  slug: string;
  status: ExperimentStatus;
  allowDuplicateEmail: boolean;
  allowParticipantCancel: boolean;
  formConfig: FormConfig;
  notificationConfig: NotificationConfig;
  createdBy: string;
  createdByName: string;
  createdAt: string;
}

export interface ExperimentSummaryResponse {
  experimentId: string;
  name: string;
  slug: string;
  status: ExperimentStatus;
  durationMinutes: number;
  createdAt: string;
}

// Slot
export type TimeSlotStatus = 'AVAILABLE' | 'FULL' | 'CANCELLED';

export interface SlotCreateRequest { startTime: string; endTime: string; capacity?: number; }
export interface SlotUpdateRequest { startTime: string; endTime: string; capacity: number; }
export interface SlotBatchCreateRequest {
  startDate: string;
  endDate: string;
  daysOfWeek: string[];
  dailyStartTime: string;
  dailyEndTime: string;
  durationMinutes: number;
  breakMinutes?: number;
  capacity?: number;
}

export interface TimeSlotResponse {
  slotId: string;
  experimentId: string;
  startTime: string;
  endTime: string;
  capacity: number;
  currentCount: number;
  status: TimeSlotStatus;
  createdAt: string;
}

// Registration
export type RegistrationStatus = 'CONFIRMED' | 'CANCELLED' | 'NO_SHOW';

export interface ParticipantRegistrationRequest {
  slotId: string;
  email: string;
  name?: string;
  phone?: string;
  studentId?: string;
  age?: number;
  gender?: string;
  dominantHand?: string;
  notes?: string;
}

export interface RegistrationStatusUpdateRequest { status: RegistrationStatus; }

export interface RegistrationResponse {
  registrationId: string;
  slotId: string;
  participantEmail: string;
  participantName: string;
  participantPhone: string;
  participantStudentId: string;
  participantAge: number;
  participantGender: string;
  participantDominantHand: string;
  participantNotes: string;
  status: RegistrationStatus;
  registeredAt: string;
  cancelledAt: string;
}

export interface PublicExperimentResponse {
  name: string;
  description: string;
  location: string;
  durationMinutes: number;
  formConfig: FormConfig;
  availableSlots: PublicTimeSlotResponse[];
}

export interface PublicTimeSlotResponse {
  slotId: string;
  startTime: string;
  endTime: string;
  capacity: number;
  currentCount: number;
  remainingSpots: number;
}

export interface PublicRegistrationResponse {
  registrationId: string;
  email: string;
  name: string;
  slotStartTime: string;
  slotEndTime: string;
  experimentName: string;
  location: string;
  status: RegistrationStatus;
  cancelToken: string;
  registeredAt: string;
}
