import { Routes } from '@angular/router';
import { authGuard, adminGuard, publicGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'labs', pathMatch: 'full' },

  {
    path: 'login',
    canActivate: [publicGuard],
    loadComponent: () => import('./features/auth/login/login').then(m => m.LoginComponent),
  },
  {
    path: 'register',
    canActivate: [publicGuard],
    loadComponent: () => import('./features/auth/register/register').then(m => m.RegisterComponent),
  },

  {
    path: 'e/:slug',
    loadComponent: () =>
      import('./features/public/experiment-register/experiment-register').then(
        m => m.ExperimentRegisterComponent,
      ),
  },
  {
    path: 'registration/:cancelToken',
    loadComponent: () =>
      import('./features/public/registration-status/registration-status').then(
        m => m.RegistrationStatusComponent,
      ),
  },
  {
    path: 'invite/:token',
    loadComponent: () =>
      import('./features/invitation/invitation-accept/invitation-accept').then(
        m => m.InvitationAcceptComponent,
      ),
  },

  {
    path: 'labs',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./shared/components/layout/layout').then(m => m.LayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/labs/lab-list/lab-list').then(m => m.LabList),
      },
      {
        path: 'create',
        loadComponent: () =>
          import('./features/labs/lab-create/lab-create').then(m => m.LabCreate),
      },
      {
        path: ':labId',
        loadComponent: () =>
          import('./features/labs/lab-detail/lab-detail').then(m => m.LabDetail),
      },
      {
        path: ':labId/members',
        loadComponent: () =>
          import('./features/labs/lab-members/lab-members').then(m => m.LabMembers),
      },
      {
        path: ':labId/invitations',
        loadComponent: () =>
          import('./features/labs/lab-invitations/lab-invitations').then(
            m => m.LabInvitations,
          ),
      },
      {
        path: ':labId/projects/:projectId',
        loadComponent: () =>
          import('./features/projects/project-detail/project-detail').then(
            m => m.ProjectDetail,
          ),
      },
    ],
  },

  {
    path: 'experiments/:experimentId',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./shared/components/layout/layout').then(m => m.LayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/experiments/experiment-detail/experiment-detail').then(
            m => m.ExperimentDetail,
          ),
      },
      {
        path: 'slots',
        loadComponent: () =>
          import('./features/experiments/experiment-slots/experiment-slots').then(
            m => m.ExperimentSlots,
          ),
      },
      {
        path: 'registrations',
        loadComponent: () =>
          import('./features/experiments/experiment-registrations/experiment-registrations').then(
            m => m.ExperimentRegistrations,
          ),
      },
    ],
  },

  {
    path: 'admin/labs',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./shared/components/layout/layout').then(m => m.LayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/admin/admin-labs/admin-labs').then(m => m.AdminLabsComponent),
      },
    ],
  },
];
