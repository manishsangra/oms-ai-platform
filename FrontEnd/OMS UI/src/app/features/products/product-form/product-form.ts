
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { ProductService } from '../../../core/services/product.service';
import { ProductRequest } from '../../../core/models/product.model';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './product-form.html',
  styleUrl: './product-form.scss'
})
export class ProductForm implements OnInit {

  private fb = inject(FormBuilder);
  private productService = inject(ProductService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  loading = false;
  errorMessage = '';

  productId: number | null = null;
  isEditMode = false;

  productForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    description: ['', Validators.required],
    price: [0, [Validators.required, Validators.min(0.01)]],
    currency: ['INR', Validators.required]
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.productId = Number(id);
      this.isEditMode = true;
      this.loadProduct(this.productId);
    }
  }

  loadProduct(id: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.productService.getProductById(id).subscribe({
      next: (product) => {
        console.log('Product received:', product);

        this.productForm.patchValue({
          name: product.name,
          description: product.description,
          price: product.price,
          currency: product.currency
        });

        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading product:', error);
        this.errorMessage = 'Unable to load product details.';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const request: ProductRequest = this.productForm.getRawValue();

    const operation = this.isEditMode && this.productId !== null
      ? this.productService.updateProduct(this.productId, request)
      : this.productService.createProduct(request);

    operation.subscribe({
      next: (product) => {
        console.log('Product saved:', product);
        this.loading = false;
        this.router.navigate(['/products']);
      },
      error: (error) => {
        console.error('Product save failed:', error);
        this.errorMessage =
          error.error?.message || 'Unable to save product.';
        this.loading = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/products']);
  }
}
