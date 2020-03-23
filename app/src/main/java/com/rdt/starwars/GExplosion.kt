package com.rdt.starwars

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

class GExplosion(val GAME: GView, var x: Int, var y: Int, private val type: Int) {

    lateinit var img: Bitmap
    var w = 0
    var h = 0
    var dead = false

    private var img_pool = arrayOfNulls<Bitmap>(6)
    private var exp_cnt = -1
    private var delay = 15
    private var n = type

    init {
        if (n == ExpType.BOSS.i) {
            n = ExpType.BIG.i
        }
        for (i in 0 until 6) {
            img_pool[i] = BitmapFactory.decodeResource(GAME.m_ctx.resources, R.drawable.exp00 + n*6 + i)
        }
        w = img_pool[0]!!.width/2
        h = img_pool[0]!!.height/2
    }

    //
    //
    //
    fun explode(): Boolean {
        exp_cnt++
        var num = exp_cnt

        if (type == ExpType.GUNSHIP.i || type == ExpType.BOSS.i) {
            num = exp_cnt / 3
            if (num > 5) {
                num = 5
            }
        }
        if (exp_cnt == 1) {
            when (type) {
                ExpType.SMALL.i -> {
                    if (GAME.is_sound) {
                        GAME.sound_pool.play(GAME.sound_exp0, 1f, 1f, 9, 0, 1f)
                    }
                }
                ExpType.BIG.i -> {
                    if (GAME.is_sound) {
                        GAME.sound_pool.play(GAME.sound_exp1, 1f, 1f, 9, 0, 1f)
                    }
                }
                ExpType.GUNSHIP.i -> {
                    if (GAME.is_sound) {
                        GAME.sound_pool.play(GAME.sound_exp2, 1f, 1f, 9, 0, 1f)
                    }
                }
                ExpType.BOSS.i -> {
                    if (GAME.is_sound) {
                        GAME.sound_pool.play(GAME.sound_exp3, 1f, 1f, 9, 0, 1f)
                    }
                }
                else -> {}
            }
        }
        img = img_pool[num]!!
        if (num < 5) {
            return false
        }

        when (type) {
            ExpType.SMALL.i -> { return true }
            ExpType.GUNSHIP.i -> { return reset_gunship() }
            else -> {
                //return check_clear()
                return true
            }
        }
    }

    fun reset_gunship(): Boolean {
        if (--delay > 0) {
            return false
        }
        if (GAME.ship >= 0) {
            GAME.gunship.reset()
            GAME.is_power = false
            GAME.is_double = false
            GAME.delay = 15
        } else {
            GAME.gunship.y = -80
            GAME.status = GGameStatus.END
        }
        return true
    }

    //
    //
    //
    enum class ExpType(val i: Int) {
        BIG(0),
        SMALL(1),
        GUNSHIP(2),
        BOSS(3)
    }

}

/* EOF */