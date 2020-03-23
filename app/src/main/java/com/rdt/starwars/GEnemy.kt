package com.rdt.starwars

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import kotlin.math.abs
import kotlin.random.Random

class GEnemy(val GAME: GView, val row: Int, val col: Int) {

    lateinit var img: Bitmap
    lateinit var path: GEnemyPath
    var state = EnemyState.UNKNOWN
    var dead = false
    var shield = 0
    var w = 0
    var h = 0
    var x = 0
    var y = 0
    var pos_x = 0
    var pos_y = 0

    private var img_pool = arrayOfNulls<Bitmap>(16)
    private val rnd = Random
    private val diff = intArrayOf(GConfig.DIFF_EASY, GConfig.DIFF_MEDIUM, GConfig.DIFF_HARD)
    private val diff_id = GConfig.difficult
    private var dx = 0f
    private var dy = 0f
    private var cur_x = 0
    private var att_id = 0
    private var path_id = 0
    private var path_n = 0
    private var delay = 0
    private var dir = 0
    private var len = 0

    init {
        if (GAME.stage.get_path_id(row, col) == -1) {
            dead = true
        } else {
            val n = GAME.stage.get_ship(row, col)
            img_pool[0] = BitmapFactory.decodeResource(GAME.m_ctx.resources, R.drawable.enemy00 + n)
            w = img_pool[0]!!.width/2
            h = img_pool[0]!!.height/2
            val canvas = Canvas()
            for (i in 1 until 16) {
                img_pool[i] = Bitmap.createBitmap(w*2, h*2, Bitmap.Config.ARGB_8888)
                canvas.setBitmap(img_pool[i])
                canvas.rotate(22.5f, w.toFloat(), h.toFloat())
                canvas.drawBitmap(img_pool[0]!!, 0f, 0f, null)
            }
            reset()
        }
    }

    //
    //
    //
    fun reset() {
        if (GAME.stage.get_path_id(row, col) == -1) {
            dead = true
        } else {
            path_id = GAME.stage.get_path_id(row, col)
            delay = GAME.stage.get_delay(row, col)
            shield = GAME.stage.get_shield(row, col)
            pos_x = GAME.stage.get_x(row, col)
            pos_y = GAME.stage.get_y(row, col)
            prepare_path(path_id)
            state = EnemyState.ENTER
            dead = false
        }
    }

    fun prepare_path(n: Int) {
        path = GAME.stage.get_path(n)
        if (path.x != -99) {
            x = path.x
        }
        if (path.y != -99) {
            y = path.y
        }
        path_n = 0
        prepare_movement_data(path_n)
    }

    fun prepare_movement_data(n: Int) {
        dir = path.dir[n]
        len = path.len[n]
        dx = GAME.stage.dx[dir]
        dy = GAME.stage.dy[dir]
        img = img_pool[dir]!!
    }

    fun move() {
        if (dead && (row != GConfig.SYNC_ENEMY_ROW || col != GConfig.SYNC_ENEMY_COLUMN)) {
            return
        }
        when (state) {
            EnemyState.ENTER -> { enter() }
            EnemyState.POSITIONING -> { positioning() }
            EnemyState.MOVE_TO_POSITION -> { move_to_position() }
            EnemyState.STANDBY -> { standby() }
            EnemyState.ATTACK -> { attack() }
            EnemyState.COMEBACK_POSITIONING -> { comeback_positioning() }
            EnemyState.COMEBACK -> { comeback() }
            else -> {}
        }
    }

    fun enter() {
        if (--delay >= 0) {
            return
        }
        x += (dx*GConfig.ENEMY_SPEED).toInt()
        y += (dy*GConfig.ENEMY_SPEED).toInt()
        val df = rnd.nextInt(5) + 6 // fir direction is 6 to 10
        if (len % 15 == 0) {
            fire_missile(df)
        }
        len--
        if (len >= 0) {
            return
        }
        path_n++
        if (path_n < path.dir.size) {
            prepare_movement_data(path_n)
        } else {
            state = EnemyState.POSITIONING
        }
    }

