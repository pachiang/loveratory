import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { UserLoginResponse } from '../../../core/models';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.html',
  styleUrl: './layout.scss',
})
export class LayoutComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected get user(): UserLoginResponse | null {
    return this.authService.getCurrentUser();
  }

  protected get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  protected logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
