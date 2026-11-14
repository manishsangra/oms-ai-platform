
import { Routes } from '@angular/router';

import { Login } from './features/login/login';
import { Dashboard } from './features/dashboard/dashboard';
import { ProductList } from './features/products/product-list/product-list';
import { ProductForm } from './features/products/product-form/product-form';
import { OrderList } from './features/orders/order-list/order-list';
import { OrderForm } from './features/orders/order-form/order-form';
import { OrderDetails } from './features/orders/order-details/order-details';
import { Analytics } from './features/analytics/analytics';

import {
  authGuard,
  adminGuard
} from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  { path: 'login', component: Login },

  {
    path: 'dashboard',
    component: Dashboard,
    canActivate: [authGuard]
  },

  {
    path: 'products',
    component: ProductList,
    canActivate: [authGuard]
  },

  {
    path: 'products/new',
    component: ProductForm,
    canActivate: [adminGuard]
  },
  {
    path: 'products/:id/edit',
    component: ProductForm,
    canActivate: [adminGuard]
  },
  {
    path: 'orders',
    component: OrderList,
    canActivate: [authGuard]
  },
  {
    path: 'orders/new',
    component: OrderForm,
    canActivate: [authGuard]
  },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },

  { path: 'products', component: ProductList, canActivate: [authGuard] },
  { path: 'products/new', component: ProductForm, canActivate: [adminGuard] },
  { path: 'products/:id/edit', component: ProductForm, canActivate: [adminGuard] },

  { path: 'orders', component: OrderList, canActivate: [authGuard] },
  { path: 'orders/new', component: OrderForm, canActivate: [authGuard] },
  { path: 'orders/:id', component: OrderDetails, canActivate: [authGuard] },

  { path: 'analytics', component: Analytics, canActivate: [authGuard] },


  { path: '**', redirectTo: 'login' }
];
