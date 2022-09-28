package yokohama.ayuki.jikanwari

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceManager
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import yokohama.ayuki.jikanwari.databinding.ActivityMainBinding
import java.io.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var grades: Grades
    private lateinit var position: SpinnerPosition

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                grades = getSavedData()
                setTable(getTermData(grades, position.grade, position.term))
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ビューバインディングを設定
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firstOpen()

        // アクションバーの文字列変更
        supportActionBar?.title = "時間割表"

        // スピナー(プルダウン)の要素とレイアウトの結合
        val gradeAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, R.layout.spinner, resources.getStringArray(R.array.grade))
        val termsAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, R.layout.spinner, resources.getStringArray(R.array.terms))
        gradeAdapter.setDropDownViewResource(R.layout.dropdown)
        termsAdapter.setDropDownViewResource(R.layout.dropdown)
        val gradeSpinner: Spinner = binding.grade
        val termsSpinner: Spinner = binding.terms
        gradeSpinner.adapter = gradeAdapter
        termsSpinner.adapter = termsAdapter
        position = getSavedPosition()
        gradeSpinner.setSelection(position.grade, false)
        termsSpinner.setSelection(position.term, false)
        gradeSpinner.onItemSelectedListener = this
        termsSpinner.onItemSelectedListener = this

        setTableClickListener()

        // Jsonファイルに保存されている状態を表に復元
        grades = getSavedData()
        setTable(getTermData(grades, position.grade, position.term))
    }

    // スピナーが選択されたときの処理
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        position.grade = binding.grade.selectedItemPosition
        position.term = binding.terms.selectedItemPosition
        val outputString = Json.encodeToString(position)
        val bufferedWriter = BufferedWriter(FileWriter(File(filesDir, "position.json")))
        val printWriter = PrintWriter(bufferedWriter)
        printWriter.println(outputString)
        printWriter.close()

        setTable(getTermData(grades, position.grade, position.term))
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        // Do Nothing
    }

    override fun onClick(view: View?) {
        val day = when (view) {
            binding.mon1, binding.mon2, binding.mon3, binding.mon4, binding.mon5 -> 0
            binding.tue1, binding.tue2, binding.tue3, binding.tue4, binding.tue5 -> 1
            binding.wed1, binding.wed2, binding.wed3, binding.wed4, binding.wed5 -> 2
            binding.thu1, binding.thu2, binding.thu3, binding.thu4, binding.thu5 -> 3
            binding.fri1, binding.fri2, binding.fri3, binding.fri4, binding.fri5 -> 4
            else -> 0
        }
        val period = when (view) {
            binding.mon1, binding.tue1, binding.wed1, binding.thu1, binding.fri1 -> 0
            binding.mon2, binding.tue2, binding.wed2, binding.thu2, binding.fri2 -> 1
            binding.mon3, binding.tue3, binding.wed3, binding.thu3, binding.fri3 -> 2
            binding.mon4, binding.tue4, binding.wed4, binding.thu4, binding.fri4 -> 3
            binding.mon5, binding.tue5, binding.wed5, binding.thu5, binding.fri5 -> 4
            else -> 0
        }

        val intent = Intent(this, EditActivity::class.java)
        intent.putExtra("GRADE", position.grade)
        intent.putExtra("TERM", position.term)
        intent.putExtra("DAY", day)
        intent.putExtra("PERIOD", period)
        launcher.launch(intent)
    }


    private fun firstOpen() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        if (pref.getBoolean("IS_FIRST", true)) {
            val inputStream1 = resources.assets.open("time_table.json")
            val bufferedReader1 = BufferedReader(InputStreamReader(inputStream1))
            val readTableString: String = bufferedReader1.readText()
            val inputStream2 = resources.assets.open("position.json")
            val bufferedReader2 = BufferedReader(InputStreamReader(inputStream2))
            val readPositionString: String = bufferedReader2.readText()
            val bufferedWriter1 = BufferedWriter(FileWriter(File(filesDir, "time_table.json")))
            val printWriter1 = PrintWriter(bufferedWriter1)
            printWriter1.println(readTableString)
            printWriter1.close()
            val bufferedWriter2 = BufferedWriter(FileWriter(File(filesDir, "position.json")))
            val printWriter2 = PrintWriter(bufferedWriter2)
            printWriter2.println(readPositionString)
            printWriter2.close()

            pref.edit().putBoolean("IS_FIRST", false).apply()
        }
    }


    // JsonデータからGradesインスタンスの取得
    private fun getSavedData(): Grades {
        val inputStream = FileInputStream(File(filesDir, "time_table.json"))
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val readString: String = bufferedReader.readText()
        return Json.decodeFromString(readString)
    }

    // Gradesインスタンスから条件に応じたDayインスタンスを返す
    private fun getTermData(data: Grades, gradePosition: Int, termPosition: Int): Day {
        val terms = when (gradePosition) {
            0 -> data.first
            1 -> data.second
            2 -> data.third
            3 -> data.fourth
            4 -> data.fifth
            5 -> data.sixth
            else -> data.first
        }

        return when (termPosition) {
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
    }

    private fun getSavedPosition(): SpinnerPosition {
        val inputStream = FileInputStream(File(filesDir, "position.json"))
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val readString: String = bufferedReader.readText()
        return Json.decodeFromString(readString)
    }

    private fun setTableClickListener() {
        binding.mon1.setOnClickListener(this)
        binding.mon2.setOnClickListener(this)
        binding.mon3.setOnClickListener(this)
        binding.mon4.setOnClickListener(this)
        binding.mon5.setOnClickListener(this)

        binding.tue1.setOnClickListener(this)
        binding.tue2.setOnClickListener(this)
        binding.tue3.setOnClickListener(this)
        binding.tue4.setOnClickListener(this)
        binding.tue5.setOnClickListener(this)

        binding.wed1.setOnClickListener(this)
        binding.wed2.setOnClickListener(this)
        binding.wed3.setOnClickListener(this)
        binding.wed4.setOnClickListener(this)
        binding.wed5.setOnClickListener(this)

        binding.thu1.setOnClickListener(this)
        binding.thu2.setOnClickListener(this)
        binding.thu3.setOnClickListener(this)
        binding.thu4.setOnClickListener(this)
        binding.thu5.setOnClickListener(this)

        binding.fri1.setOnClickListener(this)
        binding.fri2.setOnClickListener(this)
        binding.fri3.setOnClickListener(this)
        binding.fri4.setOnClickListener(this)
        binding.fri5.setOnClickListener(this)
    }

    private fun setTable(day: Day) {
        binding.mon1.setBackgroundResource(getMyColor(day.mon.color[0]))
        binding.subjectMon1.text = day.mon.subject[0]
        binding.roomMon1.text = day.mon.room[0]
        binding.teacherMon1.text = day.mon.teacher[0]

        binding.mon2.setBackgroundResource(getMyColor(day.mon.color[1]))
        binding.subjectMon2.text = day.mon.subject[1]
        binding.roomMon2.text = day.mon.room[1]
        binding.teacherMon2.text = day.mon.teacher[1]

        binding.mon3.setBackgroundResource(getMyColor(day.mon.color[2]))
        binding.subjectMon3.text = day.mon.subject[2]
        binding.roomMon3.text = day.mon.room[2]
        binding.teacherMon3.text = day.mon.teacher[2]

        binding.mon4.setBackgroundResource(getMyColor(day.mon.color[3]))
        binding.subjectMon4.text = day.mon.subject[3]
        binding.roomMon4.text = day.mon.room[3]
        binding.teacherMon4.text = day.mon.teacher[3]

        binding.mon5.setBackgroundResource(getMyColor(day.mon.color[4]))
        binding.subjectMon5.text = day.mon.subject[4]
        binding.roomMon5.text = day.mon.room[4]
        binding.teacherMon5.text = day.mon.teacher[4]


        binding.tue1.setBackgroundResource(getMyColor(day.tue.color[0]))
        binding.subjectTue1.text = day.tue.subject[0]
        binding.roomTue1.text = day.tue.room[0]
        binding.teacherTue1.text = day.tue.teacher[0]

        binding.tue2.setBackgroundResource(getMyColor(day.tue.color[1]))
        binding.subjectTue2.text = day.tue.subject[1]
        binding.roomTue2.text = day.tue.room[1]
        binding.teacherTue2.text = day.tue.teacher[1]

        binding.tue3.setBackgroundResource(getMyColor(day.tue.color[2]))
        binding.subjectTue3.text = day.tue.subject[2]
        binding.roomTue3.text = day.tue.room[2]
        binding.teacherTue3.text = day.tue.teacher[2]

        binding.tue4.setBackgroundResource(getMyColor(day.tue.color[3]))
        binding.subjectTue4.text = day.tue.subject[3]
        binding.roomTue4.text = day.tue.room[3]
        binding.teacherTue4.text = day.tue.teacher[3]

        binding.tue5.setBackgroundResource(getMyColor(day.tue.color[4]))
        binding.subjectTue5.text = day.tue.subject[4]
        binding.roomTue5.text = day.tue.room[4]
        binding.teacherTue5.text = day.tue.teacher[4]


        binding.wed1.setBackgroundResource(getMyColor(day.wed.color[0]))
        binding.subjectWed1.text = day.wed.subject[0]
        binding.roomWed1.text = day.wed.room[0]
        binding.teacherWed1.text = day.wed.teacher[0]

        binding.wed2.setBackgroundResource(getMyColor(day.wed.color[1]))
        binding.subjectWed2.text = day.wed.subject[1]
        binding.roomWed2.text = day.wed.room[1]
        binding.teacherWed2.text = day.wed.teacher[1]

        binding.wed3.setBackgroundResource(getMyColor(day.wed.color[2]))
        binding.subjectWed3.text = day.wed.subject[2]
        binding.roomWed3.text = day.wed.room[2]
        binding.teacherWed3.text = day.wed.teacher[2]

        binding.wed4.setBackgroundResource(getMyColor(day.wed.color[3]))
        binding.subjectWed4.text = day.wed.subject[3]
        binding.roomWed4.text = day.wed.room[3]
        binding.teacherWed4.text = day.wed.teacher[3]

        binding.wed5.setBackgroundResource(getMyColor(day.wed.color[4]))
        binding.subjectWed5.text = day.wed.subject[4]
        binding.roomWed5.text = day.wed.room[4]
        binding.teacherWed5.text = day.wed.teacher[4]


        binding.thu1.setBackgroundResource(getMyColor(day.thu.color[0]))
        binding.subjectThu1.text = day.thu.subject[0]
        binding.roomThu1.text = day.thu.room[0]
        binding.teacherThu1.text = day.thu.teacher[0]

        binding.thu2.setBackgroundResource(getMyColor(day.thu.color[1]))
        binding.subjectThu2.text = day.thu.subject[1]
        binding.roomThu2.text = day.thu.room[1]
        binding.teacherThu2.text = day.thu.teacher[1]

        binding.thu3.setBackgroundResource(getMyColor(day.thu.color[2]))
        binding.subjectThu3.text = day.thu.subject[2]
        binding.roomThu3.text = day.thu.room[2]
        binding.teacherThu3.text = day.thu.teacher[2]

        binding.thu4.setBackgroundResource(getMyColor(day.thu.color[3]))
        binding.subjectThu4.text = day.thu.subject[3]
        binding.roomThu4.text = day.thu.room[3]
        binding.teacherThu4.text = day.thu.teacher[3]

        binding.thu5.setBackgroundResource(getMyColor(day.thu.color[4]))
        binding.subjectThu5.text = day.thu.subject[4]
        binding.roomThu5.text = day.thu.room[4]
        binding.teacherThu5.text = day.thu.teacher[4]


        binding.fri1.setBackgroundResource(getMyColor(day.fri.color[0]))
        binding.subjectFri1.text = day.fri.subject[0]
        binding.roomFri1.text = day.fri.room[0]
        binding.teacherFri1.text = day.fri.teacher[0]

        binding.fri2.setBackgroundResource(getMyColor(day.fri.color[1]))
        binding.subjectFri2.text = day.fri.subject[1]
        binding.roomFri2.text = day.fri.room[1]
        binding.teacherFri2.text = day.fri.teacher[1]

        binding.fri3.setBackgroundResource(getMyColor(day.fri.color[2]))
        binding.subjectFri3.text = day.fri.subject[2]
        binding.roomFri3.text = day.fri.room[2]
        binding.teacherFri3.text = day.fri.teacher[2]

        binding.fri4.setBackgroundResource(getMyColor(day.fri.color[3]))
        binding.subjectFri4.text = day.fri.subject[3]
        binding.roomFri4.text = day.fri.room[3]
        binding.teacherFri4.text = day.fri.teacher[3]

        binding.fri5.setBackgroundResource(getMyColor(day.fri.color[4]))
        binding.subjectFri5.text = day.fri.subject[4]
        binding.roomFri5.text = day.fri.room[4]
        binding.teacherFri5.text = day.fri.teacher[4]
    }

    private fun getMyColor(colorText: String): Int {
        return when (colorText) {
            "pink" -> R.color.user_pink
            "orange" -> R.color.user_orange
            "yellow" -> R.color.user_yellow
            "green" -> R.color.user_green
            "blue" -> R.color.user_blue
            "purple" -> R.color.user_purple
            "white" -> R.color.white
            else -> R.color.white
        }
    }
}
