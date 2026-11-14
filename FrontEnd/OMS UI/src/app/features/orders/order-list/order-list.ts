
import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

import { Order } from '../../../core/models/order.model';
import { OrderService } from '../../../core/services/order.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './order-list.html',
  styleUrl: './order-list.scss'
})
export class OrderList implements OnInit {

  private orderService = inject(OrderService);

  private router = inject(Router);

  createNewOrder(): void {
    this.router.navigate(['/orders/new']);
  }

  viewOrderDetails(orderId: number): void {
    this.router.navigate(['/orders', orderId]);
  }

  orders = signal<Order[]>([]);
  loading = signal(false);
  errorMessage = signal('');

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.orderService.getOrders().subscribe({
      next: (response) => {
        console.log('Orders API Response:', response);

        this.orders.set(response);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error fetching orders:', error);
        this.errorMessage.set('Unable to load orders. Please try again.');
        this.loading.set(false);
      }
    });
  }
}
