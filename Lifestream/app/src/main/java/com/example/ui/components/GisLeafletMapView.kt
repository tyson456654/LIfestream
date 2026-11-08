package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.BloodRequest
import com.example.data.model.Hospital
import com.example.data.model.User
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateGrey
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/**
 * Data model for Cadastral GIS Parcel matching the React GIS implementation.
 */
data class CadastralParcel(
    val id: String,
    val surveyNumber: String,
    val ownerName: String,
    val areaAcres: Double,
    val landClassification: String,
    val village: String,
    val district: String,
    val state: String,
    val subdivisionCode: String? = null,
    val soilType: String? = null,
    val irrigationSource: String? = null,
    val status: String, // "Verified", "Pending", "Flagged"
    val recordId: String? = null,
    val coordinates: List<Pair<Double, Double>> // [(lat, lng)]
)

val defaultCadastralParcels = listOf(
    CadastralParcel(
        id = "parcel-ap-01",
        surveyNumber = "142/2A",
        ownerName = "Venkata Rao Pola",
        areaAcres = 4.85,
        landClassification = "Wetland / Irrigated",
        village = "Guntur Rural",
        district = "Guntur",
        state = "Andhra Pradesh",
        subdivisionCode = "SUB-AP-201",
        soilType = "Black Cotton Soil",
        irrigationSource = "Krishna Canal",
        status = "Verified",
        recordId = "REC-AP-8801",
        coordinates = listOf(
            Pair(16.8920, 80.0980),
            Pair(16.8945, 80.0995),
            Pair(16.8938, 80.1030),
            Pair(16.8905, 80.1010)
        )
    ),
    CadastralParcel(
        id = "parcel-up-02",
        surveyNumber = "319/B",
        ownerName = "Ramkesh Maurya",
        areaAcres = 3.20,
        landClassification = "Agricultural Dryland",
        village = "Chandauli North",
        district = "Varanasi",
        state = "Uttar Pradesh",
        subdivisionCode = "SUB-UP-412",
        soilType = "Alluvial Loam",
        irrigationSource = "Borewell 2HP",
        status = "Pending",
        recordId = "REC-UP-9402",
        coordinates = listOf(
            Pair(25.4480, 82.8580),
            Pair(25.4510, 82.8600),
            Pair(25.4495, 82.8640),
            Pair(25.4465, 82.8615)
        )
    ),
    CadastralParcel(
        id = "parcel-tn-03",
        surveyNumber = "77/4",
        ownerName = "Murugan Arumugam",
        areaAcres = 2.45,
        landClassification = "Commercial & Health Zone",
        village = "Madurai West",
        district = "Madurai",
        state = "Tamil Nadu",
        subdivisionCode = "SUB-TN-809",
        soilType = "Red Sandy Soil",
        irrigationSource = "Vaigai Reservoir Feeder",
        status = "Flagged",
        recordId = "REC-TN-1105",
        coordinates = listOf(
            Pair(10.0460, 78.3340),
            Pair(10.0485, 78.3360),
            Pair(10.0470, 78.3395),
            Pair(10.0440, 78.3370)
        )
    ),
    CadastralParcel(
        id = "parcel-ap-04",
        surveyNumber = "142/2",
        ownerName = "Gudivada Municipal Healthcare Zone",
        areaAcres = 5.10,
        landClassification = "Institutional / Public Health",
        village = "Railway Station Road",
        district = "Krishna (Gudivada)",
        state = "Andhra Pradesh",
        subdivisionCode = "SUB-AP-GDV-04",
        soilType = "Alluvial Clay Soil",
        irrigationSource = "Krishna Delta Feeder",
        status = "Verified",
        recordId = "REC-AP-GDV-101",
        coordinates = listOf(
            Pair(16.4335, 80.9920),
            Pair(16.4355, 80.9945),
            Pair(16.4340, 80.9970),
            Pair(16.4315, 80.9940)
        )
    )
)

/**
 * JavaScript interface to receive click events from Leaflet inside WebView.
 */
