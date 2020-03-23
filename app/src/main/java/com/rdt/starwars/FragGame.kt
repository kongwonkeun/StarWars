package com.rdt.starwars

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.frag_game.*

class FragGame : Fragment(), ServiceConnection, BTCallback {

    private val NL = "\n"
    private val CRNL = "\r\n"
    private var m_sock: BTSock? = null
    private var m_svc: BTService? = null
    private var m_connected: ConnectedType = ConnectedType.CONN_FALSE
    private var m_first_start: Boolean = true;
    private var m_addr: String = ""

    //
    // LIFECYCLE
    //
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity!!.bindService(Intent(activity!!, BTService::class.java), this, Context.BIND_AUTO_CREATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        m_addr = arguments!!.getString("device").toString()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v_data.movementMethod = ScrollingMovementMethod()
    }

    override fun onStart() {
        super.onStart()
        if (m_svc != null) {
            m_svc!!.attach(this)
        } else {
            activity!!.startService(Intent(activity!!, BTService::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if (m_first_start && m_svc != null) {
            m_first_start = false
            activity!!.runOnUiThread(this::connect)
        }
    }

    override fun onStop() {
        if (m_svc != null && !activity!!.isChangingConfigurations) {
            m_svc!!.detach()
        }
        super.onStop()
    }

    override fun onDestroy() {
        if (m_connected != ConnectedType.CONN_FALSE) {
            disconnect()
        }
        activity!!.stopService(Intent(activity, BTService::class.java))
        super.onDestroy()
    }

    override fun onDetach() {
        try {
            activity!!.unbindService(this)
        } catch (e: Exception) {
        }
        super.onDetach()
    }

    //
    // IMPLEMENT ServiceConnection
    //
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        m_svc = (service as BTService.MyBinder).getService()
        if (m_first_start && isResumed) {
            m_first_start = false
            activity!!.runOnUiThread(this::connect)
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        m_svc = null
    }

    //
    // IMPLEMENT BTCallback
    //
    override fun on_connect() {
        status("connected")
        m_connected = ConnectedType.CONN_TRUE
    }

    override fun on_connect_err(e: Exception) {
        status("connection failed: " + e.message)
        disconnect()
    }

    override fun on_io_err(e: Exception) {
        status("connection lost: " + e.message)
        disconnect()
    }

    override fun on_recv(data: ByteArray) {
        for (b in data) {
            state_machine(b.toInt())
        }
        //receive(data)
    }

    //
    // STATE MACHINE
    //
    var s_: Int = 0
    var v_: Int = 0
    var d_: Int = 0
    var d_last_: Int = 0
    var dir_: Int = 0
    var speed_: Int = 0
    var delay_: Int = 0

    private fun state_machine(b: Int) {
        if (b == 86) {
            s_ = 1
            v_ = 0
        } else if (b == 68) {
            s_ = 7
            d_ = 0
        } else {
            when (s_) {
                1 -> {
                    s_ = 2
                    v_ = (b - 48)
                }
                2 -> {
                    s_ = 3
                    v_ = (v_ * 10) + (b - 48)
                }
                3 -> {
                    s_ = 4
                    v_ = (v_ * 10) + (b - 48)
                }
                4 -> {
                    s_ = 5
                    v_ = (v_ * 10) + (b - 48)
                }
                5 -> {
                    s_ = 6
                    v_ = (v_ * 10) + (b - 48)
                }
                7 -> {
                    s_ = 8
                    d_ = (b - 48)

                }
                8 -> {
                    s_ = 9
                    d_ = (d_ * 10) + (b - 48)

                }
                9 -> {
                    s_ = 10
                    d_ = (d_ * 10) + (b - 48)

                }
                else -> {}
            }
        }
        if (s_ == 6) {
            delay_++
            speed_ = v_
            if (delay_%6 == 0) {
                synchronized(v_game.m_holder) {
                    v_game.m_thread.fire_3()
                }
            }
            //status("speed = $speed_")
            return
        }
        if (s_ == 10) {
            if (d_ == d_last_) {
                if (d_ < 14) {
                    dir_ = -1
                } else if (d_ > 16) {
                    dir_ = 1
                } else {
                    dir_ = 0
                }
                //
                //status("dir = $dir_")
                return
            }
            d_last_ = d_
        }
    }

    //
    // PRIVATE FUN
    //
    private fun connect() {
        try {
            val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val dev: BluetoothDevice = adapter.getRemoteDevice(m_addr)
            status("connecting...")
            m_connected = ConnectedType.CONN_PENDING
            m_sock = BTSock()
            m_svc!!.connect(this)
            m_sock!!.connect(context!!, m_svc!!, dev)
        } catch (e: Exception) {
            on_connect_err(e)
        }
    }

    private fun disconnect() {
        m_connected = ConnectedType.CONN_FALSE
        if (m_svc != null) {
            m_svc!!.disconnect()
        }
        if (m_sock != null) {
            m_sock!!.disconnect()
            m_sock = null
        }
    }

    private fun receive(data: ByteArray) {
        status(data.toString(Charsets.UTF_8))
    }

    private fun send(str: String) {
        if (m_connected != ConnectedType.CONN_TRUE) {
            Toast.makeText(activity, "not connected", Toast.LENGTH_LONG).show()
            return
        }
        try {
            status(str)
            val data: ByteArray = (str + CRNL).toByteArray(Charsets.UTF_8)
            m_sock!!.send(data)
        } catch (e: Exception) {
            on_io_err(e)
        }
    }

    private fun status(str: String) {
        v_data.append(str + '\n')
        val scroll: Int = v_data.layout.getLineTop(v_data.lineCount) - v_data.height
        if (scroll > 10000) {
            v_data.text = NL
        } else if (scroll > 0) {
            v_data.scrollTo(0, scroll)
        } else {
            v_data.scrollTo(0, 0)
        }
    }

    //
    // INNER CLASS
    //
    enum class ConnectedType(val i: Int) {
        CONN_FALSE(1),
        CONN_PENDING(2),
        CONN_TRUE(3),
        CONN_UNKNOWN(4)
    }

}

/* EOF */