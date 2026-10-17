import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  const isApiRequest =
    req.url.startsWith('/api/') || req.url.startsWith('http://localhost:8888/api/');

  const isLoginRequest = req.url.endsWith('/auth/login');

  const modifiedRequest =
    token && isApiRequest && !isLoginRequest
      ? req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`,
          },
        })
      : req;

  return next(modifiedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && isApiRequest && !isLoginRequest && authService.isLoggedIn()) {
        authService.logout();
      }

      return throwError(() => error);
    }),
  );
};