class LeafletJsBridge(
    private val onParcelSelected: (String) -> Unit,
    private val onHospitalSelected: (Int) -> Unit,
    private val onUserSelected: (Int) -> Unit,
    private val onShowToast: (String) -> Unit
) {
    @JavascriptInterface
    fun onSelectParcel(parcelId: String) {
        onParcelSelected(parcelId)
    }

    @JavascriptInterface
    fun onSelectHospital(hospitalId: String) {
        hospitalId.toIntOrNull()?.let { onHospitalSelected(it) }
    }

    @JavascriptInterface
    fun onSelectUser(userId: String) {
        userId.toIntOrNull()?.let { onUserSelected(it) }
    }

    @JavascriptInterface
    fun showToast(msg: String) {
        onShowToast(msg)
    }
}

/**
 * Complete, interactive Leaflet & OpenStreetMap GIS Map Component
 * Adapted from the user's React GisMapModule with full interactive Leaflet map,
 * Roadmap / Satellite / Hybrid layer switcher, Nominatim search, quick jumps,
 * Cadastral parcel polygons, and Healthcare (Clinics, Donors, Requests) markers.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GisLeafletMapView(
    currentUser: User?,
    hospitals: List<Hospital>,
    allUsers: List<User>,
    allRequests: List<BloodRequest>,
    onSelectHospital: (Hospital) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var mapType by remember { mutableStateOf("hybrid") } // "hybrid", "roadmap", "satellite"
    var showVerified by remember { mutableStateOf(true) }
    var showPending by remember { mutableStateOf(true) }
    var showFlagged by remember { mutableStateOf(true) }
    var showClinics by remember { mutableStateOf(true) }
    var showDonors by remember { mutableStateOf(true) }
    var showUrgentRequests by remember { mutableStateOf(true) }
    var showCadastralParcels by remember { mutableStateOf(true) }

    var selectedParcel by remember { mutableStateOf<CadastralParcel?>(defaultCadastralParcels.firstOrNull()) }
    var selectedHospital by remember { mutableStateOf<Hospital?>(hospitals.firstOrNull()) }
    var mapSearchQuery by remember { mutableStateOf("") }
    var isMapLoading by remember { mutableStateOf(true) }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val initialLat = currentUser?.latitude ?: 16.4326
    val initialLon = currentUser?.longitude ?: 80.9928

    // Generate HTML for Leaflet
    val htmlContent = remember(currentUser, hospitals, allUsers, allRequests) {
        buildLeafletHtml(
            initialLat = initialLat,
            initialLon = initialLon,
            currentUser = currentUser,
            hospitals = hospitals,
            allUsers = allUsers,
            allRequests = allRequests,
            parcels = defaultCadastralParcels
        )
    }

    // Helper functions to control the WebView Leaflet instance
    fun runJs(js: String) {
        webViewRef?.post {
            webViewRef?.evaluateJavascript(js, null)
        }
    }

    fun setMapLayer(type: String) {
        mapType = type
        runJs("window.setMapLayer('$type');")
    }

    fun jumpTo(lat: Double, lng: Double, zoom: Int = 16) {
        runJs("window.jumpToRegion($lat, $lng, $zoom);")
    }

    fun filterLayers() {
        runJs(
            """
            window.updateLayerVisibility({
                showVerified: $showVerified,
                showPending: $showPending,
                showFlagged: $showFlagged,
                showClinics: $showClinics,
                showDonors: $showDonors,
                showUrgentRequests: $showUrgentRequests,
                showParcels: $showCadastralParcels
            });
            """.trimIndent()
        )
    }

    LaunchedEffect(showVerified, showPending, showFlagged, showClinics, showDonors, showUrgentRequests, showCadastralParcels) {
        filterLayers()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("gis_leaflet_map_module")
    ) {
        // TOP BANNER (Styled as in user's React component)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SlateDark),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Compass",
                                tint = Color(0xFF2DD4BF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OPEN GIS & HEALTHCARE INFRASTRUCTURE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2DD4BF),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "GIS Cadastral & Blood Bank Map",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "LEAFLET GIS",
                                    color = Color(0xFF6EE7B7),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "India-wide map visualization with live clinics, donors, and cadastral survey boundaries",
                            fontSize = 11.sp,
                            color = SlateGrey
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // QUICK JUMP BUTTONS (Matching React implementation)
                Text(
                    text = "Quick Jump:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Button(
                            onClick = {
                                if (currentUser != null) {
                                    jumpTo(currentUser.latitude, currentUser.longitude, 16)
                                } else {
                                    jumpTo(16.4326, 80.9928, 15)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("📍 My Area", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    item {
                        Button(
                            onClick = { jumpTo(16.4326, 80.9928, 15) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Gudivada Town", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE2E8F0))
                        }
                    }
                    item {
                        Button(
                            onClick = { jumpTo(16.892, 80.098, 15) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Andhra Pradesh", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE2E8F0))
                        }
                    }
                    item {
                        Button(
                            onClick = { jumpTo(25.448, 82.858, 15) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Uttar Pradesh", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE2E8F0))
                        }
                    }
                    item {
                        Button(
                            onClick = { jumpTo(10.046, 78.334, 15) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Tamil Nadu", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE2E8F0))
                        }
                    }
                    item {
                        Button(
                            onClick = { jumpTo(20.5937, 78.9629, 5) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB).copy(alpha = 0.25f)),
                            border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("🇮🇳 All India", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF93C5FD))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // MAP TYPE SELECTOR (Hybrid / Satellite / Roadmap) + SEARCH
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MAP BASE LAYER:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = SlateGrey
                    )

                    // Layer Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            Triple("hybrid", "Hybrid", Color(0xFFF59E0B)),
                            Triple("satellite", "Satellite", Color(0xFF3B82F6)),
                            Triple("roadmap", "Roadmap", Color(0xFF10B981))
                        ).forEach { (id, label, accent) ->
                            val isSelected = mapType == id
                            Button(
                                onClick = { setMapLayer(id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                    contentColor = if (isSelected) Color.White else SlateGrey
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(accent, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // NOMINATIM SEARCH BAR (Searches OpenStreetMap directly)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = mapSearchQuery,
                        onValueChange = { mapSearchQuery = it },
                        placeholder = { Text("Search Indian town, district or place...", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (mapSearchQuery.isNotBlank()) {
                                val query = mapSearchQuery.trim()
                                runJs("window.searchNominatim('${query.replace("'", "\\'")}');")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(44.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("Search", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // LAYER FILTERS CHIPS (Cadastral & Healthcare)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "ACTIVE MAP LAYERS & FILTERS:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = SlateGrey
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = showClinics,
                            onClick = { showClinics = !showClinics },
                            label = { Text("Clinics (${hospitals.size})", fontSize = 10.sp) },
                            leadingIcon = {
                                Box(modifier = Modifier.size(8.dp).background(CrimsonPrimary, CircleShape))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonPrimary.copy(alpha = 0.12f),
                                selectedLabelColor = CrimsonPrimary
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = showDonors,
                            onClick = { showDonors = !showDonors },
                            label = { Text("Donors (${allUsers.count { it.role == "User" }})", fontSize = 10.sp) },
                            leadingIcon = {
                                Box(modifier = Modifier.size(8.dp).background(SuccessGreen, CircleShape))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SuccessGreen.copy(alpha = 0.12f),
                                selectedLabelColor = SuccessGreen
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = showUrgentRequests,
                            onClick = { showUrgentRequests = !showUrgentRequests },
                            label = { Text("Requests (${allRequests.size})", fontSize = 10.sp) },
                            leadingIcon = {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFFDC2626), CircleShape))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFDC2626).copy(alpha = 0.12f),
                                selectedLabelColor = Color(0xFFDC2626)
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = showCadastralParcels,
                            onClick = { showCadastralParcels = !showCadastralParcels },
                            label = { Text("GIS Parcels (${defaultCadastralParcels.size})", fontSize = 10.sp) },
                            leadingIcon = {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF0D9488), CircleShape))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0D9488).copy(alpha = 0.12f),
                                selectedLabelColor = Color(0xFF0D9488)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // THE INTERACTIVE LEAFLET WEBVIEW CONTAINER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            settings.setSupportZoom(true)
                            settings.cacheMode = WebSettings.LOAD_DEFAULT

                            val bridge = LeafletJsBridge(
                                onParcelSelected = { pId ->
                                    selectedParcel = defaultCadastralParcels.find { it.id == pId }
                                    selectedHospital = null
                                },
                                onHospitalSelected = { hId ->
                                    hospitals.find { it.id == hId }?.let { hosp ->
                                        selectedHospital = hosp
                                        selectedParcel = null
                                        onSelectHospital(hosp)
                                    }
                                },
                                onUserSelected = { uId ->
                                    allUsers.find { it.id == uId }?.let { u ->
                                        Toast.makeText(ctx, "Donor: ${u.name} (${u.getDecryptedBloodType()})", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onShowToast = { msg ->
                                    Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
                                }
                            )

                            addJavascriptInterface(bridge, "Android")

                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isMapLoading = false
                                }
                            }

                            loadDataWithBaseURL("https://lifestream.app/", htmlContent, "text/html", "UTF-8", null)
                            webViewRef = this
                        }
                    },
                    update = { view ->
                        webViewRef = view
                    }
                )

                if (isMapLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F172A).copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF3B82F6), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Loading OpenStreetMap & Leaflet Tiles...",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Map Legend Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(7.dp).background(Color(0xFF2563EB), CircleShape))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("You (Current GPS)", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(7.dp).background(CrimsonPrimary, CircleShape))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Partner Blood Bank", color = Color.White, fontSize = 9.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(7.dp).background(SuccessGreen, CircleShape))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Network Donor", color = Color.White, fontSize = 9.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(7.dp).background(Color(0xFF0D9488), RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Cadastral Parcel Polygon", color = Color.White, fontSize = 9.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CADASTRAL & HEALTHCARE INSPECTOR (Directly mirrors the React Cadastral Inspector)
        selectedParcel?.let { parcel ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CADASTRAL GIS INSPECTOR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2DD4BF),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Survey #${parcel.surveyNumber}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                        val statusBg = when (parcel.status) {
                            "Verified" -> Color(0xFF10B981)
                            "Flagged" -> Color(0xFFEF4444)
                            else -> Color(0xFFF59E0B)
                        }
                        Box(
                            modifier = Modifier
                                .background(statusBg.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                .border(1.dp, statusBg.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = parcel.status.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusBg
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF334155))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row {
                            Text("Registered Owner: ", fontSize = 11.sp, color = SlateGrey)
                            Text(parcel.ownerName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row {
                            Text("Area: ", fontSize = 11.sp, color = SlateGrey)
                            Text("${parcel.areaAcres} Acres (${parcel.landClassification})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row {
                            Text("Location: ", fontSize = 11.sp, color = SlateGrey)
                            Text("${parcel.village}, ${parcel.district} (${parcel.state})", fontSize = 11.sp, color = Color.White)
                        }
                        Row {
                            Text("Subdivision Code: ", fontSize = 11.sp, color = SlateGrey)
                            Text(parcel.subdivisionCode ?: "Main Block", fontSize = 11.sp, color = Color(0xFFFCD34D), fontFamily = FontFamily.Monospace)
                        }
                        if (parcel.soilType != null) {
                            Row {
                                Text("Soil Type: ", fontSize = 11.sp, color = SlateGrey)
                                Text(parcel.soilType, fontSize = 11.sp, color = Color.White)
                            }
                        }
                        if (parcel.irrigationSource != null) {
                            Row {
                                Text("Irrigation: ", fontSize = 11.sp, color = SlateGrey)
                                Text(parcel.irrigationSource, fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            Toast.makeText(context, "Inspecting statutory deed: ${parcel.recordId ?: "N/A"}", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Inspect", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Inspect Linked Statutory Revenue Record", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // CLINIC INSPECTOR CARD (When a hospital marker is tapped)
        selectedHospital?.let { hospital ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SlateDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PARTNER BLOOD BANK INSPECTOR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = hospital.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(CrimsonPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("VERIFIED PARTNER", color = CrimsonPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF334155))

                    Text("Verified Status: ${if (hospital.verifiedStatus) "Active Partner Clinic" else "Pending"}", fontSize = 11.sp, color = Color.White)
                    Text("Coordinates: ${hospital.latitude}, ${hospital.longitude}", fontSize = 10.sp, color = SlateGrey, fontFamily = FontFamily.Monospace)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val lat = hospital.latitude
                                val lon = hospital.longitude
                                jumpTo(lat, lon, 17)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Center on Map", fontSize = 11.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${hospital.latitude},${hospital.longitude}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Location: ${hospital.latitude}, ${hospital.longitude}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Get Directions", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Builds the complete Leaflet.js HTML bundle embedding OpenStreetMap, Esri Satellite,
 * Cadastral polygon layers, and Healthcare markers with full touch gestures.
 */
