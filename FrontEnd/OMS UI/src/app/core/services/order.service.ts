
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  Order,
  CreateOrderRequest
} from '../models/order.model';

import { API_CONFIG } from '../auth/api.config';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private http = inject(HttpClient);

  private readonly ordersUrl =
    `${API_CONFIG.gatewayUrl}${API_CONFIG.endpoints.orders}`;

  // Get all orders
  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.ordersUrl);
  }

  // Get order by ID
  getOrderById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.ordersUrl}/${id}`);
  }

  // Create a new order
  createOrder(request: CreateOrderRequest): Observable<Order> {
    return this.http.post<Order>(this.ordersUrl, request);
  }
}
