
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { Product } from '../../../core/models/product.model';
import { ProductService } from '../../../core/services/product.service';
import { OrderService } from '../../../core/services/order.service';

import { CreateOrderRequest } from '../../../core/models/order.model';

interface SelectedOrderItem {
  product: Product;
  quantity: number;
}

@Component({
  selector: 'app-order-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './order-form.html',
  styleUrl: './order-form.scss'
})
export class OrderForm implements OnInit {

  private productService = inject(ProductService);
  private orderService = inject(OrderService);
  private router = inject(Router);

  products = signal<Product[]>([]);
  selectedItems = signal<SelectedOrderItem[]>([]);

  customerId = signal<number | null>(null);
  selectedProductId = signal<number | null>(null);
  selectedQuantity = signal(1);

  loadingProducts = signal(false);
  submitting = signal(false);
  errorMessage = signal('');
  successMessage = signal('');

  totalAmount = computed(() =>
    this.selectedItems().reduce(
      (total, item) => total + item.product.price * item.quantity,
      0
    )
  );

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loadingProducts.set(true);

    this.productService.getProducts().subscribe({
      next: (response) => {
        this.products.set(response);
        this.loadingProducts.set(false);
      },
      error: (error) => {
        console.error('Error loading products:', error);
        this.errorMessage.set('Unable to load products.');
        this.loadingProducts.set(false);
      }
    });
  }

  addItem(): void {
    this.errorMessage.set('');

    if (!this.selectedProductId()) {
      this.errorMessage.set('Please select a product.');
      return;
    }

    if (this.selectedQuantity() < 1) {
      this.errorMessage.set('Quantity must be at least 1.');
      return;
    }

    const product = this.products().find(
      item => item.id === this.selectedProductId()
    );

    if (!product) {
      this.errorMessage.set('Selected product was not found.');
      return;
    }

    const existingItem = this.selectedItems().find(
      item => item.product.id === product.id
    );

    if (existingItem) {
      this.selectedItems.update(items =>
        items.map(item =>
          item.product.id === product.id
            ? { ...item, quantity: item.quantity + this.selectedQuantity() }
            : item
        )
      );
    } else {
      this.selectedItems.update(items => [
        ...items,
        {
          product,
          quantity: this.selectedQuantity()
        }
      ]);
    }

    this.selectedProductId.set(null);
    this.selectedQuantity.set(1);
  }

  updateItemQuantity(productId: number, quantity: number): void {
    if (!Number.isInteger(quantity) || quantity < 1) {
      return;
    }

    this.selectedItems.update(items =>
      items.map(item =>
        item.product.id === productId
          ? { ...item, quantity }
          : item
      )
    );
  }

  removeItem(productId: number): void {
    this.selectedItems.update(items =>
      items.filter(item => item.product.id !== productId)
    );
  }

  createOrder(): void {
    this.errorMessage.set('');
    this.successMessage.set('');

    const customerId = this.customerId();

    if (customerId === null || customerId < 1) {
      this.errorMessage.set('Please enter a valid Customer ID.');
      return;
    }

    if (this.selectedItems().length === 0) {
      this.errorMessage.set('Please add at least one product.');
      return;
    }

    const request: CreateOrderRequest = {
      customerId: customerId,
      items: this.selectedItems().map(item => ({
        productId: item.product.id,
        quantity: item.quantity
      }))
    };

    this.submitting.set(true);

    this.orderService.createOrder(request).subscribe({
      next: (response) => {
        this.submitting.set(false);
        this.successMessage.set(
          `Order #${response.id} created successfully!`
        );

        setTimeout(() => {
          this.router.navigate(['/orders']);
        }, 1000);
      },
      error: (error) => {
        console.error('Error creating order:', error);
        this.submitting.set(false);
        this.errorMessage.set(
          error.error?.message || 'Unable to create order. Please try again.'
        );
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/orders']);
  }
}
