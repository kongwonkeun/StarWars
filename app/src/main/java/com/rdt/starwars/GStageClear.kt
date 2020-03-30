package com.rdt.starwars

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.Log

class GStageClear(val GAME: GView) {

    private val img_msg = BitmapFactory.decodeResource(GAME.m_ctx.resources, R.drawable.msg_clear)
    private val w = img_msg.width
    private var x = (GAME.m_width - w)/2
    private val y = 300
    private var loop = 0

    init {
    }

    //
    //
    //
    fun on_stage_clear(canvas: Canvas) {
        canvas.drawBitmap(GAME.img_bg, 0f,0f, null)
        GAME.m_thread.draw_score(canvas)
        GAME.gunship.dir = 3
        val is_finish = GAME.gunship.move()
        val w_ = GAME.gunship.w
        val h_ = GAME.gunship.h
        val x_ = GAME.gunship.x
        val y_ = GAME.gunship.y
        canvas.drawBitmap(GAME.gunship.img, (x_ - w_).toFloat(), (y_ - h_).toFloat(), null)

        x = (GAME.m_width - w)/2

        loop++
        if (loop%12/6 == 0) {
            canvas.drawBitmap(img_msg, x.toFloat(), y.toFloat(), null)
        }
        if (is_finish) {
            canvas.drawBitmap(img_msg, x.toFloat(), y.toFloat(), null)
            GAME.gunship.dir = 0
            loop = 0
            goto_next_stage()
        }
    }

    //
    //
    //
    private fun goto_next_stage() {
        GAME.enemy_missile.clear()
        GAME.gunship_missile.clear()
        GAME.boss_missile.clear()
        GAME.exp.clear()
        GAME.bonus.clear()

        GAME.stage_num++
        when (GAME.stage_num) {
            3 -> GAME.killed_boss = 0
            6 -> GAME.killed_boss = 1
            else -> {}
        }

        if (GAME.stage_num > GConfig.STAGE_COUNT) {
            GAME.status = GGameStatus.ALL_CLEAR
            GAME.stage_num = GConfig.STAGE_COUNT
        } else {
            GAME.setup_stage()
            GAME.status = GGameStatus.START
        }
    }

}

/* EOF */