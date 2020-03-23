package com.rdt.starwars

class GConfig {
    companion object {
        val ENEMY_ROW = 6
        val ENEMY_COLUMN = 8
        val ENEMY_POSITION_TOP = 100
        val ENEMY_POSITION_LEFT = 72
        val ENEMY_WIDTH = 48
        val ENEMY_HEIGHT = 48
        val ENEMY_MISSILE_SPEED = 10
        val ENEMY_SPEED = 8
        val LAST_ENEMY_ROW = 0
        val LAST_ENEMY_COLUMN = 5
        val SYNC_ENEMY_ROW = 5
        val SYNC_ENEMY_COLUMN = 0

        val GUNSHIP_Y = 72
        val GUNSHIP_UNDEAD_TIME = 500
        val GUNSHIP_COUNT = 30

        val BOSS_EASY = 5
        val BOSS_MEDIUM = 10
        val BOSS_HARD = 15
        val BOSS_MISSILE_DELAY = 100
        val BOSS_COUNT = 3

        val STAGE_COUNT = 6

        val DIFF_EASY = 9
        val DIFF_MEDIUM = 7
        val DIFF_HARD = 5

        var difficult = GDifficult.EASY.i
        var music = GCtrl.ON.b
        var sound = GCtrl.ON.b
        var vibration = GCtrl.OFF.b
    }
}

enum class GShip(val i: Int) {
    STOP(0),
    LEFT(1),
    RIGHT(2),
    FIRE(3)
}

enum class GBossPart(val i: Int) {
    CENTER(0),
    LEFT(1),
    RIGHT(2),
    ALL(3)
}

enum class GDifficult(val i: Int) {
    EASY(0),
    MEDIUM(1),
    HARD(2)
}

enum class GCtrl(val b: Boolean) {
    OFF(false),
    ON(true)
}

enum class GGameStatus(val i: Int) {
    START(1),
    STAGE_CLEAR(2),
    ALL_CLEAR(3),
    END(4)
}

enum class GInput(val i: Int) {
    NONE(0),
    WAITING(1),
    TOUCH_YES(2),
    TOUCH_NO(3)
}

/* EOF */