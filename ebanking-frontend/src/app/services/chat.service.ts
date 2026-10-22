import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { environment } from '../../environments/environment.development';

export interface ChatResponse {
  reply: string;
}

@Injectable({
  providedIn: 'root',
})
export class ChatService {
  private readonly http = inject(HttpClient);

  send(message: string) {
    return this.http.post<ChatResponse>(`${environment.apiUrl}/chat`, { message });
  }
}
