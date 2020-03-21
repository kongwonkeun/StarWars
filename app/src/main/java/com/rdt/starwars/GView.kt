package com.rdt.starwars

import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Vibrator
import android.util.AttributeSet
import android.util.Log
import android.view.*
import java.lang.Exception
import kotlin.math.abs

class GView(context: Context, attributeSet: AttributeSet): SurfaceView(context, attributeSet), SurfaceHolder.Callback {

    var m_holder = holder
    var m_ctx = context
    var m_thread = GameThread(this)
    var m_width = 0
    var m_height = 0
    var score = 0
    var ship = GConfig.GUNSHIP_COUNT
    var delay = 15
    var stage_num = 1
    var status = GGameStatus.START

    lateinit var stage: GStage
    lateinit var enemy_attack: GEnemyAttack
    lateinit var boss: GBoss
    lateinit var collision: GCollision
    lateinit var stage_clear: GStageClear
    lateinit var game_over: GOver
    var enemy = Array(GConfig.ENEMY_ROW) { arrayOfNulls<GEnemy>(GConfig.ENEMY_COLUMN) }

    val enemy_missile: MutableList<GEnemyMissile> = ArrayList()
    val gunship_missile: MutableList<GGunShipMissile> = ArrayList()
    val boss_missile: MutableList<GBossMissile> = ArrayList()
    val exp: MutableList<GExplosion> = ArrayList()
    val bonus: MutableList<GBonus> = ArrayList()
    lateinit var gunship: GGunShip

    var is_music = GConfig.music
    var is_sound = GConfig.sound
    var is_vibration = GConfig.vibration
    var is_power = false
    var is_double = false
    var is_auto_fire = false
    var is_boss = false

    lateinit var img_miniship: Bitmap
    lateinit var img_bg: Bitmap
    var ew = IntArray(6) { 0 }
    var eh = IntArray(6) { 0 }

    lateinit var sound_pool: SoundPool
    var sound_fire = 0
    var sound_exp0 = 0
    var sound_exp1 = 0
    var sound_exp2 = 0
    var sound_exp3 = 0

    lateinit var vibrator: Vibrator
    lateinit var player: MediaPlayer

    init {
        m_holder.addCallback(this)

        setup_game()
        setup_stage()
        isFocusable = true
    }

