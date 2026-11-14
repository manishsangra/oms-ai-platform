export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  currency: string;
}

export interface ProductRequest {
  name: string;
  description: string;
  price: number;
  currency: string;
}
