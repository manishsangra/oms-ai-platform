
export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface Order {
  id: number;
  customerId: number;
  totalAmount: number;
  currency: string;
  status: string;
  createdAt: string;
  items: OrderItem[];
}

export interface CreateOrderItemRequest {
  productId: number;
  quantity: number;
}

export interface CreateOrderRequest {
  customerId: number;
  items: CreateOrderItemRequest[];
}
