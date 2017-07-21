package com.scienjus.smartqq.frame

import java.awt.*
import java.io.*
import javax.swing.*

/**
 * 二维码显示窗体

 * @author Dilant
 * @date 2017/4/30
 */

class QRCodeFrame : JFrame() {
    private val label: JLabel

    //创建窗体时进行初始化
    init {
        title = "请打开手机QQ并扫描二维码"
        defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        setBounds(0, 0, 400, 424)

        label = JLabel(null, null, SwingConstants.CENTER)
        label.isOpaque = true
        label.background = Color.BLACK

        add(label)
        setLocationRelativeTo(null)
        isVisible = true

        waitForQRCode() //默认为等待二维码状态
    }

    //等待二维码
    fun waitForQRCode() {
        label.icon = null
        label.text = "请等待程序获得二维码"
    }

    //显示二维码
    fun showQRCode(filePath: String) {
        val icon = ImageIcon(File(filePath).readBytes())
        label.text = null
        label.icon = icon //二维码大小为165*165
    }
}
