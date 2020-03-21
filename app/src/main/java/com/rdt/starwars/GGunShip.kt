package com.rdt.starwars

import android.graphics.Bitmap
import android.graphics.BitmapFactory

class GGunShip(val GAME: GView, var x: Int, var y: Int) {

    lateinit var img: Bitmap
    private var img_id = 0
    var w = 0
    var h = 0
    var dir = GShip.STOP.i
    var shield = 0
    var dead = false
    var undead = false
    var undead_time = 0

    private var img_pool = arrayOfNulls<Bitmap>(8)
    private var dx = arrayOf(0, -8, 8,  0)
    private var dy = arrayOf(0,  0, 0, -8)

    init {
        for (i in 0 until 8) {
            img_pool[i] = BitmapFactory.decodeResource(GAME.m_ctx.resources, R.drawable.gunship0 + i)
        }
        w = img_pool[0]!!.width/2
        h = img_pool[0]!!.height/2
        reset()
    }

    //
    //
    //
    fun reset() {
        x = GAME.m_width/2
        y = GAME.m_height - GConfig.GUNSHIP_Y
        shield = 3
        dead = false
        undead_time = 50
        undead = true
        dir = GShip.STOP.i
        img = img_pool[0]!!
    }

    fun move(): Boolean {
        img_id++
        if (img_id > 3) {
            img_id = 0
        }
        if (undead) {
            img = img_pool[img_id + 4]!!
            undead_time--
            if (undead_time < 0) {
                undead = false
            }
        } else {
            img = img_pool[img_id]!!
        }
        x += dx[dir]
        y += dy[dir]

        if (x < w) {
            x = w
            dir = GShip.STOP.i
        } else if (x > GAME.m_width - w) {
            x = GAME.m_width - w
            dir = GShip.STOP.i
        }
        return (y < -32)
    }

}

/* EOF */