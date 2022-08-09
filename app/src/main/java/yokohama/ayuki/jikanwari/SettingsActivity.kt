package yokohama.ayuki.jikanwari

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import yokohama.ayuki.jikanwari.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // バインディングを設定
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // アクションバーの文字列変更
        supportActionBar?.title = "設定"
        // アクションバーに戻るボタンを追加
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}