package com.cos229239.team02.oto.data.resource

import com.cos229239.team02.oto.data.resource.NpsAlertClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cos229239.team02.oto.data.safety.NpsParkOption
import com.cos229239.team02.oto.data.safety.SafetyHttpException
import kotlinx.coroutines.CancellationException
import okio.IOException
import org.json.JSONException


@Composable
fun NpsParkPicker(
    client: NpsAlertClient,
    selectedParkCode: String?,
    onParkSelected: (String?) -> Unit
){
    var parks by remember {
        mutableStateOf<List<NpsParkOption>>(emptyList())
    }

    var query by rememberSaveable {
        mutableStateOf("")
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var linkError by remember {
        mutableStateOf<String?>(null)
    }

    var retryCount by remember {
        mutableStateOf(0)
    }

    val uriHandler = LocalUriHandler.current

    LaunchedEffect(client, retryCount) {
        isLoading = true
        errorMessage = null

        try {
            parks = client.getParkOptions()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            errorMessage = when(error) {
                is SafetyHttpException ->
                    "NPS request failed: HTTP ${error.statusCode}."

                is IllegalArgumentException ->
                    "NPS configuration is missing or invalid" +
                            " Check NPS_API_KEY and rebuild."

                is JSONException ->
                    "NPS responded, but park data could not be parsed."
                is IOException ->
                    "Unable to load NPS data. Check connection and retry."
                else ->
                    "Unable to load parks (${error.javaClass.simpleName})."
            }
        } finally {
            isLoading = false
        }
    }

    val search = query.trim()

    val matches = remember(parks,search) {
        if (search.length < 2) {
            emptyList()
        } else {
            parks.filter { park ->
                park.fullName.contains(search, ignoreCase = true) ||
                        park.parkCode.contains(search, ignoreCase = true)

            }
        }
    }

    val selectedPark = parks.firstOrNull {
        it.parkCode == selectedParkCode
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),

        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "NATIONAL PARK NOTICES",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Search for a park and select it." +
                "OTO fill in its code automatically."
            )

            OutlinedTextField(
                value = query,

                onValueChange = {
                    query = it
                },
                label = {
                    Text("Search park name or code")
                },

                placeholder = {
                    Text("Example: Yellow Stone")
                },

                enabled = !isLoading && errorMessage == null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            when {
                isLoading -> {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Loading NPS parks...")
                }
                errorMessage != null ->{
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(
                        onClick = {
                            retryCount++
                        }
                    ) {
                        Text("Retry")
                    }
                }
                search.length < 2 -> {
                    Text(
                        text = "Enter at least two characters.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                matches.isEmpty() -> {
                    Text("No matching parks found.")
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        items(
                            items = matches,
                            key = { it.parkCode}
                        ) { park ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onParkSelected(park.parkCode)
                                        query = ""
                                    }
                                    .padding(vertical = 12.dp)

                            ) {
                                Text(
                                    text = park.fullName,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${park.states} • " +
                                    "Code: ${park.parkCode}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            if (selectedParkCode != null) {
                Text(
                    text = "Selected: " +
                            (selectedPark?.fullName ?: selectedParkCode),
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Park code: $selectedParkCode"
                )
                TextButton(
                    onClick = {
                        onParkSelected(null)
                    }
                ) {
                    Text("Clear park selection")
                }

            }
            Text(
                text = "Choosing a park changes NPS notices only. " +
                "It does not change your map location.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Where is the code? On and official park website, " +
                "look at the address after nps.gov/. " +
                "For example, nps.gov/acad/ uses acad",
                style = MaterialTheme.typography.bodySmall
            )

            TextButton(
                onClick = {
                    linkError = null

                    try {
                        //Park Code Reference Guide for Park Codes
                        uriHandler.openUri(
                            "https://www.nps.gov/articles/000/" +
                                    "historic-listing-of-nps-park-codes.htm"
                        )
                    } catch(_: Exception) {
                        linkError = "Unable to open the NPS website."
                    }
                }
            ) {
                Text("Find a park on NPS.gov")
            }
            linkError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}