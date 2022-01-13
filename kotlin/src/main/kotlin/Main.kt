// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
//package me.steakoverflow.playground.kotlin

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.Icon
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

import org.joda.time.*

import me.steakoverflow.playground.scala.Main.google
import me.steakoverflow.playground.scala.Main.WeatherItem
import me.steakoverflow.playground.scala.Main.WeatherModel
import me.steakoverflow.playground.scala.Main.ErrorData
import org.joda.time.format.DateTimeFormat
import org.xml.sax.InputSource
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter

@Composable
@Preview
fun App() {
    var query by remember { mutableStateOf("") }
    var data by remember { mutableStateOf(listOf<WeatherItem>()) }
    var error by remember { mutableStateOf<ErrorData?>(null) }

    MaterialTheme {
        Column(Modifier.fillMaxSize().padding(10.dp), Arrangement.spacedBy(10.dp), Alignment.CenterHorizontally) {
            Row(modifier = Modifier.height(55.dp), verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    maxLines = 1,
                    shape = RoundedCornerShape(4.dp, 0.dp, 0.dp, 4.dp),
                    value = query,
                    onValueChange = { query = it }
                )
                Button(
                    modifier = Modifier.fillMaxHeight(),
                    onClick = {
                        when(val resp = google(query)) {
                            is ErrorData -> {
                                data = listOf()
                                error = resp
                            }
                            is WeatherModel -> {
                                data = resp.list()
                                error = null
                            }
                            else -> {
                                data = listOf()
                                error = ErrorData("Unknown error!")
                            }
                        }
                    },
                    shape = RoundedCornerShape(0.dp, 4.dp, 4.dp, 0.dp)
                ) {
                    Block("Get Weather", Icons.Outlined.Search)
                }
            }
            if (error != null)
                Text(error?.message().orEmpty().replaceFirstChar { it.uppercaseChar() },
                    Modifier.border(5.dp, Color.Red).padding(10.dp)
                )

            data.forEach {
                TableRow(it)
            }

        }
    }
}

@Composable
@Preview
fun Block(text: String, icon: ImageVector? = null) =
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) Icon(icon, null)
        Text(text, fontSize = 10.sp)
    }

//fun xmlToImageVector(name: String): ImageVector {
//    val img = useResource<ImageVector>("$name.xml") { loadXmlImageVector(InputSource(it), Density(1F)) }
//    println(img)
//    return img
//}

@Composable
@Preview
fun TableRow(wi: WeatherItem) =
    Row(Modifier.border(1.dp, Color.LightGray).padding(5.dp), Arrangement.spacedBy(20.dp), Alignment.CenterVertically) {
        Block(DateTimeFormat.shortDate().print(Instant.ofEpochSecond(wi.dt())))
        Block("Humidity: ${wi.humidity()}%", Icons.Outlined.CheckCircle)
        Block("Pressure: ${wi.pressure()} mbar",Icons.Outlined.CheckCircle)
        Block("Avg Temp.: ${(wi.temp().average() - BigDecimal(273.15)).setScale(2, RoundingMode.FLOOR)}°C", Icons.Outlined.CheckCircle)
        Block("Wind Speed.: ${wi.wind_speed()} m/s", Icons.Outlined.CheckCircle)
    }

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Weather App") {
        App()
    }
}
