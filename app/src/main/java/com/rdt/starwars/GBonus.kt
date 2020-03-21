package com.rdt.starwars

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas

class GBonus(val GAME: GView, var x: Int, var y: Int, val type: Int) {

    lateinit var img: Bitmap
    var w = 0
    var h = 0
    var dead = false

    private var img_pool = arrayOfNulls<Bitmap>(16)
    private var img_id = 0
    private var dy = 0

    init {
        img_pool[0] = BitmapFactory.decodeResource(GAME.resources, R.drawable.bonus0 + (type - 1))
        w = img_pool[0]!!.width/2
        h = img_pool[0]!!.height/2
        val canvas = Canvas()
        for (i in 1 until 16) {
            img_pool[i] = Bitmap.createBitmap(w*2, h*2, Bitmap.Config.ARGB_8888)
            canvas.setBitmap(img_pool[i]!!)
            canvas.rotate(22.5f, w.toFloat(), h.toFloat())
            canvas.drawBitmap(img_pool[0]!!, 0f, 0f, null)
        }
        dy = 2
        img_id = 0
        move()
    }

    //
    //
    //
    fun move(): Boolean {
        img = img_pool[img_id]!!
        img_id++
        if (img_id > 15) {
            img_id = 0
        }
        y += dy
        return (y > GAME.m_height + h)
    }

}

/* EOF */