
import {
  Component,
  ChangeDetectorRef,
  ElementRef,
  ViewChild
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  AiService,
  WeatherSummary,
  ChatResponse,
  DocumentQuestionResponse
} from './services/ai.service';

import { marked } from 'marked';
import DOMPurify from 'dompurify';

import { Observable } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

// Response type for both chat APIs
type ChatApiResponse = ChatResponse | DocumentQuestionResponse;

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {

  @ViewChild('chatBody')
  chatBody!: ElementRef;

  // Unique conversation ID
  conversationId = crypto.randomUUID();

  // Weather properties
  weatherCity = '';
  weatherSummary: WeatherSummary | null = null;
  weatherLoading = false;
  weatherError = '';

  // Chat properties
  userMessage = '';
  isLoading = false;

  messages: {
    role: 'user' | 'assistant';
    content: string;
  }[] = [];

  // Chat mode
  chatMode: 'normal' | 'document' = 'normal';

  // Document upload properties
  selectedFile: File | null = null;
  isUploading = false;
  uploadMessage = '';
  uploadError = '';

  constructor(
    private aiService: AiService,
    private cdr: ChangeDetectorRef
  ) {}


  // Select document
  onFileSelected(event: Event): void {

    const input = event.target as HTMLInputElement;

    this.selectedFile = input.files?.[0] ?? null;

    this.uploadMessage = '';
    this.uploadError = '';
  }


  // Upload selected document
  uploadSelectedDocument(): void {

    if (this.isUploading) {
      return;
    }

    const file = this.selectedFile;

    if (!file) {
      return;
    }

    this.isUploading = true;
    this.uploadMessage = '';
    this.uploadError = '';

    this.aiService.uploadDocument(file)
      .subscribe({

        next: (response) => {

          this.uploadMessage =
            `${response.filename} uploaded successfully. ` +
            `${response.totalChunks} chunks created.`;

          // Switch to Document Q&A after successful upload
          this.chatMode = 'document';

          this.selectedFile = null;
          this.isUploading = false;

          this.cdr.detectChanges();
        },

        error: (error: HttpErrorResponse) => {

          console.error('Document Upload Error:', error);

          this.uploadError =
            'Document upload failed. Please try again.';

          this.isUploading = false;

          this.cdr.detectChanges();
        }

      });
  }


  // Send chat message
  sendMessage(): void {

    const message = this.userMessage.trim();

    if (!message || this.isLoading) {
      return;
    }

    // Display user message
    this.messages.push({
      role: 'user',
      content: message
    });

    this.userMessage = '';
    this.isLoading = true;

    this.cdr.detectChanges();
    this.scrollToBottom();

    // Select API based on chat mode
    const request$: Observable<ChatApiResponse> =
      this.chatMode === 'document'
        ? this.aiService.askDocument(message)
        : this.aiService.sendMessage({
          conversationId: this.conversationId,
          message: message
        });

    request$.subscribe({

      next: (response: ChatApiResponse) => {

        const answer = 'answer' in response
          ? response.answer
          : response.response;

        this.messages.push({
          role: 'assistant',
          content: answer
        });

        this.isLoading = false;

        this.cdr.detectChanges();
        this.scrollToBottom();
      },

      error: (error: HttpErrorResponse) => {

        console.error('AI API Error:', error);

        this.messages.push({
          role: 'assistant',
          content: 'Sorry, something went wrong. Please try again.'
        });

        this.isLoading = false;

        this.cdr.detectChanges();
        this.scrollToBottom();
      }

    });
  }


  // Get structured weather summary
  getWeatherSummary(): void {

    const city = this.weatherCity.trim();

    if (!city || this.weatherLoading) {
      return;
    }

    this.weatherLoading = true;
    this.weatherSummary = null;
    this.weatherError = '';

    this.aiService.getStructuredWeather(
      city,
      this.conversationId
    ).subscribe({

      next: (response: WeatherSummary) => {

        this.weatherSummary = response;
        this.weatherLoading = false;
      },

      error: (error: HttpErrorResponse) => {

        console.error('Weather API Error:', error);

        this.weatherError =
          'Unable to fetch weather information. Please try again.';

        this.weatherLoading = false;
      }

    });
  }


  // Automatically scroll chat to bottom
  scrollToBottom(): void {

    requestAnimationFrame(() => {

      if (this.chatBody) {

        const element = this.chatBody.nativeElement;

        element.scrollTo({
          top: element.scrollHeight,
          behavior: 'smooth'
        });

      }

    });
  }


  // Convert Markdown into HTML
  renderMarkdown(content: string): string {

    const html = marked.parse(content, {
      gfm: true,
      breaks: true
    }) as string;

    // Wrap tables for horizontal scrolling
    const wrappedHtml = html
      .replace(/<table>/gi, '<div class="table-wrapper"><table>')
      .replace(/<\/table>/gi, '</table></div>');

    // Sanitize generated HTML
    return DOMPurify.sanitize(wrappedHtml);
  }

}