    fun setup_game() {
        val display = (m_ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val p = Point()
        display.getSize(p)
        m_width = p.x
        m_height = p.y
        Log.d("TAG", "---- W = $m_width ----")
        Log.d("TAG", "---- H = $m_height ----")

        stage = GStage(this)
        enemy_attack = GEnemyAttack(this)
        boss = GBoss(this)
        collision = GCollision(this)
        stage_clear = GStageClear(this)
        game_over = GOver(this)

        gunship = GGunShip(this, m_width/2, m_height - 60)

        img_miniship = BitmapFactory.decodeResource(m_ctx.resources, R.drawable.miniship)

        sound_pool = SoundPool.Builder().setMaxStreams(10).build()
        sound_fire = sound_pool.load(m_ctx, R.raw.fire, 1)
        sound_exp0 = sound_pool.load(m_ctx, R.raw.exp0, 2)
        sound_exp1 = sound_pool.load(m_ctx, R.raw.exp1, 3)
        sound_exp2 = sound_pool.load(m_ctx, R.raw.exp2, 4)
        sound_exp3 = sound_pool.load(m_ctx, R.raw.exp3, 5)

        vibrator = (m_ctx.getSystemService(Context.VIBRATOR_SERVICE)) as Vibrator
        player = MediaPlayer.create(m_ctx, R.raw.green)
        player.setVolume(0.7f, 0.7f)
        player.isLooping = true
        if (is_music) {
            player.start()
        }
    }

    fun setup_stage() {
        stage.read_stage_file(stage_num)
        img_bg = BitmapFactory.decodeResource(m_ctx.resources, R.drawable.space0 + stage_num % 5 - 1)
        img_bg = Bitmap.createScaledBitmap(img_bg, m_width, m_height, true)

        for (i in 0 until GConfig.ENEMY_ROW) {
            for (j in 0 until GConfig.ENEMY_COLUMN) {
                enemy[i][j] = GEnemy(this, i, j)
            }
            ew[i] = enemy[i][2]!!.w
            eh[i] = enemy[i][2]!!.h
        }
        gunship.y = m_height - GConfig.GUNSHIP_Y
        enemy_attack.reset()
    }

    //
    // SurfaceHolder.Callback
    //
    override fun surfaceCreated(holder: SurfaceHolder?) {
        try {
            m_thread.start()
        } catch (e: Exception) {
            restart()
            if (is_music) {
                player.start()
            }
        }
    }
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        if (holder != null) {
            m_holder = holder
        }
        m_width = width
        m_height = height
    }
    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        quit()
        player.stop()
    }

    //
    //
    //
    fun setup_boss_stage() {
        for (i in 0 until GConfig.ENEMY_ROW) {
            for (j in 0 until GConfig.ENEMY_COLUMN) {
                enemy[i][j]!!.reset()
            }
        }
        stage.enemy_cnt = 24
        boss.setup()
        is_boss = true
        gunship.y = m_height - GConfig.GUNSHIP_Y
        enemy_attack.reset()
        status = GGameStatus.START
    }

    fun restart() {
        m_thread.stop_thread()
        m_thread = GameThread(this)
        m_thread.start()
    }

    fun pause() {
        m_thread.pause_and_resume(true)
    }

    fun resume() {
        m_thread.pause_and_resume(false)
    }

    fun quit() {
        m_thread.stop_thread()
        //
        // TODO
        //
    }

    //
    //
    //
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action != MotionEvent.ACTION_DOWN) {
            performClick()
            return true
        }
        synchronized(m_holder) {
            val x = event.x.toInt()
            val y = event.y.toInt()
            if (status == GGameStatus.END || status == GGameStatus.ALL_CLEAR) {
                return game_over.on_touch(x, y)
            }
            if (!gunship.dead) {
                gunship.dir = GShip.STOP.i
                if (abs(x - gunship.x) < gunship.w*2 && abs(y - gunship.y) < gunship.h*2) {
                    m_thread.fire()
                } else if (x < gunship.x - gunship.w) {
                    gunship.dir = GShip.LEFT.i
                } else if (x > gunship.x + gunship.w) {
                    gunship.dir = GShip.RIGHT.i
                }
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (gunship.dead) {
            return false
        }
        synchronized(m_holder) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    gunship.dir = GShip.LEFT.i
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    gunship.dir = GShip.RIGHT.i
                }
                KeyEvent.KEYCODE_DPAD_UP -> {
                    m_thread.fire()
                }
                else -> {
                    gunship.dir = GShip.STOP.i
                }
            }
        }
        return false
    }

    //
    //
    //
    inner class GameThread : Thread {

        constructor() : super()
        constructor(game: GView) : this() {
            GAME = game
            paint.color = Color.WHITE
            paint.isAntiAlias = true
            paint.textSize = 20f
            paint.typeface = Typeface.DEFAULT_BOLD
        }

        private var running = true
        private var wait = false
        private var loop = 0

        private lateinit var GAME: GView
        private val lock = java.lang.Object()
        private val paint = Paint()

        //
        //
        //
        override fun run() {

            while (running) {
                val canvas = m_holder.lockCanvas()
                try {
                    synchronized(m_holder) {
                        when (status) {
                            GGameStatus.START -> {
                                if (is_auto_fire) {
                                    fire()
                                }
                                check_collision()
                                move_all()
                                start_attack()
                                draw_all(canvas)
                            }
                            GGameStatus.STAGE_CLEAR -> {
                                stage_clear.on_stage_clear(canvas)
                            }
                            GGameStatus.ALL_CLEAR -> {
                                game_over.on_game_over(canvas)
                            }
                            GGameStatus.END -> {
                                game_over.on_game_over(canvas)
                            }
                        }
                    }
                } finally {
                    m_holder.unlockCanvasAndPost(canvas)
                }

                synchronized(lock) {
                    if (wait) {
                        try {
                            lock.wait()
                        } catch (e: Exception) {}
                    }
                }
            }

        }

        //
        //
        //
        fun check_collision() {
            collision.check_collision()
        }

        fun start_attack() {
            enemy_attack.attack()
        }

        fun fire() {
            if (loop < delay || gunship.dead) {
                return
            }
            if (is_double) {
                gunship_missile.add(GGunShipMissile(GAME, gunship.x - 18, gunship.y))
                gunship_missile.add(GGunShipMissile(GAME, gunship.x + 18, gunship.y))
            } else {
                gunship_missile.add(GGunShipMissile(GAME, gunship.x, gunship.y))
            }
            if (!is_auto_fire) {
                gunship.dir = GShip.STOP.i
            }
            loop = 0

            if (is_sound) {
                sound_pool.play(sound_fire, 1f, 1f, 9, 0, 1f)
            }
        }

        fun move_all() {
            loop++
            if (is_boss) {
                boss.move()
                for (i in boss_missile.size - 1 downTo 0) {
                    if (boss_missile[i].move()) {
                        boss_missile.removeAt(i)
                    }
                }
            }
            for (i in 0 until GConfig.ENEMY_ROW) {
                for (j in 0 until GConfig.ENEMY_COLUMN) {
                    enemy[i][j]!!.move()
                }
            }
            for (i in enemy_missile.size - 1 downTo 0) {
                if (enemy_missile[i].move()) {
                    enemy_missile.removeAt(i)
                }
            }
            for (i in gunship_missile.size - 1 downTo 0) {
                if (gunship_missile[i].move()) {
                    gunship_missile.removeAt(i)
                }
            }
            for (i in bonus.size - 1 downTo 0) {
                if (bonus[i].move()) {
                    bonus.removeAt(i)
                }
            }
            for (i in exp.size - 1 downTo 0) {
                if (exp[i].explode()) {
                    exp.removeAt(i)
                }
            }
            if (!gunship.dead) {
                gunship.move()
            }
        }

        fun draw_score(canvas: Canvas) {
            val x = gunship.undead_time/2
            val y = 30
            val x1 = 134
            val x2 = x1 + gunship.shield*8 + 4

            for (i in 0 until ship) {
                canvas.drawBitmap(img_miniship, (i*20 + 10).toFloat(), (y - 15).toFloat(), null)
            }
            canvas.drawText("HP", 100f, y.toFloat(), paint)
            paint.color = 0xFF00A0F0.toInt()
            for (i in 0 until gunship.shield) {
                canvas.drawRect(
                    (i*8 + x1).toFloat(),
                    (y - 10).toFloat(),
                    (i*8 + 6).toFloat(),
                    (y - 4).toFloat(),
                    paint
                )
            }
            paint.color = Color.RED
            canvas.drawRect(x2.toFloat(), (y - 10).toFloat(), (x2 + x).toFloat(), (y - 4).toFloat(), paint)
            paint.color = Color.WHITE
            canvas.drawText("Score $score", 200f, y.toFloat(), paint)
            canvas.drawText("Stage $stage_num", 400f, y.toFloat(), paint)
        }

        fun draw_all(canvas: Canvas) {
            canvas.drawBitmap(img_bg, 0f, 0f, null)
            for (i in GConfig.ENEMY_ROW - 1 downTo 0) {
                for (j in GConfig.ENEMY_COLUMN - 1 downTo 0) {
                    if (enemy[i][j]!!.dead) {
                        continue
                    }
                    canvas.drawBitmap(
                        enemy[i][j]!!.img,
                        (enemy[i][j]!!.x - ew[i]).toFloat(),
                        (enemy[i][j]!!.y - eh[i]).toFloat(),
                        null
                    )
                }
            }
            if (is_boss) {
                for (m in boss_missile) {
                    canvas.drawBitmap(m.img, (m.x - m.w).toFloat(), (m.y - m.h).toFloat(), null)
                }
                canvas.drawBitmap(boss.img, (boss.x - boss.w).toFloat(), (boss.y - boss.h).toFloat(), null)
            }
            for (m in enemy_missile) {
                canvas.drawBitmap(m.img, (m.x - 1).toFloat(), (m.y - 1).toFloat(), null)
            }
            for (m in gunship_missile) {
                canvas.drawBitmap(m.img, (m.x - m.w).toFloat(), (m.y - m.h).toFloat(), null)
            }
            for (b in bonus) {
                canvas.drawBitmap(b.img, (b.x - b.w).toFloat(), (b.y - b.h).toFloat(), null)
            }
            if (!gunship.dead) {
                canvas.drawBitmap(
                    gunship.img,
                    (gunship.x - gunship.w).toFloat(),
                    (gunship.y - gunship.h).toFloat(),
                    null
                )
            }
            for (e in exp) {
                canvas.drawBitmap(e.img, (e.x - e.w).toFloat(), (e.y - e.h).toFloat(), null)
            }
            draw_score(canvas)
        }

        fun stop_thread() {
            running = false
            synchronized(lock) {
                lock.notify()
            }
        }

        fun pause_and_resume(wait_: Boolean) {
            wait = wait_
            synchronized(lock) {
                lock.notify()
            }
        }

    }

}

/* EOF */