package com.rdt.starwars

class GEnemyPath(str: String) {

    var x = 0
    var y = 0
    var dir: IntArray
    var len: IntArray

    init {
        val tmp_1st: List<String> = str.split(":") // tmp_1st[1] = "x,y"
        var n = tmp_1st[1].indexOf(',')
        x = tmp_1st[1].substring(0, n).trim().toInt()
        y = tmp_1st[1].substring(n+1).trim().toInt()

        val tmp_2nd: List<String> = tmp_1st[2].split(",") // tmp_2st[i] = "dir-length"
        n = tmp_2nd.size

        dir = IntArray(n) { 0 }
        len = IntArray(n) { 0 }

        var x: Int
        for (i in 0 until n) {
            x = tmp_2nd[i].indexOf('-')
            dir[i] = tmp_2nd[i].substring(0, x).trim().toInt()
            len[i] = tmp_2nd[i].substring(x+1).trim().toInt()
        }
    }

}

/* EOF */