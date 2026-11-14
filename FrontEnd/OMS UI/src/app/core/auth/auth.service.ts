
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, JwtPayload } from './auth.model';
import { API_CONFIG } from './api.config';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private http = inject(HttpClient);

  private readonly TOKEN_KEY = 'oms_access_token';
  private readonly TOKEN_TYPE_KEY = 'oms_token_type';
  private readonly EXPIRY_KEY = 'oms_token_expiry';

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${API_CONFIG.authUrl}/login`,
      request
    ).pipe(
      tap(response => {
        localStorage.setItem(this.TOKEN_KEY, response.accessToken);
        localStorage.setItem(this.TOKEN_TYPE_KEY, response.tokenType);

        const expiry = Date.now() +
          response.expiresInSeconds * 1000;

        localStorage.setItem(this.EXPIRY_KEY, expiry.toString());
      })
    );
  }

  getToken(): string | null {
    if (!this.isAuthenticated()) {
      return null;
    }

    return localStorage.getItem(this.TOKEN_KEY);
  }

  getTokenType(): string {
    return localStorage.getItem(this.TOKEN_TYPE_KEY) || 'Bearer';
  }

  isAuthenticated(): boolean {
    const token = localStorage.getItem(this.TOKEN_KEY);
    const expiry = Number(localStorage.getItem(this.EXPIRY_KEY));

    return !!token && !!expiry && Date.now() < expiry;
  }

  getAuthorities(): string[] {
    const token = localStorage.getItem(this.TOKEN_KEY);

    if (!token) {
      return [];
    }

    try {
      const payload = this.decodeToken(token);

      const authorities = payload['authorities'];
      const roles = payload['roles'];
      const scope = payload['scope'];

      if (Array.isArray(authorities)) {
        return authorities.map(String);
      }

      if (Array.isArray(roles)) {
        return roles.map(String);
      }

      if (typeof scope === 'string') {
        return scope.split(' ');
      }

      return [];
    } catch {
      return [];
    }
  }

  isAdmin(): boolean {
    return this.getAuthorities().some(
      authority =>
        authority === 'ROLE_ADMIN' ||
        authority === 'ADMIN'
    );
  }

  getUsername(): string {
    const token = localStorage.getItem(this.TOKEN_KEY);

    if (!token) {
      return '';
    }

    try {
      return this.decodeToken(token).sub || '';
    } catch {
      return '';
    }
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.TOKEN_TYPE_KEY);
    localStorage.removeItem(this.EXPIRY_KEY);
  }

  private decodeToken(token: string): JwtPayload {
    const payload = token.split('.')[1];

    if (!payload) {
      throw new Error('Invalid JWT');
    }

    const base64 = payload
      .replace(/-/g, '+')
      .replace(/_/g, '/');

    return JSON.parse(
      decodeURIComponent(
        Array.from(atob(base64))
          .map(char =>
            '%' + char.charCodeAt(0).toString(16).padStart(2, '0')
          )
          .join('')
      )
    ) as JwtPayload;
  }
}
