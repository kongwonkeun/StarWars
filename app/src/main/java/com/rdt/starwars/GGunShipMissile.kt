package com.rdt.starwars

import android.graphics.Bitmap
import android.graphics.BitmapFactory

class GGunShipMissile(val GAME: GView, var x: Int, var y: Int) {

    var img: Bitmap
    var w = 0
    var h = 0
    var dead = false

    private var m = 0
    private var dy = -10

    init {
        if (GAME.is_power) {
            m = 1
        } else {
            m = 0
        }
        img = BitmapFactory.decodeResource(GAME.m_ctx.resources, R.drawable.missile1 + m)
        w = img.width/2
        h = img.height/2
    }

    //
    //
    //
    fun move(): Boolean {
        y += dy
        return (y < 10)
    }

}

/* EOF */