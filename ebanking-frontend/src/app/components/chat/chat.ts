import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { ChatService } from '../../services/chat.service';

interface ChatMessage {
  role: 'user' | 'assistant';
  text: string;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './chat.html',
})
export class Chat {
  private readonly chatService = inject(ChatService);

  input = signal('');
  messages = signal<ChatMessage[]>([]);
  loading = signal(false);
  errorMessage = signal('');

  sendMessage(): void {
    const message = this.input().trim();

    if (!message || this.loading()) return;

    this.messages.update((messages) => [...messages, { role: 'user', text: message }]);

    this.input.set('');
    this.errorMessage.set('');
    this.loading.set(true);

    this.chatService
      .send(message)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => {
          this.messages.update((messages) => [
            ...messages,
            {
              role: 'assistant',
              text: response.reply,
            },
          ]);
        },
        error: (err) => {
          this.errorMessage.set(
            err.status === 503
              ? 'The local AI model is unavailable.'
              : (err.error?.detail ?? 'Unable to send message.'),
          );
        },
      });
  }
}