private fun buildLeafletHtml(
    initialLat: Double,
    initialLon: Double,
    currentUser: User?,
    hospitals: List<Hospital>,
    allUsers: List<User>,
    allRequests: List<BloodRequest>,
    parcels: List<CadastralParcel>
): String {
    val hospitalsJson = JSONArray().apply {
        hospitals.forEach { h ->
            put(JSONObject().apply {
                put("id", h.id)
                put("name", h.name)
                put("lat", h.latitude)
                put("lon", h.longitude)
                put("verified", h.verifiedStatus)
            })
        }
    }.toString()

    val usersJson = JSONArray().apply {
        allUsers.filter { currentUser == null || it.id != currentUser.id }.forEach { u ->
            put(JSONObject().apply {
                put("id", u.id)
                put("name", u.name)
                put("lat", u.latitude)
                put("lon", u.longitude)
                put("role", u.role)
                put("bloodGroup", u.getDecryptedBloodType())
            })
        }
    }.toString()

    val requestsJson = JSONArray().apply {
        allRequests.forEach { r ->
            put(JSONObject().apply {
                put("id", r.id)
                put("hospitalName", r.hospitalName)
                put("lat", r.hospitalLatitude)
                put("lon", r.hospitalLongitude)
                put("bloodType", r.bloodTypeNeeded)
                put("units", r.unitsRequired)
                put("urgency", r.urgencyLevel)
            })
        }
    }.toString()

    val parcelsJson = JSONArray().apply {
        parcels.forEach { p ->
            put(JSONObject().apply {
                put("id", p.id)
                put("surveyNumber", p.surveyNumber)
                put("ownerName", p.ownerName)
                put("areaAcres", p.areaAcres)
                put("village", p.village)
                put("district", p.district)
                put("state", p.state)
                put("status", p.status)
                val coordsArr = JSONArray()
                p.coordinates.forEach { (lat, lng) ->
                    coordsArr.put(JSONArray().apply {
                        put(lat)
                        put(lng)
                    })
                }
                put("coordinates", coordsArr)
            })
        }
    }.toString()

    val userLat = currentUser?.latitude ?: initialLat
    val userLon = currentUser?.longitude ?: initialLon
    val userName = currentUser?.name ?: "Current User"

    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
    <title>Leaflet GIS Map</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <style>
        html, body {
            margin: 0;
            padding: 0;
            width: 100%;
            height: 100%;
            background-color: #0f172a;
            overflow: hidden;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
        }
        #map {
            width: 100%;
            height: 100%;
        }
        .leaflet-popup-content-wrapper {
            background: #0f172a;
            color: #f8fafc;
            border-radius: 10px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.5);
            border: 1px solid #334155;
        }
        .leaflet-popup-tip {
            background: #0f172a;
        }
        .pulse-marker {
            width: 16px;
            height: 16px;
            background: #2563eb;
            border: 2px solid #ffffff;
            border-radius: 50%;
            box-shadow: 0 0 10px rgba(37,99,235,0.8);
            animation: pulse 1.8s infinite;
        }
        @keyframes pulse {
            0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(37,99,235,0.7); }
            70% { transform: scale(1.1); box-shadow: 0 0 0 12px rgba(37,99,235,0); }
            100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(37,99,235,0); }
        }
    </style>