    fun positioning() {
        if (x < pos_x + GAME.stage.sync_x) {
            dir = 2
        } else {
            dir = 14
        }
        if (y < pos_y) {
            if (dir == 2) {
                dir = 6
            } else {
                dir = 10
            }
        }
        dx = GAME.stage.dx[dir]
        dy = GAME.stage.dy[dir]
        img = img_pool[dir]!!
        state = EnemyState.MOVE_TO_POSITION
    }

    fun move_to_position() {
        x += (dx*GConfig.ENEMY_SPEED).toInt()
        y += (dy*GConfig.ENEMY_SPEED).toInt()
        if (x < pos_x + GAME.stage.sync_x) {
            dir = 2
        } else {
            dir = 14
        }
        if (y < pos_y) {
            if (dir == 2) {
                dir = 6
            } else {
                dir = 10
            }
        }
        if (abs(y - pos_y) <= 4) {
            y = pos_y
            if (x < pos_x + GAME.stage.sync_x) {
                dir = 4
            } else {
                dir = 12
            }
        }
        if (abs(x - (pos_x + GAME.stage.sync_x)) <= 4) {
            x = pos_x + GAME.stage.sync_x
            dir = 0
        }
        if (y == pos_y && x == pos_x + GAME.stage.sync_x) {
            img = img_pool[0]!!
            dx = 1f
            state = EnemyState.STANDBY
            return
        }
        dx = GAME.stage.dx[dir]
        dy = GAME.stage.dy[dir]
        img = img_pool[dir]!!
    }

    fun standby() {
        cur_x = GAME.stage.dx[GAME.stage.dir].toInt()
        x += cur_x
        if (row == GConfig.SYNC_ENEMY_ROW && col == GConfig.SYNC_ENEMY_COLUMN) {
            GAME.stage.sync_x += cur_x
            GAME.stage.dir_x++
            if (GAME.stage.dir_x >= GAME.stage.dir_x_max) {
                GAME.stage.dir_x = 0
                GAME.stage.dir_x_max = 104
                GAME.stage.dir = 16 - GAME.stage.dir
            }
        }
    }

    fun begin_attack(id: Int) {
        if (dead || (row == GConfig.SYNC_ENEMY_ROW && col == GConfig.SYNC_ENEMY_COLUMN)) {
            return
        }
        att_id = id
        prepare_path(att_id + 10)
        state = EnemyState.ATTACK
    }

    fun attack() {
        x += (dx*GConfig.ENEMY_SPEED).toInt()
        y += (dy*GConfig.ENEMY_SPEED).toInt()
        if (y < -164 || y > GAME.m_height + 164 || x < -164 || x > GAME.m_width + 164) {
            state = EnemyState.COMEBACK_POSITIONING
            return
        }
        len--
        if (len >= 0) {
            return
        }
        path_n++
        if (path_n < path.dir.size) {
            prepare_movement_data(path_n)
            if (dir in 6..10) {
                fire_missile(dir)
            }
        } else {
            state = EnemyState.POSITIONING
        }
    }

    fun comeback_positioning() {
        y = -32
        x = pos_x + GAME.stage.sync_x
        img = img_pool[0]!!
        state = EnemyState.COMEBACK
    }

    fun comeback() {
        cur_x = GAME.stage.dx[GAME.stage.dir].toInt()
        y += 2
        x += cur_x
        if (abs(y - pos_y) <= 4) {
            prepare_path(att_id + 10)
            state = EnemyState.ATTACK
        }
    }

    //
    //
    //
    private fun fire_missile(dir: Int) {
        if (rnd.nextInt(10) >= diff[diff_id]) {
            GAME.enemy_missile.add(GEnemyMissile(GAME, x, y, dir))
        }
    }

    //
    //
    //
    enum class EnemyState {
        UNKNOWN,
        ENTER,
        POSITIONING,
        MOVE_TO_POSITION,
        STANDBY,
        ATTACK,
        COMEBACK_POSITIONING,
        COMEBACK
    }

}

/* EOF */