export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface AppUser {
  username: string;
  roles: string[];
  customerId: number | null;
}

export interface JwtPayload {
  sub: string;
  roles: string[];
  customerId?: number;
  exp: number;
  iss?: string;
  aud?: string[];
}
