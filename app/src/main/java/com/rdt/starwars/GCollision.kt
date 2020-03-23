package com.rdt.starwars

import kotlin.math.abs
import kotlin.random.Random

class GCollision(val GAME: GView) {

    val rnd = Random

    init {
    }

    //
    //
    //
    fun check_collision() {
        check_gunship_missile_to_enemy()
        check_enemy_missile_to_gunship()
        check_enemy_to_gunship()
        check_bonus_to_gunship()

        if (GAME.is_boss) {
            check_boss_missile_to_gunship()
            check_gunship_missile_to_boss()
        }
    }

    //
    //
    //
    private fun check_gunship_missile_to_enemy() {

        val r = rnd.nextInt(100) - 93

        out@ for (p in GAME.gunship_missile.size - 1 downTo 0) {
            val gx = GAME.gunship_missile[p].x
            val gy = GAME.gunship_missile[p].y

            for (i in 0 until GConfig.ENEMY_ROW) {
                for (j in 0 until GConfig.ENEMY_COLUMN) {
                    if (GAME.enemy[i][j]!!.dead) {
                        continue
                    }
                    val w = GAME.enemy[i][j]!!.w
                    val h = GAME.enemy[i][j]!!.h
                    val x = GAME.enemy[i][j]!!.x
                    val y = GAME.enemy[i][j]!!.y

                    if (abs(x - gx) > w || abs(y - gy) > h) {
                        continue
                    }
                    if (GAME.is_power) {
                        GAME.enemy[i][j]!!.shield -= 4
                    } else {
                        GAME.enemy[i][j]!!.shield--
                    }
                    if (GAME.enemy[i][j]!!.shield > 0) {
                        GAME.exp.add(GExplosion(GAME, gx, gy, GExplosion.ExpType.SMALL.i))
                        GAME.score += (6-i)*100
                    } else {
                        GAME.enemy[i][j]!!.dead = true
                        GAME.stage.enemy_cnt--
                        GAME.exp.add(GExplosion(GAME, x, y, GExplosion.ExpType.BIG.i))
                        GAME.score += (6-i)*200
                        if (r > 0) {
                            GAME.bonus.add(GBonus(GAME, x, y, r))
                        }
                    }
                    GAME.gunship_missile.removeAt(p)
                    continue@out
                }
            }
        }
    }

    private fun check_enemy_missile_to_gunship() {

        if (GAME.gunship.undead || GAME.gunship.dead) {
            return
        }
        val w = GAME.gunship.w
        val h = GAME.gunship.h
        val x = GAME.gunship.x
        val y = GAME.gunship.y

        for (i in GAME.enemy_missile.size - 1 downTo 0) {
            val mx = GAME.enemy_missile[i].x
            val my = GAME.enemy_missile[i].y

            if (abs(x - mx) > w || abs(y - my) > h) {
                continue
            }
            GAME.enemy_missile.removeAt(i)
            GAME.gunship.shield--

            if (GAME.gunship.shield >= 0) {
                GAME.exp.add(GExplosion(GAME, mx, my, GExplosion.ExpType.SMALL.i))
            } else {
                GAME.gunship.dead = true
                GAME.ship--
                GAME.exp.add(GExplosion(GAME, x, y, GExplosion.ExpType.GUNSHIP.i))
            }
            break
        }
    }

    private fun check_enemy_to_gunship() {
        if (GAME.gunship.dead) {
            return
        }
        val w = GAME.gunship.w
        val h = GAME.gunship.h
        val x = GAME.gunship.x
        val y = GAME.gunship.y

        for (i in 0 until GConfig.ENEMY_ROW) {
            for (j in 0 until GConfig.ENEMY_COLUMN) {
                if (GAME.enemy[i][j]!!.dead) {
                    continue
                }
                val ex = GAME.enemy[i][j]!!.x
                val ey = GAME.enemy[i][j]!!.y

                if (abs(x - ex) > w || abs(y - ey) > h) {
                    continue
                }
                GAME.enemy[i][j]!!.dead = true
                GAME.stage.enemy_cnt--
                GAME.score += (6-i)*200
                if (GAME.gunship.undead) {
                    GAME.exp.add(GExplosion(GAME, ex, ey, GExplosion.ExpType.BIG.i))
                } else {
                    GAME.gunship.dead = true
                    GAME.ship--
                    GAME.exp.add(GExplosion(GAME, x, y, GExplosion.ExpType.GUNSHIP.i))
                }
                return
            }
        }
    }

    private fun check_bonus_to_gunship() {

        val w = GAME.gunship.w
        val h = GAME.gunship.h
        val x = GAME.gunship.x
        val y = GAME.gunship.y

        for (i in GAME.bonus.size - 1 downTo 0) {
            val bx = GAME.bonus[i].x
            val by = GAME.bonus[i].y

            if (abs(x - bx) > w*2 || abs(y - by) > h*2) {
                continue
            }
            val bonus = GAME.bonus[i].type
            GAME.bonus.removeAt(i)

            when (bonus) {
                1 -> {
                    GAME.is_double = true
                }
                2 -> {
                    GAME.is_power = true
                }
                3 -> {
                    if (GAME.delay > 6) {
                        GAME.delay -= 2
                    }
                }
                4 -> { GAME.gunship.shield = 6 }
                5 -> {
                    GAME.gunship.undead_time = GConfig.GUNSHIP_UNDEAD_TIME*2
                    GAME.gunship.undead = true
                }
                6 -> {
                    if (GAME.ship < GConfig.GUNSHIP_COUNT) {
                        GAME.ship++
                    }
                }
                else -> {}
            }
        }
    }

