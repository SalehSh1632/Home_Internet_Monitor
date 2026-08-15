package com.homemonitor.app.data

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** یک پیام دریافتی از بله (برای تشخیص دستور گزارش لحظه‌ای) */
data class BaleUpdate(val updateId: Long, val chatId: String, val text: String)

/**
 * کلاینت بازوی (بات) بله. چون تلگرام در ایران فیلتر است، گزارش‌ها از طریق پیام‌رسان
 * بله فرستاده می‌شوند. API بله دقیقاً هم‌ساختار با API بات تلگرام است، فقط آدرس پایه
 * فرق دارد: https://tapi.bale.ai/bot<token>/METHOD_NAME
 * برای ساخت بازو و گرفتن توکن، در اپ بله با @botfather گفتگو کن.
 */
object BaleApi {

    private const val BASE_URL = "https://tapi.bale.ai"
    private val client = OkHttpClient()

    /** ارسال یک پیام متنی به بازوی بله. خروجی true یعنی موفق بود. */
    fun sendMessage(botToken: String, chatId: String, text: String): Boolean {
        if (botToken.isBlank() || chatId.isBlank()) return false
        return try {
            val url = "$BASE_URL/bot$botToken/sendMessage"
            val body = FormBody.Builder()
                .add("chat_id", chatId)
                .add("text", text)
                .build()
            val request = Request.Builder().url(url).post(body).build()
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * دریافت پیام‌های جدید بازو (برای تشخیص دستور گزارش لحظه‌ای در گروه).
     * offset را همیشه ۰ می‌فرستیم و پیام‌ها را تأیید (confirm) نمی‌کنیم، چون چند گوشی
     * هم‌زمان از همین بازو استفاده می‌کنند و نباید پیام را برای بقیه «مصرف» کنند؛
     * تشخیص پیام تکراری به‌صورت محلی و با شناسه‌ی آخرین پیام دیده‌شده انجام می‌شود.
     */
    fun getUpdates(botToken: String, timeoutSeconds: Int = 20): List<BaleUpdate> {
        if (botToken.isBlank()) return emptyList()
        return try {
            val url = "$BASE_URL/bot$botToken/getUpdates?timeout=$timeoutSeconds&offset=0"
            val shortClient = client.newBuilder()
                .readTimeout((timeoutSeconds + 10).toLong(), TimeUnit.SECONDS)
                .build()
            val request = Request.Builder().url(url).build()
            shortClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return emptyList()
                val body = resp.body?.string() ?: return emptyList()
                val json = JSONObject(body)
                if (!json.optBoolean("ok", false)) return emptyList()
                val results = json.getJSONArray("result")
                val list = mutableListOf<BaleUpdate>()
                for (i in 0 until results.length()) {
                    val upd = results.getJSONObject(i)
                    val updateId = upd.optLong("update_id", -1L)
                    val msg = upd.optJSONObject("message") ?: continue
                    val text = msg.optString("text", "")
                    val chat = msg.optJSONObject("chat") ?: continue
                    val chatId = chat.optLong("id").toString()
                    if (updateId >= 0) list.add(BaleUpdate(updateId, chatId, text))
                }
                list
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
