package com.example.springaiassistant.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeTools {

    private static final ZoneId INDIA_ZONE =
            ZoneId.of("Asia/Kolkata");

    @Tool(description = """
            Get the actual current date and time in India.
            Always use this tool when the user asks for:
            current date, today's date, current time,
            date and time now, or what time it is.
            Never guess or invent the current date or time.
            """)

    public String getCurrentDateTime() {

        LocalDateTime now =
                LocalDateTime.now(INDIA_ZONE);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "EEEE, dd MMMM yyyy, hh:mm:ss a"
                );

        return now.format(formatter)
                + " IST (Asia/Kolkata)";
    }
}