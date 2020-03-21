package com.rdt.starwars

import android.graphics.Bitmap
import android.graphics.BitmapFactory

class GBoss(val GAME: GView) {

    lateinit var img: Bitmap
    var img_pool = arrayOfNulls<Bitmap>(4)
    var shield = arrayListOf(0, 0, 0)
    var w = 0
    var h = 0
    var x = 0
    var y = 0

    private var shield_by_diff = arrayListOf(GConfig.BOSS_EASY, GConfig.BOSS_MEDIUM, GConfig.BOSS_HARD)
    private var loop = 0
    private var dir = 0
    private var dx = 0
    private var dy = 0

    init {
        for (i in 0 until 4) {
            img_pool[i] = BitmapFactory.decodeResource(GAME.m_ctx.resources, R.drawable.boss0 + i)
        }
        w = img_pool[3]!!.width/2
        h = img_pool[3]!!.height/2
    }

    //
    //
    //
    fun setup() {
        shield[GBossPart.CENTER.i] = shield_by_diff[GConfig.difficult]*2
        shield[GBossPart.LEFT.i] = shield_by_diff[GConfig.difficult]
        shield[GBossPart.RIGHT.i] = shield_by_diff[GConfig.difficult]
        x = GAME.m_width/2
        y = -60
        dx = 4
        dy = 4
        dir = 0
        loop = 0
        img = img_pool[GBossPart.ALL.i]!!
    }

    fun move() {
        x += dx * dir
        y += dy
        if (y > 100) {
            dy = 0
            if (dir == 0) {
                dir = 1
            }
        }
        if (x < 100 || x > GAME.m_width - 100) {
            dir = -dir
        }
        loop++
        if (loop % GConfig.BOSS_MISSILE_DELAY > 0) {
            return
        }
        GAME.boss_missile.add(GBossMissile(GAME, x, y, GBossPart.CENTER.i))
        if (shield[GBossPart.LEFT.i] > 0) {
            GAME.boss_missile.add(GBossMissile(GAME, x-w/2, y, GBossPart.LEFT.i))
        }
        if (shield[GBossPart.RIGHT.i] > 0) {
            GAME.boss_missile.add(GBossMissile(GAME, x+w/2, y, GBossPart.RIGHT.i))
        }
    }

}

/* EOF */