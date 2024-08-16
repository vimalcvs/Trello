package com.vimalcvs.trello.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.vimalcvs.trello.activities.MyProfileActivity

object Constants {

    const val USERS: String = "Users"
    const val BOARDS: String = "Boards"

    const val ID: String = "id"
    const val NAME: String = "name"
    const val EMAIL: String = "email"
    const val IMAGE: String = "image"
    const val MOBILE: String = "mobile"
    const val FCM_TOKEN: String = "fcmToken"
    const val FCM_TOKEN_UPDATED: String = "fcmToken_updated"
    const val ASSIGNED_TO: String = "assignedTo"
    const val DOCUMENT_ID: String = "documentId"
    const val TASK_LIST: String = "taskList"
    const val BOARS_DETAIL: String = "board_detail"
    const val BOARS_MEMBER_LIST: String = "board_member_list"
    const val SELECT: String = "String"
    const val UN_SELECT: String = "UnString"

    const val TRELLO_PREFERENCES: String = "trello_preferences"

    const val TASK_LIST_ITEM_POSITION: String = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION: String = "card_list_item_position"
    const val MY_PROFILE_DATA: String = "my_profile"

    const val FCM_BASE_URL: String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION: String = "authorization"
    const val FCM_KEY: String = "key"
    const val FCM_SERVER_KEY: String =
        "AAAARnsBL_0:APA91bH2jjLSHBeqOFkT0matxGKKUYIbdc6ygr34i9N6Yvwq1ccj0PIBUOE2_nVIEcVmV6GJmXcZ92ZLc9qA4V3Ke1M4b6ARRSs04U8gpdznDQFEo_2ZqEnrVXfx6ADk9_8A_8ab8hPSbHyT"
    const val FCM_KEY_TITLE: String = "title"
    const val FCM_KEY_MESSAGE: String = "message"
    const val FCM_KEY_DATA: String = "data"
    const val FCM_KEY_TO: String = "to"

    fun showImageChooser(activity: Activity) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, MyProfileActivity.PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}