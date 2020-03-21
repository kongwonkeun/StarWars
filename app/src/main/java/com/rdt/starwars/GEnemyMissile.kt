package com.rdt.starwars

import android.graphics.Bitmap
import android.graphics.BitmapFactory

class GEnemyMissile(val GAME: GView, var x: Int, var y: Int, var dir: Int) {

    val img: Bitmap = BitmapFactory.decodeResource(GAME.m_ctx.resources, R.drawable.missile0)
    var dead = false

    private var dx = GAME.stage.dx[dir]
    private var dy = GAME.stage.dy[dir]

    init {
        move()
    }

    //
    //
    //
    fun move(): Boolean {
        x += (dx * GConfig.ENEMY_MISSILE_SPEED).toInt()
        y += (dy * GConfig.ENEMY_MISSILE_SPEED).toInt()
        return (x < 0 || x > GAME.m_width || y > GAME.m_height)
    }

}

/* EOF */