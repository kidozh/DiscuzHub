package com.kidozh.discuzhub.entities

class AppTheme {
    constructor(primaryColor: Int, primaryDarkColor: Int, accentColor: Int, nameResource: Int) {
        this.primaryColor = primaryColor
        this.primaryDarkColor = primaryDarkColor
        this.accentColor = accentColor
        this.nameResource = nameResource
    }

    var primaryColor:Int
    var primaryDarkColor:Int
    var accentColor :Int
    var nameResource: Int
}