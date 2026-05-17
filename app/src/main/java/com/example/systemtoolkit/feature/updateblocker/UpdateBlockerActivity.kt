package com.example.systemtoolkit.feature.updateblocker

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.systemtoolkit.R
import com.google.android.material.button.MaterialButton

class UpdateBlockerActivity : AppCompatActivity() {

    private lateinit var notifyStatus: TextView
    private lateinit var pkgStatusText: TextView
    private lateinit var btnCopyDisable: MaterialButton
    private lateinit var btnCopyRestore: MaterialButton
    private lateinit var btnCopyAllDisable: MaterialButton
    private lateinit var btnCopyAllRestore: MaterialButton

    private val packages = SystemUpdatePackages.allBlockedPackages()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_blocker)

        notifyStatus = findViewById(R.id.notify_block_status)
        pkgStatusText = findViewById(R.id.pkg_status_text)
        btnCopyDisable = findViewById(R.id.btn_copy_disable)
        btnCopyRestore = findViewById(R.id.btn_copy_restore)
        btnCopyAllDisable = findViewById(R.id.btn_copy_all_disable)
        btnCopyAllRestore = findViewById(R.id.btn_copy_all_restore)

        btnCopyDisable.setOnClickListener { copyAllDisable() }
        btnCopyRestore.setOnClickListener { copyAllRestore() }
        btnCopyAllDisable.setOnClickListener { copyAllDisable() }
        btnCopyAllRestore.setOnClickListener { copyAllRestore() }

        updateNotifyStatus()
        buildPackageList()
    }

    // ---------- 通知拦截状态 ----------

    private fun updateNotifyStatus() {
        val blocked = SystemUpdatePackages.allBlockedPackages()
        val pkgs = blocked.joinToString("\n")
        notifyStatus.text = getString(R.string.update_blocker_notify_active, blocked.size, pkgs)
        notifyStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
    }

    // ---------- 包列表 ----------

    private fun buildPackageList() {
        val sb = StringBuilder()
        for (pkg in packages) {
            sb.appendLine("✖  $pkg")
        }
        sb.appendLine()
        sb.append(getString(R.string.update_blocker_keyword_note))
        for (kw in SystemUpdatePackages.notificationKeywords) {
            sb.appendLine("  · $kw")
        }
        pkgStatusText.text = sb.toString().trimEnd()
    }

    // ---------- ADB 命令复制 ----------

    private fun copyAllDisable() {
        val cmds = packages.joinToString("\n") {
            "adb shell pm disable-user --user 0 $it"
        }
        copyToClipboard(cmds)
        Toast.makeText(this, R.string.update_blocker_copied_disable, Toast.LENGTH_LONG).show()
    }

    private fun copyAllRestore() {
        val cmds = packages.joinToString("\n") {
            "adb shell pm enable $it"
        }
        copyToClipboard(cmds)
        Toast.makeText(this, R.string.update_blocker_copied_restore, Toast.LENGTH_LONG).show()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("adb_commands", text))
    }
}
