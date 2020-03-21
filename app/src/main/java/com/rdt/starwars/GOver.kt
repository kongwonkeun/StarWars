package com.rdt.starwars

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.util.Log

class GOver(val GAME: GView) {

    private val img_over  = BitmapFactory.decodeResource(GAME.m_ctx.resources, R.drawable.msg_over)
    private val img_congrats = BitmapFactory.decodeResource(GAME.m_ctx.resources, R.drawable.msg_all)
    private val img_again = BitmapFactory.decodeResource(GAME.m_ctx.resources, R.drawable.msg_again)
    private val img_yes   = BitmapFactory.decodeResource(GAME.m_ctx.resources, R.drawable.btn_yes)
    private val img_no    = BitmapFactory.decodeResource(GAME.m_ctx.resources, R.drawable.btn_no)
    private val rect_yes: Rect
    private val rect_no: Rect

    private var w1 = 0
    private val y1 = 260
    private var x1 = 0

    private val w2 = img_again.width
    private val y2 = 550
    private val x2 = (GAME.m_width - w2)/2

    private val wb  = img_yes.width
    private val hb  = img_yes.height
    private val yb  = 630
    private val xb1 = 100
    private val xb2 = GAME.m_width - 100 - wb

    private var state = GInput.WAITING
    private var loop = 0

    init {
        rect_yes = Rect(xb1, yb, xb1 + wb, yb + hb)
        rect_no  = Rect(xb2, yb, xb2 + wb, yb + hb)
    }

    //
    //
    //
    fun on_game_over(canvas: Canvas) {
        when (GAME.status) {
            GGameStatus.END -> { w1 = img_over.width }
            GGameStatus.ALL_CLEAR -> { w1 = img_congrats.width }
            else -> {}
        }
        x1 = (GAME.m_width - w1)/2

        when (state) {
            GInput.WAITING -> draw(canvas)
            GInput.TOUCH_YES -> restart()
            GInput.TOUCH_NO -> quit()
            else -> {}
        }
    }

    fun on_touch(x: Int, y: Int): Boolean {
        if (rect_yes.contains(x, y)) {
            state = GInput.TOUCH_YES
        }
        if (rect_no.contains(x, y)) {
            state = GInput.TOUCH_NO
        }
        return true
    }

    //
    //
    //
    private fun draw(canvas: Canvas) {
        canvas.drawBitmap(GAME.img_bg, 0f, 0f, null)
        GAME.m_thread.move_all()
        GAME.m_thread.start_attack()
        GAME.m_thread.draw_all(canvas)

        loop++
        if (loop%12/6 == 0) {
            if (GAME.status == GGameStatus.END) {
                canvas.drawBitmap(img_over, x1.toFloat(), y1.toFloat(), null)
            } else {
                canvas.drawBitmap(img_congrats, x1.toFloat(), y1.toFloat(), null)
            }
        }
        canvas.drawBitmap(img_again, x2.toFloat(), y2.toFloat(), null)
        canvas.drawBitmap(img_yes, xb1.toFloat(), yb.toFloat(), null)
        canvas.drawBitmap(img_no, xb2.toFloat(), yb.toFloat(), null)
    }

    private fun restart() {
        state = GInput.WAITING

        GAME.enemy_missile.clear()
        GAME.gunship_missile.clear()
        GAME.boss_missile.clear()
        GAME.exp.clear()
        GAME.bonus.clear()
        GAME.boss.setup()

        GAME.stage_num = 1
        GAME.score = 0
        GAME.ship = GConfig.GUNSHIP_COUNT
        GAME.setup_stage()
        GAME.gunship.reset()
        GAME.status = GGameStatus.START
    }

    private fun quit() {
        GAME.quit()
    }

}

/* EOF */