
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
}

export interface JwtPayload {
  sub?: string;
  roles?: string[];
  authorities?: string[];
  exp?: number;
  [key: string]: unknown;
}
