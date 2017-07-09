package com.scienjus.smartqq.frame

import javax.swing.*
import java.awt.*

/**
 * 二维码显示窗体

 * @author Dilant
 * *
 * @date 2017/4/30
 */

class QRCodeFrame : JFrame() {
    private val label: JLabel
    private var icon: ImageIcon? = null

    //创建窗体时进行初始化
    init {
        this.title = "请打开手机QQ并扫描二维码"
        this.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        this.setBounds(0, 0, 400, 424)

        this.label = JLabel(null, null, SwingConstants.CENTER)
        this.label.isOpaque = true
        this.label.background = Color.BLACK

        this.add(label)
        this.setLocationRelativeTo(null)
        this.isVisible = true

        this.waitForQRCode() //默认为等待二维码状态
    }

    //等待二维码
    fun waitForQRCode() {
        this.label.icon = null
        this.label.text = "请等待程序获得二维码"
    }

    //显示二维码
    fun showQRCode(filePath: String) {
        this.icon = ImageIcon(filePath)
        this.label.text = null
        this.label.icon = icon //二维码大小为165*165
    }
}
