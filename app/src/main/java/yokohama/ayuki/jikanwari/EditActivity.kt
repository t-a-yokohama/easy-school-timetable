package yokohama.ayuki.jikanwari

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import yokohama.ayuki.jikanwari.databinding.ActivityEditBinding
import java.io.*

class EditActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityEditBinding
    private lateinit var grades: Grades
    private lateinit var periods: Period
    private var grade: Int = 0
    private var term: Int = 0
    private var day: Int = 0
    private var period: Int = 0
    private var termText: String = ""
    private var periodText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ビューバインディングを設定
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // インテントのデータ受け取り
        grade = intent.getIntExtra("GRADE", 0)
        term = intent.getIntExtra("TERM", 0)
        day = intent.getIntExtra("DAY", 0)
        period = intent.getIntExtra("PERIOD", 0)

        // アクションバーの文字列変更
        termText = when (grade) {
            0 -> "1年"
            1 -> "2年"
            2 -> "3年"
            3 -> "4年"
            4 -> "5年"
            5 -> "6年"
            else -> "1年"
        }
        termText += when (term) {
            0 -> "前期"
            1 -> "後期"
            2 -> "春学期"
            3 -> "夏学期"
            4 -> "秋学期"
            5 -> "冬学期"
            6 -> "1学期"
            7 -> "2学期"
            8 -> "3学期"
            9 -> "4学期"
            else -> "前期"
        }
        periodText = when (day) {
            0 -> "月曜"
            1 -> "火曜"
            2 -> "水曜"
            3 -> "木曜"
            4 -> "金曜"
            5 -> "土曜"
            6 -> "日曜"
            else -> "月曜"
        }
        periodText += when (period) {
            0 -> "1限"
            1 -> "2限"
            2 -> "3限"
            3 -> "4限"
            4 -> "5限"
            5 -> "6限"
            6 -> "7限"
            else -> "1限"
        }
        supportActionBar?.title = "時間割変更 - $periodText"

        grades = getSavedData()
        periods = getPeriodData(grades, grade, term, day)

        binding.subject.setText(periods.subject[period])
        binding.room.setText(periods.room[period])
        binding.teacher.setText(periods.teacher[period])
        binding.email.setText(periods.email[period])
        when (periods.color[period]) {
            "white" -> binding.colors.check(R.id.white)
            "pink" -> binding.colors.check(R.id.pink)
            "orange" -> binding.colors.check(R.id.orange)
            "yellow" -> binding.colors.check(R.id.yellow)
            "green" -> binding.colors.check(R.id.green)
            "blue" -> binding.colors.check(R.id.blue)
            "purple" -> binding.colors.check(R.id.purple)
        }

        binding.submit.setOnClickListener(this)
        binding.cancel.setOnClickListener(this)
        binding.sendEmail.setOnClickListener(this)
    }

    // アプリバーのメニューをインフレート
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit_action, menu)
        return true
    }

    // 登録・キャンセル・メール送信が押されたときの処理
    override fun onClick(view: View?) {
        // 登録
        when (view) {
            binding.submit -> {
                periods.subject[period] = binding.subject.text.toString()
                periods.room[period] = binding.room.text.toString()
                periods.teacher[period] = binding.teacher.text.toString()
                periods.email[period] = binding.email.text.toString()
                periods.color[period] = when (binding.colors.checkedRadioButtonId) {
                    R.id.white -> "white"
                    R.id.pink -> "pink"
                    R.id.orange -> "orange"
                    R.id.yellow -> "yellow"
                    R.id.green -> "green"
                    R.id.blue -> "blue"
                    R.id.purple -> "purple"
                    else -> "white"
                }

                val outputString = Json.encodeToString(grades)
                val bufferedWriter = BufferedWriter(FileWriter(File(filesDir, "time_table.json")))
                val printWriter = PrintWriter(bufferedWriter)
                printWriter.println(outputString)
                printWriter.close()

                setResult(Activity.RESULT_OK)
                finish()
            }

            // メール送信
            binding.sendEmail -> {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(binding.email.text.toString()))
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }

            // キャンセル
            else -> {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    // クリア が押されたときの挙動
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        AlertDialog.Builder(this)
            .setTitle("時間割の消去")
            .setMessage("${termText}の「${periodText}」に登録されている情報を消去しますか？")
            .setPositiveButton("消去", DialogInterface.OnClickListener { _, _ ->
                periods.subject[period] = ""
                periods.room[period] = ""
                periods.teacher[period] = ""
                periods.color[period] = ""
                periods.email[period] = ""

                val outputString = Json.encodeToString(grades)
                val bufferedWriter = BufferedWriter(FileWriter(File(filesDir, "time_table.json")))
                val printWriter = PrintWriter(bufferedWriter)
                printWriter.println(outputString)
                printWriter.close()

                setResult(Activity.RESULT_OK)
                finish()
            })
            .setNeutralButton("キャンセル", DialogInterface.OnClickListener { _, _ -> })
            .show()

        return true
    }


    // JsonデータからGradesインスタンスの取得
    private fun getSavedData(): Grades {
        val inputStream = FileInputStream(File(filesDir, "time_table.json"))
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val readString: String = bufferedReader.readText()
        return Json.decodeFromString(readString)
    }

    // GradesインスタンスからPeriodインスタンスをリターン
    private fun getPeriodData(data: Grades, grade: Int, term: Int, day: Int): Period {
        val terms = when (grade) {
            0 -> data.first
            1 -> data.second
            2 -> data.third
            3 -> data.fourth
            4 -> data.fifth
            5 -> data.sixth
            else -> data.first
        }

        val days = when (term) {
            0 -> terms.prophase
            1 -> terms.late
            2 -> terms.spring
            3 -> terms.summer
            4 -> terms.autumn
            5 -> terms.winter
            6 -> terms.first
            7 -> terms.second
            8 -> terms.third
            9 -> terms.fourth
            else -> terms.prophase
        }

        return when (day) {
            0 -> days.mon
            1 -> days.tue
            2 -> days.wed
            3 -> days.thu
            4 -> days.fri
            5 -> days.sat
            6 -> days.sun
            else -> days.mon
        }
    }
}