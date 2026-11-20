package com.estebangomez.asignaciones

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Busca, descarga e instala las versiones nuevas publicadas como Releases de GitHub. */
object Updater {
    @Volatile
    private var busy = false

    /** Devuelve JSON: {ok:true, ver, url, size} o {ok:false, err}. Se llama desde un hilo aparte. */
    fun check(): String {
        val repo = BuildConfig.UPDATE_REPO
        if (repo.isBlank()) return JSONObject().put("ok", false).put("err", "norepo").toString()
        return try {
            val c = URL("https://api.github.com/repos/$repo/releases/latest").openConnection() as HttpURLConnection
            c.connectTimeout = 8000
            c.readTimeout = 8000
            c.setRequestProperty("Accept", "application/vnd.github+json")
            c.setRequestProperty("User-Agent", "Asignaciones-App")
            if (c.responseCode != 200) {
                return JSONObject().put("ok", false).put("err", "http" + c.responseCode).toString()
            }
            val j = JSONObject(c.inputStream.bufferedReader().use { it.readText() })
            val assets = j.optJSONArray("assets")
            var url = ""
            var size = 0L
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val a = assets.getJSONObject(i)
                    if (a.optString("name").endsWith(".apk")) {
                        url = a.optString("browser_download_url")
                        size = a.optLong("size")
                        break
                    }
                }
            }
            JSONObject().put("ok", true).put("ver", j.optString("tag_name").removePrefix("v"))
                .put("url", url).put("size", size).toString()
        } catch (e: Exception) {
            JSONObject().put("ok", false).put("err", "net").toString()
        }
    }

    /** Solo se descargan APK del propio repositorio. */
    fun allowed(url: String): Boolean =
        BuildConfig.UPDATE_REPO.isNotBlank() &&
            url.startsWith("https://github.com/" + BuildConfig.UPDATE_REPO + "/releases/download/")

    fun canInstall(ctx: Context): Boolean = ctx.packageManager.canRequestPackageInstalls()

    fun openInstallSettings(ctx: Context) {
        val i = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + ctx.packageName))
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(i)
    }

    /** Descarga el APK informando el avance (0..100). Devuelve null si falla. */
    fun download(ctx: Context, url: String, expected: Long, onProgress: (Int) -> Unit): File? {
        if (busy) return null
        busy = true
        return try {
            val dir = File(ctx.cacheDir, "updates")
            dir.mkdirs()
            dir.listFiles()?.forEach { it.delete() }
            val out = File(dir, "Asignaciones.apk")
            val c = URL(url).openConnection() as HttpURLConnection
            c.connectTimeout = 15000
            c.readTimeout = 20000
            c.instanceFollowRedirects = true
            c.setRequestProperty("User-Agent", "Asignaciones-App")
            if (c.responseCode !in 200..299) return null
            val total = if (c.contentLengthLong > 0) c.contentLengthLong else expected
            c.inputStream.use { input ->
                out.outputStream().use { o ->
                    val buf = ByteArray(32 * 1024)
                    var done = 0L
                    var last = -1
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        o.write(buf, 0, n)
                        done += n
                        if (total > 0) {
                            val p = ((done * 100) / total).toInt().coerceIn(0, 100)
                            if (p != last) {
                                last = p
                                onProgress(p)
                            }
                        }
                    }
                }
            }
            if (expected > 0 && out.length() != expected) {
                out.delete()
                null
            } else out
        } catch (e: Exception) {
            null
        } finally {
            busy = false
        }
    }

    fun install(ctx: Context, apk: File) {
        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", apk)
        val i = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(i)
    }
}
