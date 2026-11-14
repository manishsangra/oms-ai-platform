
import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { ProductService } from '../../../core/services/product.service';
import { Product } from '../../../core/models/product.model';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './product-list.html',
  styleUrl: './product-list.scss'
})
export class ProductList implements OnInit {

  private productService = inject(ProductService);
  private authService = inject(AuthService);

  products = signal<Product[]>([]);
  loading = signal(false);
  errorMessage = signal('');

  isAdmin = this.authService.isAdmin();

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.productService.getProducts().subscribe({
      next: (response) => {
        console.log('Products received:', response);

        this.products.set(response);
        this.loading.set(false);

        console.log('Products count:', this.products().length);
      },

      error: (error) => {
        console.error('Error loading products:', error);

        this.errorMessage.set(
          'Unable to load products. Please try again.'
        );

        this.loading.set(false);
      }
    });
  }


  deleteProduct(product: Product): void {
    const confirmed = window.confirm(
      `Are you sure you want to delete "${product.name}"?`
    );

    if (!confirmed) {
      return;
    }

    this.productService.deleteProduct(product.id).subscribe({
      next: () => {
        console.log('Product deleted:', product.id);

        this.products.update(products =>
          products.filter(item => item.id !== product.id)
        );
      },
      error: (error) => {
        console.error('Product deletion failed:', error);
        this.errorMessage.set(
          'Unable to delete product. Please try again.'
        );
      }
    });
  }

}