    private fun check_boss_missile_to_gunship() {

        if (GAME.gunship.undead) {
            return
        }
        val w = GAME.gunship.w
        val h = GAME.gunship.h
        val x = GAME.gunship.x
        val y = GAME.gunship.y

        for (i in GAME.boss_missile.size - 1 downTo 0) {
            val mx = GAME.boss_missile[i].x
            val my = GAME.boss_missile[i].y

            if (abs(x - mx) <= w && abs(y - my) <= h) {
                GAME.boss_missile.removeAt(i)
                GAME.gunship.dead = true
                GAME.exp.add(GExplosion(GAME, x, y, GExplosion.ExpType.GUNSHIP.i))
                GAME.ship--
            }
        }

    }

    private fun check_gunship_missile_to_boss() {

        val w = GAME.boss.w/2
        val h = GAME.boss.h
        val c = GAME.boss.x
        val l = GAME.boss.x - w
        val r = GAME.boss.x + w
        val y = GAME.boss.y

        var damage = 1
        if (GAME.is_power) {
            damage = 4
        }

        for (i in GAME.gunship_missile.size - 1 downTo 0) {
            val mx = GAME.gunship_missile[i].x
            val my = GAME.gunship_missile[i].y

            if (abs(mx - c) < w && abs(my - y) < h) {
                GAME.boss.shield[GBossPart.CENTER.i] -= damage
                GAME.gunship_missile.removeAt(i)
                if (GAME.boss.shield[GBossPart.CENTER.i] >= 0) {
                    GAME.exp.add(GExplosion(GAME, mx, my, GExplosion.ExpType.SMALL.i))
                    GAME.score += 50
                    continue
                }
                clear_all_enemy()
                return
            }
            if (abs(mx - l) < w && abs(my - y) < h && GAME.boss.shield[GBossPart.LEFT.i] > 0) {
                GAME.boss.shield[GBossPart.LEFT.i] -= damage
                GAME.gunship_missile.removeAt(i)
                if (GAME.boss.shield[GBossPart.LEFT.i] >= 0) {
                    GAME.exp.add(GExplosion(GAME, mx, my, GExplosion.ExpType.SMALL.i))
                    GAME.score += 50
                    continue
                }
                GAME.exp.add(GExplosion(GAME, l, y, GExplosion.ExpType.BIG.i))
                GAME.score += 1000
                GAME.gunship_missile.removeAt(i)
                if (GAME.boss.shield[GBossPart.RIGHT.i] > 0) {
                    GAME.boss.img = GAME.boss.img_pool[GBossPart.RIGHT.i]!!
                } else {
                    GAME.boss.img = GAME.boss.img_pool[GBossPart.CENTER.i]!!
                }
                continue
            }
            if (abs(mx - r) < w && abs(my - y) < h && GAME.boss.shield[GBossPart.RIGHT.i] > 0) {
                GAME.boss.shield[GBossPart.RIGHT.i] -= damage
                GAME.gunship_missile.removeAt(i)
                if (GAME.boss.shield[GBossPart.RIGHT.i] >= 0) {
                    GAME.exp.add(GExplosion(GAME, mx, my, GExplosion.ExpType.SMALL.i))
                    GAME.score += 50
                    continue
                }
                GAME.exp.add(GExplosion(GAME, r, y, GExplosion.ExpType.BIG.i))
                GAME.score += 1000
                GAME.gunship_missile.removeAt(i)
                if (GAME.boss.shield[GBossPart.LEFT.i] > 0) {
                    GAME.boss.img = GAME.boss.img_pool[GBossPart.LEFT.i]!!
                } else {
                    GAME.boss.img = GAME.boss.img_pool[GBossPart.CENTER.i]!!
                }
            }
        }
    }

    private fun clear_all_enemy() {

        val w = GAME.boss.w/2
        var c = GAME.boss.x
        val l = c - w
        val r = c + w
        var y = GAME.boss.y

        GAME.exp.add(GExplosion(GAME, c, y, GExplosion.ExpType.BOSS.i))
        GAME.score += 5000

        if (GAME.boss.shield[GBossPart.LEFT.i] > 0) {
            GAME.boss.shield[GBossPart.LEFT.i] = 0
            GAME.exp.add(GExplosion(GAME, l, y, GExplosion.ExpType.BOSS.i))
        }
        if (GAME.boss.shield[GBossPart.RIGHT.i] > 0) {
            GAME.boss.shield[GBossPart.RIGHT.i] = 0
            GAME.exp.add(GExplosion(GAME, r, y, GExplosion.ExpType.BOSS.i))
        }
        for (m in GAME.boss_missile) {
            GAME.exp.add(GExplosion(GAME, m.x, m.y, GExplosion.ExpType.BIG.i))
        }
        GAME.boss_missile.clear()

        for (i in 0 until GConfig.ENEMY_ROW) {
            for (j in 0 until GConfig.ENEMY_COLUMN) {
                if (GAME.enemy[i][j]!!.shield > 0) {
                    GAME.enemy[i][j]!!.shield = 0
                    c = GAME.enemy[i][j]!!.x
                    y = GAME.enemy[i][j]!!.y
                    GAME.exp.add(GExplosion(GAME, c, y, GExplosion.ExpType.BIG.i))
                    GAME.stage.enemy_cnt--
                }
            }
        }
    }

}

/* EOF */