
package com.manish.springaiassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manish.springaiassistant.data.model.WeatherSummary

@Composable
fun WeatherCard(weather: WeatherSummary) {

    val primaryBlue = Color(0xFF2563EB)
    val darkText = Color(0xFF172554)
    val secondaryText = Color(0xFF64748B)
    val lightBlue = Color(0xFFEFF6FF)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "CURRENT WEATHER",
                    color = secondaryText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = "Weather",
                    tint = primaryBlue,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // City
            Text(
                text = weather.city,
                color = darkText,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            // Temperature and condition
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "${weather.temperature}°C",
                    color = primaryBlue,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = weather.condition,
                    color = secondaryText,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(18.dp))

            // Weather details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Humidity",
                    tint = primaryBlue,
                    modifier = Modifier.size(25.dp)
                )

                Spacer(Modifier.width(10.dp))

                Column {
                    Text(
                        text = "HUMIDITY",
                        color = secondaryText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${weather.humidity}%",
                        color = darkText,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Recommendation section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = lightBlue,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Recommendation",
                        tint = primaryBlue,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = "Recommendation",
                        color = darkText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = weather.recommendation,
                    color = Color(0xFF334155),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
