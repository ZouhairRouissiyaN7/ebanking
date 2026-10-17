import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

import { tap } from 'rxjs';

import { AppUser, JwtPayload, LoginRequest, LoginResponse } from '../models/auth.model';

import { environment } from '../../environments/environment.development';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly TOKEN_KEY = 'ebanking_access_token';

  currentUser = signal<AppUser | null>(null);

  isAuthenticated = computed(() => this.currentUser() !== null);

  constructor(
    private http: HttpClient,
    private router: Router,
  ) {
    this.restoreSession();
  }

  login(credentials: LoginRequest) {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, credentials).pipe(
      tap((response) => {
        localStorage.setItem(this.TOKEN_KEY, response.accessToken);

        this.restoreSession();
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    const token = localStorage.getItem(this.TOKEN_KEY);

    if (!token) {
      return null;
    }

    const payload = this.decodeToken(token);

    if (!payload || !payload.exp) {
      this.clearSession();
      return null;
    }

    if (payload.exp * 1000 <= Date.now()) {
      this.clearSession();
      return null;
    }

    return token;
  }

  isLoggedIn(): boolean {
    return this.getToken() !== null;
  }

  isAdmin(): boolean {
    return this.currentUser()?.roles.includes('ADMIN') ?? false;
  }

  isCustomer(): boolean {
    return this.currentUser()?.roles.includes('CUSTOMER') ?? false;
  }

  getDefaultRoute(): string {
    return this.isAdmin() ? '/customers' : '/accounts';
  }

  private restoreSession(): void {
    const token = this.getToken();

    if (!token) {
      this.currentUser.set(null);
      return;
    }

    const payload = this.decodeToken(token);

    if (!payload || !payload.sub || !Array.isArray(payload.roles)) {
      this.clearSession();
      return;
    }

    this.currentUser.set({
      username: payload.sub,
      roles: payload.roles,
      customerId: payload.customerId ?? null,
    });
  }

  private clearSession(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.currentUser.set(null);
  }

  private decodeToken(token: string): JwtPayload | null {
    try {
      const payload = token.split('.')[1];

      if (!payload) return null;

      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');

      const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=');

      return JSON.parse(atob(padded)) as JwtPayload;
    } catch {
      return null;
    }
  }
}
