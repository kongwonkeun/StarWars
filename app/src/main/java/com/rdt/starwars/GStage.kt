package com.rdt.starwars

import android.util.Log
import java.io.IOException

class GStage(val GAME: GView) {

    val dx = floatArrayOf(  0f,  0.39f,  0.75f,  0.93f, 1f, 0.93f, 0.75f, 0.39f, 0f, -0.39f, -0.75f, -0.93f, -1f, -0.93f, -0.75f, -0.39f )
    val dy = floatArrayOf( -1f, -0.93f, -0.75f, -0.39f, 0f, 0.39f, 0.75f, 0.93f, 1f,  0.93f,  0.75f,  0.39f,  0f, -0.39f, -0.75f, -0.93f )
    var sync_x = 0 // sum of dx
    var dir_x = 0 // sum of dx to current direction
    var dir_x_max = 52
    var dir = 4 // dx,dy = 1,0 --> move to x+ direction
    var enemy_cnt = 0
    var delay_max = 0

    private lateinit var m_path: EnemyPath
    private lateinit var m_path_id: EnemyPathId
    private lateinit var m_delay: EnemyDelay
    private lateinit var m_position: EnemyPosition
    private lateinit var m_shield: EnemyShield

    init {
    }

    //
    //
    //
    fun read_stage_file(stage: Int) { // start from 1
        val n = stage - 1
        val f = GAME.m_ctx.resources.openRawResource(R.raw.stage01 + n)
        try {
            val data = ByteArray(f.available()) { _ -> 0 }
            f.read(data)
            f.close()
            val s = data.toString(Charsets.UTF_8)
            make_stage(s)
        } catch (e: IOException) {}
    }

    fun make_stage(str: String) {
        val x1 = str.indexOf("selection")
        val x2 = str.indexOf("delay")
        val x3 = str.indexOf("position")
        val x4 = str.indexOf("shield")

        m_path = EnemyPath(str.substring(0, x1))
        m_path_id = EnemyPathId(str.substring(x1, x2))
        m_delay = EnemyDelay(str.substring(x2, x3))
        m_position = EnemyPosition(str.substring(x3, x4))
        m_shield = EnemyShield(str.substring(x4))
        delay_max = m_delay.get_delay(GConfig.LAST_ENEMY_ROW, GConfig.LAST_ENEMY_COLUMN) // the last enemy ship (0, 5)
        enemy_cnt = m_path_id.get_cnt()
    }

    fun get_path(id: Int): GEnemyPath {
        return m_path.get_path(id)
    }

    fun get_path_id(row: Int, col: Int): Int {
        return m_path_id.get_path_id(row, col)
    }

    fun get_cnt(): Int {
        return m_path_id.get_cnt()
    }

    fun get_delay(row: Int, col: Int): Int {
        return m_delay.get_delay(row, col)
    }

    fun get_x(row: Int, col: Int): Int {
        return m_position.get_x(row, col)
    }

    fun get_y(row: Int, col: Int): Int {
        return m_position.get_y(row, col)
    }

    fun get_ship(row: Int, col: Int): Int {
        return m_position.get_ship(row, col)
    }

    fun get_shield(row: Int, col: Int): Int {
        return m_shield.get_shield(row, col)
    }

    //
    //
    //
    inner class EnemyPath(str: String) {

        private val path: MutableList<GEnemyPath> = ArrayList()

        init {
            val tmp: List<String> = str.split("\n")
            for (i in tmp.indices) {
                if (tmp[i].indexOf("//") >= 0 || tmp[i].trim() == "") {
                    continue
                }
                path.add(GEnemyPath(tmp[i]))
            }
        }

        fun get_path(id: Int): GEnemyPath {
            return path[id]
        }

    }

