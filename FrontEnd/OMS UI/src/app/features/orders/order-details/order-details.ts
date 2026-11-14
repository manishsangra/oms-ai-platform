
import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

import { Order } from '../../../core/models/order.model';
import { OrderService } from '../../../core/services/order.service';

@Component({
  selector: 'app-order-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './order-details.html',
  styleUrl: './order-details.scss'
})
export class OrderDetails implements OnInit {

  private orderService = inject(OrderService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  order = signal<Order | null>(null);
  loading = signal(false);
  errorMessage = signal('');

  ngOnInit(): void {
    const orderId = Number(this.route.snapshot.paramMap.get('id'));

    if (!Number.isInteger(orderId) || orderId < 1) {
      this.errorMessage.set('Invalid order ID.');
      return;
    }

    this.loadOrder(orderId);
  }

  loadOrder(orderId: number): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.orderService.getOrderById(orderId).subscribe({
      next: (response) => {
        this.order.set(response);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error fetching order details:', error);
        this.errorMessage.set(
          error.error?.message || 'Unable to load order details.'
        );
        this.loading.set(false);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/orders']);
  }
}
