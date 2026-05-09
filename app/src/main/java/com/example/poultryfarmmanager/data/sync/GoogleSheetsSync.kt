package com.example.poultryfarmmanager.data.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSheetsSync @Inject constructor() {

    suspend fun getValues(token: String, sheetName: String, range: String = "A1:Z1000"): List<List<Any>> =
        withContext(Dispatchers.IO) {
            val url = URL("https://sheets.googleapis.com/v4/spreadsheets/${SheetsConfig.SHEET_ID}/values/$sheetName!$range")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Accept", "application/json")

            try {
                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = reader.readText()
                    reader.close()
                    val json = JSONObject(response)
                    val valuesArray = json.optJSONArray("values")
                    if (valuesArray != null) {
                        val result = mutableListOf<List<Any>>()
                        for (i in 0 until valuesArray.length()) {
                            val rowArray = valuesArray.getJSONArray(i)
                            val row = mutableListOf<Any>()
                            for (j in 0 until rowArray.length()) {
                                row.add(rowArray.get(j))
                            }
                            result.add(row)
                        }
                        result
                    } else emptyList()
                } else emptyList()
            } catch (e: Exception) {
                emptyList()
            } finally {
                conn.disconnect()
            }
        }

    suspend fun writeValues(token: String, sheetName: String, values: List<List<Any>>) =
        withContext(Dispatchers.IO) {
            ensureSheetExistsInternal(token, sheetName)
            clearSheetDataInternal(token, sheetName)

            val url = URL("https://sheets.googleapis.com/v4/spreadsheets/${SheetsConfig.SHEET_ID}/values/$sheetName!A1?valueInputOption=RAW")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Authorization", "Bearer $token")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            try {
                val body = JSONObject()
                val valuesArray = JSONArray()
                values.forEach { row ->
                    val rowArray = JSONArray()
                    row.forEach { cell -> rowArray.put(cell) }
                    valuesArray.put(rowArray)
                }
                body.put("values", valuesArray)

                val writer = OutputStreamWriter(conn.outputStream)
                writer.write(body.toString())
                writer.flush()
                writer.close()

                conn.responseCode
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                conn.disconnect()
            }
        }

    private fun ensureSheetExistsInternal(token: String, sheetName: String) {
        try {
            val url = URL("https://sheets.googleapis.com/v4/spreadsheets/${SheetsConfig.SHEET_ID}")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 15_000
            conn.readTimeout = 15_000
            conn.requestMethod = "GET"
            conn.setRequestProperty("Authorization", "Bearer $token")

            val reader = BufferedReader(InputStreamReader(conn.inputStream))
            val response = reader.readText()
            reader.close()
            conn.disconnect()

            val json = JSONObject(response)
            val sheets = json.getJSONArray("sheets")
            for (i in 0 until sheets.length()) {
                val sheet = sheets.getJSONObject(i)
                val props = sheet.getJSONObject("properties")
                if (props.getString("title") == sheetName) return
            }

            addSheetInternal(token, sheetName)
        } catch (_: Exception) {
            addSheetInternal(token, sheetName)
        }
    }

    private fun addSheetInternal(token: String, sheetName: String) {
        val url = URL("https://sheets.googleapis.com/v4/spreadsheets/${SheetsConfig.SHEET_ID}:batchUpdate")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000
        conn.readTimeout = 15_000
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true

        try {
            val body = JSONObject()
            val requests = JSONArray()
            val request = JSONObject()
            val addSheet = JSONObject()
            val properties = JSONObject()
            properties.put("title", sheetName)
            addSheet.put("properties", properties)
            request.put("addSheet", addSheet)
            requests.put(request)
            body.put("requests", requests)

            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(body.toString())
            writer.flush()
            writer.close()

            conn.responseCode
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn.disconnect()
        }
    }

    private fun clearSheetDataInternal(token: String, sheetName: String) {
        val url = URL("https://sheets.googleapis.com/v4/spreadsheets/${SheetsConfig.SHEET_ID}/values/$sheetName!A1:Z1000:clear")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000
        conn.readTimeout = 15_000
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", "Bearer $token")
        conn.setRequestProperty("Content-Length", "0")
        conn.doOutput = true

        try {
            conn.outputStream.write(ByteArray(0))
            conn.responseCode
        } catch (_: Exception) {
        } finally {
            conn.disconnect()
        }
    }

    suspend fun writeBirds(token: String, data: List<List<Any>>) {
        writeValues(token, SheetsConfig.SHEET_BIRDS, listOf(
            listOf("ID", "Breed", "Quantity", "Sold", "Price/Bird", "Slaughtered", "Date Acquired", "Notes")
        ) + data)
    }

    suspend fun writeEggs(token: String, data: List<List<Any>>) {
        writeValues(token, SheetsConfig.SHEET_EGGS, listOf(
            listOf("ID", "Date", "Total Eggs", "Eggs Sold", "Price/Egg", "Eggs Consumed", "Notes")
        ) + data)
    }

    suspend fun writeFeed(token: String, data: List<List<Any>>) {
        writeValues(token, SheetsConfig.SHEET_FEED, listOf(
            listOf("ID", "Name", "Quantity (kg)", "Unit Price", "Low Stock Threshold", "Last Restocked")
        ) + data)
    }

    suspend fun writeTasks(token: String, data: List<List<Any>>) {
        writeValues(token, SheetsConfig.SHEET_TASKS, listOf(
            listOf("ID", "Title", "Description", "Due Date", "Completed", "Created At")
        ) + data)
    }
}
