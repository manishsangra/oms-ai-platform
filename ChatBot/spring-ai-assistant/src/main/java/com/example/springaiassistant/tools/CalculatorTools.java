
package com.example.springaiassistant.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTools {

    @Tool(
            name = "calculateSimpleInterest",
            description = "Calculate simple interest and total amount " +
                    "using principal, annual interest rate and duration in years."
    )
    public String calculateSimpleInterest(

            @ToolParam(description = "Principal amount in rupees")
            double principal,

            @ToolParam(description = "Annual interest rate in percentage")
            double annualRate,

            @ToolParam(description = "Investment duration in years")
            int years) {

        if (principal <= 0 || annualRate < 0 || years <= 0) {
            throw new IllegalArgumentException(
                    "Principal and years must be positive; rate cannot be negative."
            );
        }

        double interest = (principal * annualRate * years) / 100;

        double totalAmount = principal + interest;

        return String.format(
                "Principal: ₹%.2f, Simple Interest: ₹%.2f, Total Amount: ₹%.2f",
                principal,
                interest,
                totalAmount
        );
    }
}
