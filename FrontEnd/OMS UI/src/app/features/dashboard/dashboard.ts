
import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard {

  private authService = inject(AuthService);
  private router = inject(Router);

  username = this.authService.getUsername();
  isAdmin = this.authService.isAdmin();

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
