package com.rdt.starwars

import android.graphics.Bitmap
import android.graphics.BitmapFactory

class GBossMissile(val GAME: GView, var x: Int, var y: Int, var dir: Int) {

    val img: Bitmap = BitmapFactory.decodeResource(GAME.m_ctx.resources, R.drawable.boss_missile)
    var w = img.width/2
    var h = img.height/2
    var dead = false

    private var dx = 0
    private var dy = 10

    init {
        if (dir == GBossPart.LEFT.i) {
            dx = -2
        }
        if (dir == GBossPart.RIGHT.i) {
            dx = 2
        }
    }

    //
    //
    //
    fun move(): Boolean {
        x += dx
        y += dy
        return (x < w || x > GAME.m_width + w || y > GAME.m_height + h)
    }

}

/* EOF */