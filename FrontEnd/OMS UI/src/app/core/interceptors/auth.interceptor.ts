
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authService = inject(AuthService);

  const token = authService.getToken();

  if (!token || req.url.includes('/auth/login')) {
    return next(req);
  }

  const authRequest = req.clone({
    setHeaders: {
      Authorization: `${authService.getTokenType()} ${token}`
    }
  });

  return next(authRequest);
};
