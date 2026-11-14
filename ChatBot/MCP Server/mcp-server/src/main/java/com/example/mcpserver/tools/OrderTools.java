
package com.example.mcpserver.tools;

import com.example.mcpserver.dto.OrderResponse;
import com.example.mcpserver.service.OrderCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
public class OrderTools {

    private static final Logger log =
            LoggerFactory.getLogger(OrderTools.class);

    private final OrderCacheService orderCacheService;

    public OrderTools(OrderCacheService orderCacheService) {
        this.orderCacheService = orderCacheService;
    }

    @McpTool(
            name = "get_order_status",
            description = "Get the current status of an order using its numeric order ID"
    )
    public String getOrderStatus(
            @McpToolParam(
                    description = "Numeric order ID",
                    required = true
            )
            Long orderId) {

        log.info("Getting order status for ID: {}", orderId);

        if (orderId == null || orderId <= 0) {
            return "Please provide a valid positive order ID.";
        }

        try {
            OrderResponse order =
                    orderCacheService.getOrder(orderId);

            return "Order ID: " + order.orderId()
                    + ", Customer ID: " + order.customerId()
                    + ", Status: " + order.status()
                    + ", Currency: " + order.currency()
                    + ", Total Amount: " + order.totalAmount();

        } catch (HttpClientErrorException.NotFound exception) {

            log.warn("Order not found: {}", orderId);
            return "Order not found with ID: " + orderId;

        } catch (HttpClientErrorException.Unauthorized |
                 HttpClientErrorException.Forbidden exception) {

            log.error("Authentication/authorization failure for order: {}",
                    orderId);

            return "Authentication failed or you are not authorized to access this order.";

        } catch (Exception exception) {

            log.error("Error retrieving order: {}", orderId, exception);

            return "Unable to retrieve order details currently. Please try again later.";
        }
    }


    @McpTool(
            name = "get_order_details",
            description = """
                    Available tools:
       
                   1. get_order_status
                       - Retrieves the current status of an order using its order ID.
        
                    2. get_order_details
                       - Retrieves complete order details using its order ID.
                       - Returns customer ID, order status, total amount, currency,
                         creation date, product names, product IDs, quantities,
                         unit prices and line totals.
        
                    When asked which tools are available, list all available tools,
                    including MCP tools and locally registered tools.
                    Do not claim a tool is available unless it is actually registered.
        """
    )
    public String getOrderDetails(
            @McpToolParam(
                    description = "Numeric order ID",
                    required = true
            )
            Long orderId) {

        log.info("Getting complete order details for ID: {}", orderId);

        if (orderId == null || orderId <= 0) {
            return "Please provide a valid positive order ID.";
        }

        try {
            OrderResponse order = orderCacheService.getOrder(orderId);

            StringBuilder response = new StringBuilder();

            response.append("Order Details\n");
            response.append("----------------------\n");
            response.append("Order ID: ").append(order.orderId()).append("\n");
            response.append("Customer ID: ").append(order.customerId()).append("\n");
            response.append("Status: ").append(order.status()).append("\n");
            response.append("Total Amount: ")
                    .append(order.currency()).append(" ")
                    .append(order.totalAmount()).append("\n");
            response.append("Created At: ").append(order.createdAt()).append("\n");

            response.append("\nOrder Items:\n");

            if (order.items() != null && !order.items().isEmpty()) {

                int itemNumber = 1;

                for (OrderResponse.OrderItem item : order.items()) {

                    response.append("\nItem ").append(itemNumber++).append("\n");
                    response.append("Product ID: ").append(item.productId()).append("\n");
                    response.append("Product Name: ").append(item.productName()).append("\n");
                    response.append("Quantity: ").append(item.quantity()).append("\n");
                    response.append("Unit Price: ").append(item.unitPrice()).append("\n");
                    response.append("Line Total: ").append(item.lineTotal()).append("\n");
                }

            } else {
                response.append("No order items found.\n");
            }

            log.info("Successfully retrieved complete order details: {}", orderId +":"+ response.toString());

            return response.toString();

        } catch (HttpClientErrorException.NotFound exception) {

            log.warn("Order not found: {}", orderId);
            return "Order not found with ID: " + orderId;

        } catch (Exception exception) {

            log.error("Error retrieving order details: {}", orderId, exception);

            return "Unable to retrieve order details currently. Please try again later.";
        }
    }

}
