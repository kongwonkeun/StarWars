package com.rdt.starwars

import kotlin.random.Random

class GEnemyAttack(val GAME: GView) {

    private var loop = 0

    private val rnd = Random
    private var r1 = 0
    private var r2 = 0

    init {
    }

    fun reset() {
        loop = 0
    }

    fun attack() {
        if (GAME.stage.enemy_cnt <= 10) {
            attack_all()
            return
        }
        loop++
        val n = loop - (GAME.stage.delay_max + 120)
        if (n < 0) {
            return
        }
        r1 = rnd.nextInt(10) + 1
        r2 = rnd.nextInt(10) + 1

        when (n % 600) {
            0 -> {
                attack_one(3, 1, r1)
                attack_one(3, 3, r1)
                attack_one(2, 1, r1)
            }
            50 -> {
                attack_one(5, 4, r1)
                attack_one(5, 2, r1)
                attack_one(4, 0, r1)
            }
            100 -> {
                attack_one(3, 0, r1)
                attack_one(3, 2, r1)
                attack_one(2, 4, r1)
            }
            150 -> {
                attack_one(0, 2, r1)
                attack_one(1, 3, r1)
                attack_one(1, 4, r1)
            }
            200 -> {
                attack_one(5, 3, r1)
                attack_one(5, 5, r1)
                attack_one(4, 6, r1)
            }
            250 -> {
                attack_one(3, 6, r1)
                attack_one(3, 4, r1)
                attack_one(2, 2, r1)
            }
            300 -> {
                attack_one(2, 7, r1)
                attack_one(2, 5, r1)
                attack_one(0, 5, r2)
                attack_one(1, 1, r2)
            }
            350 -> {
                attack_one(4, 6, r1)
                attack_one(4, 5, r1)
                attack_one(3, 5, r1)
                attack_one(3, 7, r2)
                attack_one(4, 4, r2)
            }
            400 -> {
                attack_one(5, 6, r1)
                attack_one(5, 1, r1)
                attack_one(2, 6, r2)
                attack_one(2, 3, r2)
            }
            450 -> {
                attack_one(1, 2, r1)
                attack_one(1, 6, r1)
                attack_one(2, 0, r2)
                attack_one(4, 3, r2)
            }
            500 -> {
                attack_one(4, 2, r1)
                attack_one(4, 1, r1)
                attack_one(1, 5, r2)
                attack_one(5, 7, r2)
            }
            else -> {}
        }
    }

    //
    //
    //
    private fun attack_one(row: Int, col: Int, pid: Int) {
        GAME.enemy[row][col]!!.begin_attack(pid)
    }

    private fun attack_all() {
        for (i in 0 until GConfig.ENEMY_ROW) {
            for (j in 0 until GConfig.ENEMY_COLUMN) {
                if (GAME.enemy[i][j]!!.state == GEnemy.EnemyState.STANDBY) {
                    attack_one(i, j, rnd.nextInt(10) + 1)
                }
            }
        }
    }

}

/* EOF */