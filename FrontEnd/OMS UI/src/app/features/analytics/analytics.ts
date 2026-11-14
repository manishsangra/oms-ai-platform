
import {
  Component,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

import { Order } from '../../core/models/order.model';
import { OrderService } from '../../core/services/order.service';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './analytics.html',
  styleUrl: './analytics.scss'
})
export class Analytics implements OnInit {

  private orderService = inject(OrderService);
  private router = inject(Router);

  orders = signal<Order[]>([]);
  loading = signal(false);
  errorMessage = signal('');

  totalOrders = computed(() => this.orders().length);

  totalRevenue = computed(() =>
    this.orders().reduce(
      (total, order) => total + order.totalAmount,
      0
    )
  );

  pendingOrders = computed(() =>
    this.orders().filter(
      order => order.status?.toUpperCase() === 'PENDING'
    ).length
  );

  completedOrders = computed(() =>
    this.orders().filter(
      order => order.status?.toUpperCase() === 'COMPLETED'
    ).length
  );

  cancelledOrders = computed(() =>
    this.orders().filter(
      order => order.status?.toUpperCase() === 'CANCELLED'
    ).length
  );

  totalItems = computed(() =>
    this.orders().reduce(
      (total, order) =>
        total + order.items.reduce(
          (itemTotal, item) => itemTotal + item.quantity,
          0
        ),
      0
    )
  );

  averageOrderValue = computed(() =>
    this.totalOrders() > 0
      ? this.totalRevenue() / this.totalOrders()
      : 0
  );

  currency = computed(() =>
    this.orders()[0]?.currency || 'INR'
  );

  recentOrders = computed(() =>
    [...this.orders()]
      .sort(
        (a, b) =>
          new Date(b.createdAt).getTime() -
          new Date(a.createdAt).getTime()
      )
      .slice(0, 5)
  );

  statusBreakdown = computed(() => {
    const orders = this.orders();
    const statuses = ['PENDING', 'COMPLETED', 'CANCELLED'];

    return statuses.map(status => {
      const count = orders.filter(
        order => order.status?.toUpperCase() === status
      ).length;

      return {
        status,
        count,
        percentage: orders.length
          ? (count / orders.length) * 100
          : 0
      };
    });
  });

  ngOnInit(): void {
    console.log('Analytics component initialized');
    this.loadAnalytics();
  }

  loadAnalytics(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.orderService.getOrders().subscribe({
      next: response => {
        this.orders.set(response);
        this.loading.set(false);
      },
      error: error => {
        console.error('Analytics loading failed:', error);
        this.errorMessage.set(
          'Unable to load analytics. Please try again.'
        );
        this.loading.set(false);
      }
    });
  }

  viewOrderDetails(orderId: number): void {
    this.router.navigate(['/orders', orderId]);
  }

  viewAllOrders(): void {
    this.router.navigate(['/orders']);
  }
}