</head>
<body>
    <div id="map"></div>
    <script>
        var hospitals = $hospitalsJson;
        var users = $usersJson;
        var requests = $requestsJson;
        var parcels = $parcelsJson;

        var map = L.map('map', {
            center: [$userLat, $userLon],
            zoom: 14,
            zoomControl: true,
            attributionControl: false
        });

        // 1. Tile Layers (Roadmap, Satellite, Labels for Hybrid)
        var roadmapLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19
        });

        var satelliteLayer = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
            maxZoom: 19
        });

        var labelsLayer = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/Reference/World_Boundaries_and_Places/MapServer/tile/{z}/{y}/{x}', {
            maxZoom: 19
        });

        // Default base layer: Hybrid (Satellite + Labels)
        satelliteLayer.addTo(map);
        labelsLayer.addTo(map);

        window.setMapLayer = function(type) {
            if (map.hasLayer(roadmapLayer)) map.removeLayer(roadmapLayer);
            if (map.hasLayer(satelliteLayer)) map.removeLayer(satelliteLayer);
            if (map.hasLayer(labelsLayer)) map.removeLayer(labelsLayer);

            if (type === 'roadmap') {
                roadmapLayer.addTo(map);
            } else if (type === 'satellite') {
                satelliteLayer.addTo(map);
            } else { // hybrid
                satelliteLayer.addTo(map);
                labelsLayer.addTo(map);
            }
        };

        window.jumpToRegion = function(lat, lng, zoom) {
            map.flyTo([lat, lng], zoom || 15, { duration: 1.2 });
        };

        // User Marker
        var userIcon = L.divIcon({
            className: 'pulse-container',
            html: '<div class="pulse-marker"></div>',
            iconSize: [16, 16],
            iconAnchor: [8, 8]
        });
        L.marker([$userLat, $userLon], { icon: userIcon })
            .bindPopup('<div style="padding:4px;"><strong>📍 $userName</strong><br/><span style="font-size:11px;color:#94a3b8;">Your Current Area</span></div>')
            .addTo(map);

        // Hospital Markers Group
        var clinicLayerGroup = L.layerGroup().addTo(map);
        hospitals.forEach(function(h) {
            var m = L.circleMarker([h.lat, h.lon], {
                radius: 8,
                color: '#be123c',
                weight: 2,
                fillColor: '#e11d48',
                fillOpacity: 0.9
            });
            m.bindPopup(
                '<div style="min-width:160px;padding:4px;">' +
                '<strong style="color:#f43f5e;font-size:13px;">' + h.name + '</strong><br/>' +
                '<span style="font-size:11px;color:#94a3b8;">Verified Blood Bank</span><br/>' +
                '<button onclick="window.Android && window.Android.onSelectHospital(\'' + h.id + '\')" style="margin-top:6px;width:100%;background:#e11d48;border:none;color:#fff;padding:4px;border-radius:4px;font-size:10px;font-weight:bold;cursor:pointer;">Select Blood Bank</button>' +
                '</div>'
            );
            m.on('click', function() {
                if (window.Android) window.Android.onSelectHospital(String(h.id));
            });
            clinicLayerGroup.addLayer(m);
        });

        // Network Donors Group
        var donorLayerGroup = L.layerGroup().addTo(map);
        users.forEach(function(u) {
            var color = u.role === 'Blood Bank' ? '#f59e0b' : '#10b981';
            var m = L.circleMarker([u.lat, u.lon], {
                radius: 6,
                color: color,
                weight: 2,
                fillColor: color,
                fillOpacity: 0.85
            });
            m.bindPopup(
                '<div style="padding:4px;">' +
                '<strong>' + u.name + '</strong><br/>' +
                '<span style="font-size:11px;color:#94a3b8;">' + u.role + ' • Group: ' + u.bloodGroup + '</span>' +
                '</div>'
            );
            m.on('click', function() {
                if (window.Android) window.Android.onSelectUser(String(u.id));
            });
            donorLayerGroup.addLayer(m);
        });

        // Urgent Requests Group
        var requestLayerGroup = L.layerGroup().addTo(map);
        requests.forEach(function(r) {
            var m = L.circleMarker([r.lat, r.lon], {
                radius: 9,
                color: '#b91c1c',
                weight: 3,
                fillColor: '#ef4444',
                fillOpacity: 0.95
            });
            m.bindPopup(
                '<div style="padding:4px;">' +
                '<strong style="color:#ef4444;">🚨 Urgent: ' + r.bloodType + ' (' + r.units + ' Units)</strong><br/>' +
                '<span style="font-size:11px;color:#cbd5e1;">' + r.hospitalName + '</span>' +
                '</div>'
            );
            requestLayerGroup.addLayer(m);
        });

        // Cadastral Parcels Layer
        var parcelLayerGroup = L.layerGroup().addTo(map);
        parcels.forEach(function(p) {
            var strokeColor = p.status === 'Verified' ? '#10b981' : (p.status === 'Flagged' ? '#ef4444' : '#f59e0b');
            var poly = L.polygon(p.coordinates, {
                color: strokeColor,
                weight: 2,
                opacity: 0.9,
                fillColor: strokeColor,
                fillOpacity: 0.3
            });
            poly.bindPopup(
                '<div style="padding:4px;min-width:160px;">' +
                '<strong style="color:#38bdf8;">Survey #' + p.surveyNumber + '</strong><br/>' +
                '<span style="font-size:11px;font-weight:bold;">' + p.ownerName + '</span><br/>' +
                '<span style="font-size:10px;color:#94a3b8;">' + p.areaAcres + ' Acres • ' + p.village + '</span><br/>' +
                '<span style="font-size:9px;font-weight:bold;color:' + strokeColor + ';">STATUS: ' + p.status.toUpperCase() + '</span>' +
                '</div>'
            );
            poly.on('click', function() {
                if (window.Android) window.Android.onSelectParcel(p.id);
            });
            parcelLayerGroup.addLayer(poly);
        });

        // Layer Visibility Controller
        window.updateLayerVisibility = function(filters) {
            if (filters.showClinics) {
                if (!map.hasLayer(clinicLayerGroup)) clinicLayerGroup.addTo(map);
            } else {
                if (map.hasLayer(clinicLayerGroup)) map.removeLayer(clinicLayerGroup);
            }

            if (filters.showDonors) {
                if (!map.hasLayer(donorLayerGroup)) donorLayerGroup.addTo(map);
            } else {
                if (map.hasLayer(donorLayerGroup)) donorLayerGroup.addTo(map);
            }

            if (filters.showUrgentRequests) {
                if (!map.hasLayer(requestLayerGroup)) requestLayerGroup.addTo(map);
            } else {
                if (map.hasLayer(requestLayerGroup)) map.removeLayer(requestLayerGroup);
            }

            if (filters.showParcels) {
                if (!map.hasLayer(parcelLayerGroup)) parcelLayerGroup.addTo(map);
            } else {
                if (map.hasLayer(parcelLayerGroup)) map.removeLayer(parcelLayerGroup);
            }
        };

        // OpenStreetMap Nominatim Search
        var searchMarker = null;
        window.searchNominatim = function(query) {
            var url = 'https://nominatim.openstreetmap.org/search?q=' + encodeURIComponent(query + ', India') + '&format=json&limit=1';
            fetch(url, { headers: { 'Accept': 'application/json' } })
                .then(function(res) { return res.json(); })
                .then(function(data) {
                    if (data && data.length > 0) {
                        var item = data[0];
                        var lat = parseFloat(item.lat);
                        var lon = parseFloat(item.lon);
                        map.flyTo([lat, lon], 14, { duration: 1.2 });
                        if (searchMarker) map.removeLayer(searchMarker);
                        searchMarker = L.circleMarker([lat, lon], {
                            radius: 10,
                            color: '#3b82f6',
                            weight: 3,
                            fillColor: '#60a5fa',
                            fillOpacity: 0.9
                        }).bindPopup('<strong>' + item.display_name + '</strong>').addTo(map).openPopup();
                        if (window.Android) window.Android.showToast('Found: ' + item.display_name);
                    } else {
                        if (window.Android) window.Android.showToast('No location found for "' + query + '"');
                    }
                })
                .catch(function(err) {
                    if (window.Android) window.Android.showToast('Search failed: ' + err);
                });
        };
    </script>
</body>
</html>
    """.trimIndent()
}
