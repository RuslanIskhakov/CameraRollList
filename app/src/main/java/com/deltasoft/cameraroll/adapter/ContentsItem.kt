package com.deltasoft.cameraroll.adapter

import com.deltasoft.cameraroll.enums.ContentsType;

data class ContentsItem (val type: ContentsType, val filePath: String){
    companion object {
        public fun getPlusItem():ContentsItem {
            return ContentsItem(ContentsType.PLUS_ITEM, "")
        }
    }
}