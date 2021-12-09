package com.ruben.composeanimation.data

import com.ruben.composeanimation.download.models.DownloadInfo

/**
 * Created by Ruben Quadros on 08/12/21
 **/
object MockData {

    fun getMockAnimAssets(): List<DownloadInfo> = listOf(
        DownloadInfo(downloadId = "1", animUrl = "https://www.javatpoint.com/fullformpages/images/png.png"),
        DownloadInfo(downloadId = "2", animUrl ="https://cdn-icons-png.flaticon.com/512/281/281786.png"),
        DownloadInfo(downloadId = "3", animUrl ="https://static.wikia.nocookie.net/gensin-impact/images/d/d4/Item_Primogem.png"),
        DownloadInfo(downloadId = "4", animUrl ="https://www.freepnglogos.com/uploads/camera-logo-png/photography-camera-logo-image-24.png"),
        DownloadInfo(downloadId = "5", animUrl ="https://cdn-icons-png.flaticon.com/512/281/281752.png"),
        DownloadInfo(downloadId = "6", animUrl ="https://cdn.sharechat.com/295d8c15_1597741425818_thumbnail.jpeg"),
        DownloadInfo(downloadId = "7", animUrl ="https://cdn.sharechat.com/295d8c15_1597741425818_thumbnail.jpeg"),
        DownloadInfo(downloadId = "8", animUrl ="https://cdn.sharechat.com/295d8c15_1597741425818_thumbnail.jpeg"),
        DownloadInfo(downloadId = "9", animUrl ="https://cdn.sharechat.com/295d8c15_1597741425818_thumbnail.jpeg"),
        DownloadInfo(downloadId = "10", animUrl ="https://cdn.sharechat.com/295d8c15_1597741425818_thumbnail.jpeg")
    )
}