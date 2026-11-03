package com.example.giaydo

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.giaydo.ui.theme.GiaydoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GiaydoTheme {
                ProductDetailScreen()
            }
        }
    }
}

data class Product(
    val id: String = "",
    val name: String = "",
    val des: String = "",
    val price: Double = 0.0,
    val imgURL: String = ""
)

suspend fun fetchProductFromApi(): Product? = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://mock.apidog.com/m1/890655-872447-default/v2/product")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 6000
        connection.readTimeout = 6000
        if (connection.responseCode == 200) {
            val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(jsonString)
            Product(
                id = json.optString("id"),
                name = json.optString("name"),
                des = json.optString("des"),
                price = json.optDouble("price", 0.0),
                imgURL = json.optString("imgURL")
            )
        } else null
    } catch (_: Exception) {
        null
    }
}

suspend fun fetchBitmapFromUrl(urlString: String): ImageBitmap? = withContext(Dispatchers.IO) {
    try {
        if (urlString.isBlank()) return@withContext null
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 6000
        connection.readTimeout = 6000
        if (connection.responseCode == 200) {
            val bitmap = BitmapFactory.decodeStream(connection.inputStream)
            bitmap?.asImageBitmap()
        } else null
    } catch (_: Exception) {
        null
    }
}

@Composable
fun BackArrowButton(onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .padding(start = 12.dp)
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0xFF00A0E9))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(14.dp)) {
            val path = Path().apply {
                moveTo(size.width * 0.65f, size.height * 0.15f)
                lineTo(size.width * 0.25f, size.height * 0.5f)
                lineTo(size.width * 0.65f, size.height * 0.85f)
            }
            drawPath(
                path = path,
                color = Color.White,
                style = Stroke(width = 5f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen() {
    var productState by remember { mutableStateOf<Product?>(null) }
    var remoteBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var apiStatusMessage by remember { mutableStateOf("Đang tải dữ liệu...") }
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            isLoading = true
            apiStatusMessage = "Đang gọi API GET..."
            val result = fetchProductFromApi()
            if (result != null) {
                productState = result
                apiStatusMessage = "Gọi API thành công!"
                if (result.imgURL.isNotBlank()) {
                    val bitmap = fetchBitmapFromUrl(result.imgURL)
                    remoteBitmap = bitmap
                }
            } else {
                apiStatusMessage = "Không kết nối được API, hiển thị dữ liệu mẫu"
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Product detail",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF00A0E9),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    BackArrowButton()
                },
                actions = {
                    Spacer(modifier = Modifier.width(46.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFECEFF1)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // API Control & Status Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GET | mock.apidog.com/.../v2/product",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0288D1)
                        )
                        Text(
                            text = apiStatusMessage,
                            fontSize = 12.sp,
                            color = if (productState != null) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                        )
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color(0xFF00A0E9),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Button(
                            onClick = { loadData() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A0E9)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(text = "Gọi API", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }

            // Main Product Detail Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Shoe Image Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE84125)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (remoteBitmap != null) {
                            Image(
                                bitmap = remoteBitmap!!,
                                contentDescription = "Product Image from API",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.shoe),
                                contentDescription = "Giày Nike",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    val titleText = if (productState != null && productState!!.name.isNotBlank()) {
                        "${productState!!.name} - Giày Nike Nam Nữ Chính Hãng"
                    } else {
                        "Giày Nike Nam Nữ Chính Hãng - Nike Air Force 1 '07 LV8 - Màu Trắng | JapanSport HF2898-100"
                    }

                    Text(
                        text = titleText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D2939),
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Price
                    val formattedPrice = if (productState != null && productState!!.price > 0) {
                        val nf = NumberFormat.getInstance()
                        "Giá: ${nf.format(productState!!.price)}đ"
                    } else {
                        "Giá: 4.000.000đ"
                    }

                    Text(
                        text = formattedPrice,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description
                    val descriptionText = if (productState != null && productState!!.des.isNotBlank()) {
                        productState!!.des
                    } else {
                        "Với giày chạy bộ, từng gram đều quan trọng. Đó là lý do tại sao đế giữa LIGHTSTRIKE PRO mới nhẹ hơn so với phiên bản trước. Mút foam đế giữa siêu nhẹ và thoải mái này có lớp đệm đàn hồi được thiết kế để hạn chế tiêu hao năng lượng. Trong các mẫu giày tập luyện, công nghệ này được thiết kế nhằm hỗ trợ cơ bắp của vận động viên để họ có thể phục hồi nhanh hơn giữa các cuộc đua."
                    }

                    Text(
                        text = descriptionText,
                        fontSize = 13.sp,
                        color = Color(0xFF475467),
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProductDetailScreenPreview() {
    GiaydoTheme {
        ProductDetailScreen()
    }
}
