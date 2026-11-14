
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

// Chat request
export interface ChatRequest {
  conversationId: string;
  message: string;
}

// Chat response
export interface ChatResponse {
  conversationId: string;
  response: string;
}

// Weather response
export interface WeatherSummary {
  city: string;
  temperature: number;
  humidity: number;
  condition: string;
  recommendation: string;
}

// Document upload response
export interface DocumentUploadResponse {
  message: string;
  filename: string;
  totalChunks: number;
}

// RAG request
export interface DocumentQuestionRequest {
  question: string;
}

// RAG response
export interface DocumentQuestionResponse {
  answer: string;
}

@Injectable({
  providedIn: 'root'
})
export class AiService {

  private baseUrl = 'http://localhost:8085';

  private apiUrl = `${this.baseUrl}/api/ai/chat`;

  constructor(private http: HttpClient) {}

  // Existing normal chat API
  sendMessage(request: ChatRequest): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(
      this.apiUrl,
      request
    );
  }

  // Existing structured weather API
  getStructuredWeather(
    city: string,
    conversationId: string
  ): Observable<WeatherSummary> {

    const params = new HttpParams()
      .set('city', city)
      .set('conversationId', conversationId);

    return this.http.get<WeatherSummary>(
      `${this.baseUrl}/api/ai/structured-weather`,
      { params }
    );
  }

  // Upload document for RAG
  uploadDocument(
    file: File
  ): Observable<DocumentUploadResponse> {

    const formData = new FormData();

    formData.append('file', file);

    return this.http.post<DocumentUploadResponse>(
      `${this.baseUrl}/api/documents/upload`,
      formData
    );
  }

  // Ask question using RAG
  askDocument(
    question: string
  ): Observable<DocumentQuestionResponse> {

    const request: DocumentQuestionRequest = {
      question: question
    };

    return this.http.post<DocumentQuestionResponse>(
      `${this.baseUrl}/api/documents/ask`,
      request
    );
  }
}
