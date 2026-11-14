
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Product, ProductRequest } from '../models/product.model';
import { API_CONFIG } from '../auth/api.config';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private http = inject(HttpClient);

  private readonly productsUrl =
    `${API_CONFIG.gatewayUrl}${API_CONFIG.endpoints.products}`;

  // Get all products
  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(this.productsUrl);
  }

  // Get product by ID
  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.productsUrl}/${id}`);
  }

  // Create product
  createProduct(request: ProductRequest): Observable<Product> {
    return this.http.post<Product>(this.productsUrl, request);
  }

  // Update product
  updateProduct(
    id: number,
    request: ProductRequest
  ): Observable<Product> {
    return this.http.put<Product>(`${this.productsUrl}/${id}`, request);
  }

  // Delete product
  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.productsUrl}/${id}`);
  }
}
