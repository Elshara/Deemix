package app.deemix.downloader.types

class DeezerDate {
    var day: String = "00"
    var month: String = "00"
    var year: String = "0000"

    constructor(date: String){
        year = date.substring(0, 4)
        month = date.substring(5, 7)
        day = date.substring(8, 10)
        fixDayMonth()
    }

    fun fixDayMonth(){
        if (month.toInt() > 12){
            val monthTemp = month
            month = day
            day = monthTemp
        }
    }

    override fun toString(): String = "$year-$month-$day"
    fun toID3String(): String = "$day$month"
}