    //
    //
    //
    inner class EnemyPathId(str: String) {

        private val path_id = Array(GConfig.ENEMY_ROW) { IntArray(GConfig.ENEMY_COLUMN) }
        private var cnt = 0

        init {
            val tmp: List<String> = str.split("\n")
            var s: String
            var c: Char
            for (i in 1 until tmp.size - 1) { // tmp[0] is "selection:", tmp[7] is ""
                s = tmp[i]
                for (j in 0 until GConfig.ENEMY_COLUMN) {
                    c = s[j]
                    when (c) {
                        '-' -> {
                            path_id[i-1][j] = -1
                        }
                        else -> {
                            cnt++
                            if (c <= '9') {
                                path_id[i-1][j] = c.toInt() - 48
                            } else {
                                path_id[i-1][j] = c.toInt() - 87
                            }
                        }
                    }
                }
            }
        }

        fun get_path_id(row: Int, col: Int): Int {
            return path_id[row][col]
        }

        fun get_cnt(): Int {
            return cnt
        }

    }

    //
    //
    //
    inner class EnemyDelay(str: String) {

        private val delay = Array(GConfig.ENEMY_ROW) { IntArray(GConfig.ENEMY_COLUMN) }

        init {
            val tmp: List<String> = str.split("\n")
            var s: String
            for (i in 1 until tmp.size - 1) { // tmp[0] is "delay:"
                for (j in 0 until GConfig.ENEMY_COLUMN) {
                    s = tmp[i].substring(j*4, (j+1)*4).trim()
                    if (s == "---") {
                        delay[i-1][j] = -1
                    } else {
                        delay[i-1][j] = s.toInt()
                    }
                }
            }
        }

        fun get_delay(row: Int, col: Int): Int {
            return delay[row][col]
        }

    }

    //
    //
    //
    inner class EnemyPosition(str: String) {

        private val x = Array(GConfig.ENEMY_ROW) { IntArray(GConfig.ENEMY_COLUMN) }
        private val y = Array(GConfig.ENEMY_ROW) { IntArray(GConfig.ENEMY_COLUMN) }
        private val ship = Array(GConfig.ENEMY_ROW) { IntArray(GConfig.ENEMY_COLUMN) }

        init {
            val tmp: List<String> = str.split("\n")
            var s: String
            var c: Char
            for (i in 1 until tmp.size - 1) { // tmp[0] is "position:"
                s = tmp[i]
                for (j in 0 until GConfig.ENEMY_COLUMN) {
                    c = s[j]
                    if (c == '-') {
                        ship[i-1][j] = -1
                    } else if (c <= '9') {
                        ship[i-1][j] = c.toInt() - 48
                    } else {
                        ship[i-1][j] = c.toInt() - 87
                    }
                }
            }
            val w = GConfig.ENEMY_WIDTH
            val h = GConfig.ENEMY_HEIGHT
            val top = GConfig.ENEMY_POSITION_TOP
            val left = GAME.m_width/2 - w*GConfig.ENEMY_COLUMN/2

                    for (i in 0 until GConfig.ENEMY_ROW) {
                if (i <= 1) {
                    for (j in 0 until GConfig.ENEMY_COLUMN) {
                        x[i][j] = j*w + left
                        y[i][j] = i*h + top
                    }
                } else {
                    for (j in 0 until GConfig.ENEMY_COLUMN) {
                        x[i][j] = j*w + left
                        y[i][j] = i*h + top
                    }
                }
            }
        }

        fun get_x(row: Int, col: Int): Int {
            return x[row][col]
        }

        fun get_y(row: Int, col: Int): Int {
            return y[row][col]
        }

        fun get_ship(row: Int, col: Int): Int {
            return ship[row][col]
        }

    }

    //
    //
    //
    inner class EnemyShield(str: String) {

        private val shield = Array(GConfig.ENEMY_ROW) { IntArray(GConfig.ENEMY_COLUMN) }

        init {
            val tmp: List<String> = str.split("\n")
            var s: String
            var c: Char
            for (i in 1 until tmp.size - 1) { // tmp[0] is "shield:"
                s = tmp[i]
                for (j in 0 until GConfig.ENEMY_COLUMN) {
                    c = s[j]
                    when (c) {
                        '-' -> {
                            shield[i-1][j] = -1
                        }
                        else -> {
                            shield[i-1][j] = c.toInt() - 48
                        }
                    }
                }
            }
        }

        fun get_shield(row: Int, col: Int): Int {
            return shield[row][col]
        }

    }


}

/* EOF */