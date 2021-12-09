package com.ruben.composeanimation.data

import com.ruben.composeanimation.download.models.DownloadInfo

/**
 * Created by Ruben Quadros on 08/12/21
 **/
object MockData {

    fun getMockAnimAssets(): List<DownloadInfo> = listOf(
        DownloadInfo(downloadId = "1", animUrl = "https://www.javatpoint.com/fullformpages/images/png.png"),
        DownloadInfo(downloadId = "2", animUrl ="https://www.javatpoint.com/fullformpages/images/png.png"),
        DownloadInfo(downloadId = "3", animUrl ="https://cdn.sharechat.com/295d8c15_1597741425818_thumbnail.jpeg"),
        DownloadInfo(downloadId = "4", animUrl ="https://cdn.sharechat.com/295d8c15_1597741425818_thumbnail.jpeg"),
        DownloadInfo(downloadId = "5", animUrl ="https://cdn.sharechat.com/295d8c15_1597741425818_thumbnail.jpeg"),
        DownloadInfo(downloadId = "6", animUrl ="https://cdn.sharechat.com/295d8c15_1597741425818_thumbnail.jpeg"),
        DownloadInfo(downloadId = "7", animUrl ="https://cdn.sharechat.com/295d8c15_1597741425818_thumbnail.jpeg"),
        DownloadInfo(downloadId = "8", animUrl ="https://cdn.sharechat.com/295d8c15_1597741425818_thumbnail.jpeg"),
        DownloadInfo(downloadId = "9", animUrl ="https://cdn.sharechat.com/295d8c15_1597741425818_thumbnail.jpeg"),
        DownloadInfo(downloadId = "10", animUrl ="https://cdn.sharechat.com/295d8c15_1597741425818_thumbnail.jpeg")
    )
}